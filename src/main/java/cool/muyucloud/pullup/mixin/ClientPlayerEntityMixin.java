package cool.muyucloud.pullup.mixin;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.access.ClientPlayerEntityAccess;
import cool.muyucloud.pullup.common.condition.Condition;
import cool.muyucloud.pullup.common.condition.ConditionTrigger;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends Player implements ClientPlayerEntityAccess {
    @Shadow
    @Override
    public abstract float getViewYRot(float tickDelta);

    @Shadow
    @Override
    public abstract void tick();

    @Shadow
    private float yRotLast;
    @Shadow
    private float xRotLast;
    @Shadow
    @Final
    protected Minecraft minecraft;
    @Unique
    private static final Config CONFIG = PullUp.getConfig();
    @Unique
    private int ticks = 0;
    @Unique
    private boolean isNewTick = false;
    @Unique
    private long flightStart = new Date().getTime();
    @Unique
    private final HashMap<ResourceLocation, ConditionTrigger> conditionTriggers = new HashMap<>();
    @Unique
    private final HashSet<ResourceLocation> triggersToRemove = new HashSet<>();
    @Unique
    private final TreeMap<ResourceLocation, Condition.ColoredText> hudTexts = new TreeMap<>();

    public ClientPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.updateTick();
        if (!this.isNewTick) {
            return;
        }

        this.checkConditions();
        this.playSoundsAndDisplayTexts();
        this.clearTriggers();
    }

    @Unique
    private void checkConditions() {
        for (Condition condition : Registry.CONDITIONS.getAll()) {
            if (this.ticks % condition.getCheckDelay() != 0) {
                continue;
            }

            this.registerTrigger(condition.getId());
            ConditionTrigger trigger = this.conditionTriggers.get(condition.getId());

            if (!condition.verifyExpressions(((LocalPlayer) (Object) this), this.level())) {
                trigger.isTriggered = false;
                trigger.lastPlay = -1;
                continue;
            }

            trigger.isTriggered = true;
        }
    }

    @Unique
    private void registerTrigger(ResourceLocation id) {
        if (this.conditionTriggers.containsKey(id)) {
            return;
        }

        ConditionTrigger trigger = new ConditionTrigger();
        trigger.lastPlay = -1;
        this.conditionTriggers.put(id, trigger);
    }

    @Unique
    private void playSoundsAndDisplayTexts() {
        if (this.minecraft.level == null) {
            return;
        }

        for (ResourceLocation id : this.conditionTriggers.keySet()) {
            Condition condition = Registry.CONDITIONS.get(id);
            if (condition == null) {
                this.triggersToRemove.add(id);
                continue;
            }

            ConditionTrigger trigger = this.conditionTriggers.get(id);
            final Condition.ColoredText hudText = condition.getHudText();
            if (!trigger.isTriggered) {
                if (!hudText.isEmpty()) hudTexts.remove(id);
                continue;
            } else {
                if (!hudText.isEmpty()) hudTexts.put(id, hudText);
            }

            if (!condition.shouldLoopPlay()) {
                if (trigger.lastPlay == -1) {
                    this.minecraft.level.playLocalSound(this.getX(), this.getY(), this.getZ(),
                            SoundEvent.createFixedRangeEvent(condition.getSound(), 0),
                            SoundSource.VOICE, 1.0F, 1.0F, false);
                    trigger.lastPlay = this.ticks;
                }
                continue;
            }

            if (condition.getPlayDelay() < (this.ticks - trigger.lastPlay)) {
                trigger.lastPlay = this.ticks;
                this.minecraft.level.playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvent.createFixedRangeEvent(condition.getSound(), 0),
                        SoundSource.VOICE, 1.0F, 1.0F, false);
            }
        }
    }

    @Unique
    private void clearTriggers() {
        for (ResourceLocation id : this.triggersToRemove) {
            this.conditionTriggers.remove(id);
        }
        this.triggersToRemove.clear();
    }

    @Unique
    private void updateTick() {
        if (!this.isFallFlying()) {
            this.isNewTick = false;
            this.ticks = 0;
            this.flightStart = new Date().getTime();
            return;
        }

        long tmpTime = new Date().getTime();
        int tmpTick = (int) ((tmpTime - this.flightStart) / 50);
        this.isNewTick = tmpTick != this.ticks;
        this.ticks = tmpTick;
    }

    @Unique
    @Override
    public double getDistanceHorizontal() {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = this.getEyePosition(0);
        Vec3 rotate = this.calculateViewVector(0, this.getYRot());
        Vec3 endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3 target = this.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getLocation();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getPitchedDistanceAhead(float pitch) {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = this.getEyePosition(0);
        Vec3 rotate = this.calculateViewVector(this.getXRot() + pitch, this.getYRot());
        Vec3 endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3 target = this.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getLocation();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getRelativeHeight() {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = this.getEyePosition(0);
        Vec3 endPos = cameraPos.add(0, -maxDistance, 0);
        Vec3 target = this.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getLocation();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getDeltaYaw() {
        return this.getYRot() - this.yRotLast;
    }

    @Unique
    @Override
    public double getDeltaPitch() {
        return this.getXRot() - this.xRotLast;
    }

    @Unique
    @Override
    public double getFlightTicks() {
        return this.ticks;
    }

    @Unique
    @Override
    public List<Condition.ColoredText> getHudTexts() {
        return hudTexts.values().stream().toList();
    }
}
