package cool.muyucloud.pullup.util.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.network.handler.NetworkHandlerS2C;
import cool.muyucloud.pullup.util.network.msg.S2CClearConditions;
import cool.muyucloud.pullup.util.network.msg.S2CLoadConditions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.PacketDistributor;

public class ServerCommand {
    private static final SuggestionProvider<CommandSourceStack> CONDITION_SETS = (context, builder) -> SharedSuggestionProvider.suggest(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = PullUp.getConfig();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("pullupServer")
                .requires(source -> source.hasPermission(2));

        root.then(buildLoad());
        root.then(Commands.literal("enableSend").executes(ServerCommand::enableSend));
        root.then(Commands.literal("disableSend").executes(ServerCommand::disableSend));

        dispatcher.register(root);
    }

    private static int enableSend(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MutableComponent text = Component.translatable("command.pullup.server.enableSend");
        source.sendSuccess(() -> text, true);
        CONFIG.set("sendServer", true);
        return 1;
    }

    private static int disableSend(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MutableComponent text = Component.translatable("command.pullup.server.disableSend");
        source.sendSuccess(() -> text, true);
        CONFIG.set("sendServer", false);
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
            source.sendSuccess(() -> Component.translatable("command.pullup.client.load.specific.notExist"), false);
            return 0;
        }

        MutableComponent text = Component.translatable("command.pullup.client.load.specific.loading");
        source.sendSuccess(() -> text, true);
        CONFIG.set("loadSet", name);
        if (CONFIG.getAsBool("sendServer")) {
            final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(source::getPlayer);
            NetworkHandlerS2C.S2C_CLEAR_CONDITIONS.send(target, new S2CClearConditions());
            NetworkHandlerS2C.S2C_LOAD_CONDITIONS.send(target, new S2CLoadConditions());
        }
        return 1;
    }

    private static int loadDefault(CommandSourceStack source) {
        MutableComponent text = Component.translatable("command.pullup.client.load.default");
        source.sendSuccess(() -> text, true);
        CONFIG.set("loadSet", "default");
        if (CONFIG.getAsBool("sendServer")) {
            final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(source::getPlayer);
            NetworkHandlerS2C.S2C_CLEAR_CONDITIONS.send(target, new S2CClearConditions());
            NetworkHandlerS2C.S2C_LOAD_CONDITIONS.send(target, new S2CLoadConditions());
        }
        return 1;
    }
}
