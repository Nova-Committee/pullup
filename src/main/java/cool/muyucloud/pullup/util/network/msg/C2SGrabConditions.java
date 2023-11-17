package cool.muyucloud.pullup.util.network.msg;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Date;

public class C2SGrabConditions {
    private static long LAST_SEND = new Date().getTime();

    public C2SGrabConditions() {
    }

    public C2SGrabConditions(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public void handler(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null) return;
            long tmp = new Date().getTime();
            final Config cfg = PullUp.getConfig();
            final PacketDistributor.PacketTarget dist = PacketDistributor.PLAYER.with(ctx.getSender());
            if (LAST_SEND + cfg.getAsInt("sendDelay") >= tmp || cfg.getAsBool("sendServer")) {
                NetworkHandlerS2C.S2C_REFUSE.send(new S2CRefuse(), dist);
                return;
            }
            LAST_SEND = tmp;
            NetworkHandlerS2C.S2C_CLEAR_CONDITIONS.send(new S2CClearConditions(), dist);
            NetworkHandlerS2C.S2C_LOAD_CONDITIONS.send(new S2CLoadConditions(), dist);
        });
    }
}
