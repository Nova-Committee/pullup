package cool.muyucloud.pullup.util.network.handler;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.util.network.msg.C2SGrabConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandlerC2S {
    public static SimpleChannel C2S_GRAB;
    private static int id = 0;
    public static final ResourceLocation GRAB_CONDITIONS = new ResourceLocation(PullUp.MODID, "grab_conditions");

    public static int nextId() {
        return id++;
    }

    public static void registerMessage() {
        C2S_GRAB = NetworkRegistry.newSimpleChannel(
                GRAB_CONDITIONS,
                () -> NetworkRegistry.ACCEPTVANILLA,
                NetworkRegistry.ACCEPTVANILLA::equals,
                NetworkRegistry.ACCEPTVANILLA::equals
        );
        C2S_GRAB.messageBuilder(C2SGrabConditions.class, nextId())
                .encoder(C2SGrabConditions::toBytes)
                .decoder(C2SGrabConditions::new)
                .consumerMainThread(C2SGrabConditions::handler)
                .add();
    }
}
