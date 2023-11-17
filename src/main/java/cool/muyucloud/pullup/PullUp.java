package cool.muyucloud.pullup;

import cool.muyucloud.pullup.client.PullUpClient;
import cool.muyucloud.pullup.common.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.command.ServerCommand;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerC2S;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerS2C;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PullUp.MODID)
public class PullUp {
    public static final String MODID = "pullup";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Config CONFIG = new Config();

    public PullUp() {
        LOGGER.info("Loading config.");
        CONFIG.loadAndCorrect();

        LOGGER.info("Registering arguments.");
        Registry.registerArguments();

        LOGGER.info("Registering operators.");
        Registry.registerOperators();

        LOGGER.info("Registering events & server commands.");
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Registering network.");
        NetworkHandlerS2C.registerMessage();
        NetworkHandlerC2S.registerMessage();

        LOGGER.info("Generating example condition set.");
        ConditionLoader.writeDefaultConditions();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PullUpClient::init);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return CONFIG;
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Dumping current config into file.");
        CONFIG.save();
        LOGGER.info("Generating example condition set.");
        ConditionLoader.writeDefaultConditions();
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        ServerCommand.register(event.getDispatcher());
    }
}
