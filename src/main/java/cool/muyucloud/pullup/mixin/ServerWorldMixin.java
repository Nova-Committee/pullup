package cool.muyucloud.pullup.mixin;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerS2C;
import cool.muyucloud.pullup.util.network.msg.S2CClearConditions;
import cool.muyucloud.pullup.util.network.msg.S2CLoadConditions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {
    @Inject(method = "addNewPlayer", at = @At("TAIL"))
    public void inject$addNewPlayer(ServerPlayer player, CallbackInfo ci) {
        if (PullUp.getConfig().getAsBool("sendServer")) {
            final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> player);
            NetworkHandlerS2C.S2C_CLEAR_CONDITIONS.send(target, new S2CClearConditions());
            NetworkHandlerS2C.S2C_LOAD_CONDITIONS.send(target, new S2CLoadConditions());
        }
    }
}
