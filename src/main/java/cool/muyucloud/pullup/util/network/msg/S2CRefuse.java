package cool.muyucloud.pullup.util.network.msg;

import cool.muyucloud.pullup.util.network.executor.ClientExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public class S2CRefuse {
    public S2CRefuse() {
    }

    public S2CRefuse(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public void handler(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientExecutor::notifyRefused));
    }
}
