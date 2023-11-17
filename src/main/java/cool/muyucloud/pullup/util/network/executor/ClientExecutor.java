package cool.muyucloud.pullup.util.network.executor;

import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientExecutor {
    public static void notifyRefused() {
        Minecraft.getInstance().gui.getChat().addMessage(
                Component.translatable("network.client.pullup.refuse.receive").withStyle(ChatFormatting.RED));
    }

    public static void loadConditions(String spaceName, String json) {
        if (PullUp.getConfig().getAsBool("loadServer")) {
            new ConditionLoader(spaceName, json).load();
        }
    }

    public static void clearConditions() {
        if (PullUp.getConfig().getAsBool("loadServer")) {
            Registry.CONDITIONS.clear();
        }
    }
}
