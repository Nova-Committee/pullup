package cool.muyucloud.pullup.util.network.msg;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Date;
import java.util.function.Supplier;

public class C2SGrabConditions {
    private static long LAST_SEND = new Date().getTime();

    public C2SGrabConditions() {
    }

    public C2SGrabConditions(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public void handler(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null) return;
            long tmp = new Date().getTime();
            final Config cfg = PullUp.getConfig();
            final PacketDistributor.PacketTarget dist = PacketDistributor.PLAYER.with(ctx::getSender);
            if (LAST_SEND + cfg.getAsInt("sendDelay") >= tmp || cfg.getAsBool("sendServer")) {
                NetworkHandlerS2C.S2C_REFUSE.send(dist, new S2CRefuse());
                return;
            }
            LAST_SEND = tmp;
            NetworkHandlerS2C.S2C_CLEAR_CONDITIONS.send(dist, new S2CClearConditions());
            NetworkHandlerS2C.S2C_LOAD_CONDITIONS.send(dist, new S2CLoadConditions());
        });
    }
}
