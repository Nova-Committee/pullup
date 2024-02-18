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
    private static final Config pullup$CONFIG = PullUp.getConfig();
    @Unique
    private int pullup$ticks = 0;
    @Unique
    private boolean pullup$isNewTick = false;
    @Unique
    private long pullup$flightStart = new Date().getTime();
    @Unique
    private final HashMap<ResourceLocation, ConditionTrigger> pullup$conditionTriggers = new HashMap<>();
    @Unique
    private final HashSet<ResourceLocation> pullup$triggersToRemove = new HashSet<>();
    @Unique
    private final TreeMap<ResourceLocation, Condition.ColoredText> pullup$hudTexts = new TreeMap<>();

    public ClientPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.pullup$updateTick();
        if (!this.pullup$isNewTick) {
            return;
        }

        this.pullup$checkConditions();
        this.pullup$playSoundsAndDisplayTexts();
        this.pullup$clearTriggers();
    }

    @Unique
    private void pullup$checkConditions() {
        for (Condition condition : Registry.CONDITIONS.getAll()) {
            if (this.pullup$ticks % condition.getCheckDelay() != 0) {
                continue;
            }

            this.pullup$registerTrigger(condition.getId());
            ConditionTrigger trigger = this.pullup$conditionTriggers.get(condition.getId());

            if (!condition.verifyExpressions(((LocalPlayer) (Object) this), this.level())) {
                trigger.isTriggered = false;
                trigger.lastPlay = -1;
                continue;
            }

            trigger.isTriggered = true;
        }
    }

    @Unique
    private void pullup$registerTrigger(ResourceLocation id) {
        if (this.pullup$conditionTriggers.containsKey(id)) {
            return;
        }

        ConditionTrigger trigger = new ConditionTrigger();
        trigger.lastPlay = -1;
        this.pullup$conditionTriggers.put(id, trigger);
    }

    @Unique
    private void pullup$playSoundsAndDisplayTexts() {
        if (this.minecraft.level == null) {
            return;
        }

        for (ResourceLocation id : this.pullup$conditionTriggers.keySet()) {
            Condition condition = Registry.CONDITIONS.get(id);
            if (condition == null) {
                this.pullup$triggersToRemove.add(id);
                continue;
            }

            ConditionTrigger trigger = this.pullup$conditionTriggers.get(id);
            final Condition.ColoredText hudText = condition.getHudText();
            if (!trigger.isTriggered) {
                if (!hudText.isEmpty()) pullup$hudTexts.remove(id);
                continue;
            } else {
                if (!hudText.isEmpty()) pullup$hudTexts.put(id, hudText);
            }

            if (!condition.shouldLoopPlay()) {
                if (trigger.lastPlay == -1) {
                    this.minecraft.level.playLocalSound(this.getX(), this.getY(), this.getZ(),
                            SoundEvent.createFixedRangeEvent(condition.getSound(), 0),
                            SoundSource.VOICE, 1.0F, 1.0F, false);
                    trigger.lastPlay = this.pullup$ticks;
                }
                continue;
            }

            if (condition.getPlayDelay() < (this.pullup$ticks - trigger.lastPlay)) {
                trigger.lastPlay = this.pullup$ticks;
                this.minecraft.level.playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvent.createFixedRangeEvent(condition.getSound(), 0),
                        SoundSource.VOICE, 1.0F, 1.0F, false);
            }
        }
    }

    @Unique
    private void pullup$clearTriggers() {
        for (ResourceLocation id : this.pullup$triggersToRemove) {
            this.pullup$conditionTriggers.remove(id);
        }
        this.pullup$triggersToRemove.clear();
    }

    @Unique
    private void pullup$updateTick() {
        if (!this.isFallFlying()) {
            this.pullup$isNewTick = false;
            this.pullup$ticks = 0;
            this.pullup$flightStart = new Date().getTime();
            return;
        }

        long tmpTime = new Date().getTime();
        int tmpTick = (int) ((tmpTime - this.pullup$flightStart) / 50);
        this.pullup$isNewTick = tmpTick != this.pullup$ticks;
        this.pullup$ticks = tmpTick;
    }

    @Unique
    @Override
    public double pullup$getDistanceHorizontal() {
        int maxDistance = pullup$CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = this.getEyePosition(0);
        Vec3 rotate = this.calculateViewVector(0, this.getYRot());
        Vec3 endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3 target = this.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getLocation();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double pullup$getPitchedDistanceAhead(float pitch) {
        int maxDistance = pullup$CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = this.getEyePosition(0);
        Vec3 rotate = this.calculateViewVector(this.getXRot() + pitch, this.getYRot());
        Vec3 endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3 target = this.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getLocation();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double pullup$getRelativeHeight() {
        int maxDistance = pullup$CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = this.getEyePosition(0);
        Vec3 endPos = cameraPos.add(0, -maxDistance, 0);
        Vec3 target = this.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getLocation();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double pullup$getDeltaYaw() {
        return this.getYRot() - this.yRotLast;
    }

    @Unique
    @Override
    public double pullup$getDeltaPitch() {
        return this.getXRot() - this.xRotLast;
    }

    @Unique
    @Override
    public double pullup$getFlightTicks() {
        return this.pullup$ticks;
    }

    @Unique
    @Override
    public List<Condition.ColoredText> pullup$getHudTexts() {
        return pullup$hudTexts.values().stream().toList();
    }
}
