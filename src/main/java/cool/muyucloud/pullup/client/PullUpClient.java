package cool.muyucloud.pullup.client;

import cool.muyucloud.pullup.common.compat.CompatHandler;
import cool.muyucloud.pullup.util.command.ClientCommand;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PullUpClient {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        LOGGER.info("Registering client commands.");
        MinecraftForge.EVENT_BUS.register(new PullUpClient());
        LOGGER.info("Handling mod compatibilities.");
        CompatHandler.init();
    }

    @SubscribeEvent
    public void onRegisterClientCommand(RegisterClientCommandsEvent event) {
        ClientCommand.register(event.getDispatcher());
    }
}
