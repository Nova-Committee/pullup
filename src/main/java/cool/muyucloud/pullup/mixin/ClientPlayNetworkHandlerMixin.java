package cool.muyucloud.pullup.mixin;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Registry;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Objects;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handleLogin", at = @At("HEAD"))
    public void inject$handleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (!PullUp.getConfig().getAsBool("loadServer")) {
            try {
                this.pullup$loadConditions();
            } catch (Exception e) {
                PullUp.getLogger().error("Try to load local condition set but failed.", e);
            }
        }
    }

    @Unique
    private void pullup$loadConditions() throws IOException {
        Registry.CONDITIONS.clear();
        String loadSet = PullUp.getConfig().getAsString("loadSet");
        if (Objects.equals(loadSet, "default")) {
            new ConditionLoader().load();
        } else {
            new ConditionLoader(loadSet).load();
        }
    }
}
