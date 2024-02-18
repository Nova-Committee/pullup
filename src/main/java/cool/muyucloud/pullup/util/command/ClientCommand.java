package cool.muyucloud.pullup.util.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerC2S;
import cool.muyucloud.pullup.util.network.msg.C2SGrabConditions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Logger;

public class ClientCommand {
    private static final SuggestionProvider<CommandSourceStack> CONDITION_SETS =
            (context, builder) -> SharedSuggestionProvider.suggest(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = PullUp.getConfig();
    private static final Logger LOGGER = PullUp.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("pullupClient");

        root.then(buildLoad());
        root.then(Commands.literal("enable").executes(ClientCommand::executeEnable));
        root.then(Commands.literal("disable").executes(ClientCommand::executeDisable));
        root.then(Commands.literal("grab").executes(ClientCommand::grabConditions));
        root.then(Commands.literal("enableServer").executes(ClientCommand::enableServer));
        root.then(Commands.literal("enableServer").executes(ClientCommand::disableServer));

        dispatcher.register(root);
    }

    private static int executeEnable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MutableComponent text = Component.translatable("command.pullup.client.enable");
        source.sendSystemMessage(text);
        CONFIG.set("enable", true);
        return 1;
    }

    private static int executeDisable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MutableComponent text = Component.translatable("command.pullup.client.disable");
        source.sendSystemMessage(text);
        CONFIG.set("enable", true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildLoad() {
        LiteralArgumentBuilder<CommandSourceStack> conditionSet = Commands.literal("load");

        conditionSet.then(
                Commands.argument("setName", StringArgumentType.string()).suggests(CONDITION_SETS)
                        .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        conditionSet.then(
                Commands.literal("default").executes(context -> loadDefault(context.getSource()))
        );

        return conditionSet;
    }

    private static int loadSet(String name, CommandSourceStack source) {
        if (!ConditionLoader.containsFile(name)) {
            source.sendFailure(Component.translatable("command.pullup.client.load.specific.notExist", name));
            return 0;
        }

        MutableComponent text = Component.translatable("command.pullup.client.load.specific.loading");
        source.sendSystemMessage(text);
        CONFIG.set("loadSet", name);
        Registry.CONDITIONS.clear();
        try {
            new ConditionLoader(name).load();
        } catch (Exception e) {
            LOGGER.error(String.format("Failed to load condition set %s.", name));
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    private static int loadDefault(CommandSourceStack source) {
        MutableComponent text = Component.translatable("command.pullup.client.load.default");
        source.sendFailure(text);
        CONFIG.set("loadSet", "default");
        Registry.CONDITIONS.clear();
        new ConditionLoader().load();
        return 1;
    }

    private static int grabConditions(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!CONFIG.getAsBool("loadServer")) {
            source.sendFailure(Component.translatable("command.pullup.client.grab.enableLoadServer"));
            return 0;
        }

        NetworkHandlerC2S.C2S_GRAB.sendToServer(new C2SGrabConditions());
        source.sendSystemMessage(Component.translatable("command.pullup.client.grab.sent"));
        return 1;
    }

    private static int enableServer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MutableComponent text = Component.translatable("command.pullup.client.loadServer.enable");
        source.sendSystemMessage(text);
        CONFIG.set("loadServer", true);
        return 1;
    }

    private static int disableServer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MutableComponent text = Component.translatable("command.pullup.client.loadServer.disable");
        source.sendSystemMessage(text);
        CONFIG.set("loadServer", false);
        return 1;
    }
}
