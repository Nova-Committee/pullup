package cool.muyucloud.pullup.util;

import cool.muyucloud.pullup.access.ServerPlayerEntityAccess;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.objecthunter.exp4j.operator.Operator;

import java.util.Collection;
import java.util.HashMap;

public class Registry<T> {
    public static final Registry<Argument> ARGUMENTS = new Registry<>();
    public static final Registry<Condition> CONDITIONS = new Registry<>();
    public static final Registry<Operator> OPERATOR = new Registry<>();

    public static void registerArguments() {
        ARGUMENTS.register(new Identifier("pullup:absolute_height"), (player, world) -> player.getY());
        ARGUMENTS.register(new Identifier("pullup:relative_height"), (player, world) -> ((ServerPlayerEntityAccess) player).getRelativeHeight());
        ARGUMENTS.register(new Identifier("pullup:speed"), (player, world) -> {
            Vec3d velocity = player.getVelocity();
            double x = velocity.x;
            double y = velocity.y;
            double z = velocity.z;
            return Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
        });
        ARGUMENTS.register(new Identifier("pullup:horizontal_speed"), (player, world) -> {
            Vec3d velocity = player.getVelocity();
            double x = velocity.x;
            double z = velocity.z;
            return Math.pow(Math.pow(x, 2) + Math.pow(z, 2), 0.5);
        });
        ARGUMENTS.register(new Identifier("pullup:vertical_speed"), (player, world) -> player.getVelocity().y);
        ARGUMENTS.register(new Identifier("pullup:yaw"), (player, world) -> player.getYaw());
        ARGUMENTS.register(new Identifier("pullup:pitch"), (player, world) -> player.getPitch());
        ARGUMENTS.register(new Identifier("pullup:distance_ahead"), (player, world) -> ((ServerPlayerEntityAccess) player).getDistanceAhead());
        ARGUMENTS.register(new Identifier("pullup:distance_pitched_10"), (player, world) -> ((ServerPlayerEntityAccess) player).getDistancePitched10());
        ARGUMENTS.register(new Identifier("pullup:distance_pitched_m10"), (player, world) -> ((ServerPlayerEntityAccess) player).getDistancePitchedM10());
    }

    public static void registerOperators() {
        OPERATOR.register(new Identifier("pullup:gt"), new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] > args[1] ? 1 : -1;
            }
        });
        OPERATOR.register(new Identifier("pullup:geq"), new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] >= args[1] ? 1 : -1;
            }
        });
        OPERATOR.register(new Identifier("pullup:lt"), new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] < args[1] ? 1 : -1;
            }
        });
        OPERATOR.register(new Identifier("pullup:leq"), new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] <= args[1] ? 1 : -1;
            }
        });
        OPERATOR.register(new Identifier("pullup:eq"), new Operator("==", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] == args[1] ? 1 : -1;
            }
        });
        OPERATOR.register(new Identifier("pullup:and"), new Operator("&", 2, true, Operator.PRECEDENCE_ADDITION - 2) {
            @Override
            public double apply(double... args) {
                return args[0] >= 0 && args[1] >= 0 ? 1 : -1;
            }
        });
        OPERATOR.register(new Identifier("pullup:or"), new Operator("|", 2, true, Operator.PRECEDENCE_ADDITION - 3) {
            @Override
            public double apply(double... args) {
                return args[0] >= 0 || args[1] >= 0 ? 1 : -1;
            }
        });
    }

    private final HashMap<Identifier, T> registries = new HashMap<>();

    public void register(Identifier id, T content) {
        this.registries.put(id, content);
    }

    public T get(Identifier name) {
        return this.registries.get(name);
    }

    /**
     * Not a copy! Please do not modify.
     * Provide convenience for mixin class to execute.
     */
    public Collection<T> getAll() {
        return this.registries.values();
    }

    public void clear() {
        this.registries.clear();
    }
}
