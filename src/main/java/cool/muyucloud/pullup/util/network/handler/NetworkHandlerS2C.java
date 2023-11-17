package cool.muyucloud.pullup.util.network.handler;

import com.mojang.logging.LogUtils;
import cool.muyucloud.pullup.util.network.msg.S2CClearConditions;
import cool.muyucloud.pullup.util.network.msg.S2CLoadConditions;
import cool.muyucloud.pullup.util.network.msg.S2CRefuse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import org.slf4j.Logger;

public class NetworkHandlerS2C {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static SimpleChannel S2C_CLEAR_CONDITIONS;
    public static SimpleChannel S2C_LOAD_CONDITIONS;
    public static SimpleChannel S2C_REFUSE;

    public static final ResourceLocation CLEAR_CONDITIONS = new ResourceLocation("pullup:clear_conditions");
    public static final ResourceLocation LOAD_CONDITIONS = new ResourceLocation("pullup:load_conditions");
    public static final ResourceLocation REFUSE = new ResourceLocation("pullup:refuse");
    private static int id = 0;

    public static int nextId() {
        return id++;
    }

    public static void registerMessage() {
        S2C_CLEAR_CONDITIONS = ChannelBuilder.named(CLEAR_CONDITIONS)
                .optional()
                .simpleChannel();
        S2C_CLEAR_CONDITIONS.messageBuilder(S2CClearConditions.class, nextId())
                .encoder(S2CClearConditions::toBytes)
                .decoder(S2CClearConditions::new)
                .consumerMainThread(S2CClearConditions::handler)
                .add();
        S2C_LOAD_CONDITIONS = ChannelBuilder.named(LOAD_CONDITIONS)
                .optional()
                .simpleChannel();
        S2C_LOAD_CONDITIONS.messageBuilder(S2CLoadConditions.class, nextId())
                .encoder(S2CLoadConditions::toBytes)
                .decoder(S2CLoadConditions::new)
                .consumerMainThread(S2CLoadConditions::handler)
                .add();
        S2C_REFUSE = ChannelBuilder.named(REFUSE)
                .optional()
                .simpleChannel();
        S2C_REFUSE.messageBuilder(S2CRefuse.class, nextId())
                .encoder(S2CRefuse::toBytes)
                .decoder(S2CRefuse::new)
                .consumerMainThread(S2CRefuse::handler)
                .add();
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
