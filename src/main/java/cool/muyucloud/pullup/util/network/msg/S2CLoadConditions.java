package cool.muyucloud.pullup.util.network.msg;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.condition.ConditionLoader;
import cool.muyucloud.pullup.util.network.executor.ClientExecutor;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.Objects;

public class S2CLoadConditions {
    private final String spaceName;
    private final String json;

    public S2CLoadConditions() {
        this.spaceName = PullUp.getConfig().getAsString("loadSet");
        String json = "";
        try {
            if (Objects.equals(this.spaceName, "default")) {
                json = new ConditionLoader().getFileContent();
            } else {
                json = new ConditionLoader(this.spaceName).getFileContent();
            }
        } catch (Exception e) {
            NetworkHandlerS2C.getLogger().error(String.format("Error in reading condition set file %s.", spaceName), e);
        }
        this.json = json;
    }

    public S2CLoadConditions(FriendlyByteBuf buf) {
        this.spaceName = buf.readUtf();
        this.json = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.spaceName);
        buf.writeUtf(this.json);
    }

    public void handler(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientExecutor.loadConditions(spaceName, json)));
    }
}
