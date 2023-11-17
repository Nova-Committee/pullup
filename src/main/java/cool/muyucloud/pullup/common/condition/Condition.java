package cool.muyucloud.pullup.common.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.util.Registry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public class Condition {
    private static final Logger LOGGER = PullUp.getLogger();
    public static final int DEFAULT_COLOR = new Color(255, 0, 0, 127).getRGB();
    ;

    private final ResourceLocation id;
    private final int checkDelay;
    private final int playDelay;
    private final boolean loopPlay;
    private final ColoredText hudText;
    private final ResourceLocation sound;
    private final HashMap<String, ResourceLocation> arguments;
    private final HashSet<Expression> expressions;

    public Condition(String spaceName, JsonObject object) {
        this.id = new ResourceLocation(spaceName, object.get("name").getAsString());
        this.sound = new ResourceLocation(object.get("sound").getAsString());
        this.loopPlay = !object.has("loop_play") || object.get("loop_play").getAsBoolean();
        this.playDelay = loopPlay && object.has("play_delay") ? object.get("play_delay").getAsInt() : 40;
        this.checkDelay = object.has("check_delay") ? object.get("check_delay").getAsInt() : 5;
        if (object.has("hud_text")) {
            JsonObject textMap = object.get("hud_text").getAsJsonObject();
            final boolean hasText = textMap.has("key");
            if (hasText) {
                final Component text = Component.translatable(textMap.get("key").getAsString());
                int hudTextColor = DEFAULT_COLOR;
                if (textMap.has("color")) {
                    final JsonObject color = textMap.get("color").getAsJsonObject();
                    hudTextColor = new Color(
                            color.has("red") ? color.get("red").getAsInt() : 0,
                            color.has("green") ? color.get("green").getAsInt() : 0,
                            color.has("blue") ? color.get("blue").getAsInt() : 0,
                            color.has("alpha") ? color.get("alpha").getAsInt() : 255
                    ).getRGB();
                }
                this.hudText = new ColoredText(text, hudTextColor);
            } else this.hudText = ColoredText.EMPTY;
        } else {
            this.hudText = ColoredText.EMPTY;
        }
        this.arguments = new HashMap<>();
        this.expressions = new HashSet<>();

        JsonObject argMap = object.get("arguments").getAsJsonObject();
        for (String key : argMap.keySet()) {
            String value = argMap.get(key).getAsString();
            if (this.isArgumentValid(key, value)) {
                this.arguments.put(key, new ResourceLocation(value));
            } else {
                LOGGER.warn(
                        String.format(
                                "Argument mapping %s:%s is invalid, does argument id exists? Or variable got invalid character?",
                                key, value));
            }
        }

        for (JsonElement element : object.getAsJsonArray("expressions")) {
            this.expressions.add(parseExpression(element.getAsString()));
        }

        // verify if expression is valid.
        this.tryCompute();
    }

    private void tryCompute() {
        for (Expression expression : this.expressions) {
            for (String arg : this.arguments.keySet()) {
                expression.setVariable(arg, 0);
            }
            expression.evaluate();
        }
    }

    private boolean isArgumentValid(String key, String id) {
        ResourceLocation k = ResourceLocation.tryParse("pullup:" + key);
        ResourceLocation v = ResourceLocation.tryParse(id);
        return k != null && v != null && Registry.ARGUMENTS.has(v);
    }

    @NotNull
    private Expression parseExpression(String expression) {
        ExpressionBuilder builder = new ExpressionBuilder(expression);
        for (String var : this.arguments.keySet()) {
            builder.variable(var);
        }
        for (Operator operator : Registry.OPERATORS.getAll()) {
            builder.operator(operator);
        }
        return builder.build();
    }

    public ResourceLocation getSound() {
        return this.sound;
    }

    public ColoredText getHudText() {
        return hudText;
    }

    public boolean verifyExpressions(LocalPlayer player, Level world) {
        for (Expression expression : this.expressions) {
            if (computeExpression(player, world, expression) < 0) return false;
        }
        return true;
    }

    private double computeExpression(LocalPlayer player, Level world, Expression expression) {
        for (String var : this.arguments.keySet()) {
            double value = Registry.ARGUMENTS.get(this.arguments.get(var)).compute(player, world);
            expression.setVariable(var, value);
        }
        return expression.evaluate();
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public int getPlayDelay() {
        return this.playDelay;
    }

    public int getCheckDelay() {
        return this.checkDelay;
    }

    public boolean shouldLoopPlay() {
        return this.loopPlay;
    }

    public record ColoredText(Component text, int color) {
        public static final ColoredText EMPTY = new ColoredText(Component.empty(), 0);

        public boolean isEmpty() {
            return this.equals(EMPTY);
        }
    }
}
