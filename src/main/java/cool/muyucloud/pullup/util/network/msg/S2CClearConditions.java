package cool.muyucloud.pullup.util.network.msg;

import cool.muyucloud.pullup.util.network.executor.ClientExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CClearConditions {
    public S2CClearConditions() {
    }

    public S2CClearConditions(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public void handler(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientExecutor::clearConditions));
        ctx.setPacketHandled(true);
    }
}
