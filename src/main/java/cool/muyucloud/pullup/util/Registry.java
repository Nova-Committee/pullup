package cool.muyucloud.pullup.util;

import cool.muyucloud.pullup.common.access.ClientPlayerEntityAccess;
import cool.muyucloud.pullup.common.condition.Condition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

import java.util.Collection;
import java.util.HashMap;

public class Registry<T> {
    public static final Registry<Function> FUNCTIONS = new Registry<>();
    public static final Registry<Argument> ARGUMENTS = new Registry<>();
    public static final Registry<Condition> CONDITIONS = new Registry<>();
    public static final Registry<Operator> OPERATORS = new Registry<>();

    public static void registerArguments() {
        ARGUMENTS.register(new ResourceLocation("pullup:absolute_height"), (player, world) -> player.getY());
        ARGUMENTS.register(new ResourceLocation("pullup:relative_height"),
                (player, world) -> ((ClientPlayerEntityAccess) player).pullup$getRelativeHeight());
        ARGUMENTS.register(new ResourceLocation("pullup:speed"), (player, world) -> {
            Vec3 velocity = player.getDeltaMovement();
            double x = velocity.x;
            double y = velocity.y;
            double z = velocity.z;
            return Math.pow(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2), 0.5);
        });
        ARGUMENTS.register(new ResourceLocation("pullup:horizontal_speed"), (player, world) -> {
            Vec3 velocity = player.getDeltaMovement();
            double x = velocity.x;
            double z = velocity.z;
            return Math.pow(Math.pow(x, 2) + Math.pow(z, 2), 0.5);
        });
        ARGUMENTS.register(new ResourceLocation("pullup:vertical_speed"), (player, world) -> player.getDeltaMovement().y);
        ARGUMENTS.register(new ResourceLocation("pullup:yaw"), (player, world) -> player.getYRot());
        ARGUMENTS.register(new ResourceLocation("pullup:delta_yaw"),
                (player, world) -> ((ClientPlayerEntityAccess) player).pullup$getDeltaYaw());
        ARGUMENTS.register(new ResourceLocation("pullup:pitch"), (player, world) -> player.getXRot());
        ARGUMENTS.register(new ResourceLocation("pullup:delta_pitch"),
                (player, world) -> ((ClientPlayerEntityAccess) player).pullup$getDeltaPitch());
        ARGUMENTS.register(new ResourceLocation("pullup:distance_ahead"),
                (player, world) -> ((ClientPlayerEntityAccess) player).pullup$getPitchedDistanceAhead(0));
        ARGUMENTS.register(new ResourceLocation("pullup:distance_horizontal"),
                (player, world) -> ((ClientPlayerEntityAccess) player).pullup$getDistanceHorizontal());
        ARGUMENTS.register(new ResourceLocation("pullup:flight_ticks"),
                (player, world) -> ((ClientPlayerEntityAccess) player).pullup$getFlightTicks());
    }

    public static void registerOperators() {
        OPERATORS.register(new ResourceLocation("pullup:gt"), new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] > args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:geq"), new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] >= args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:lt"), new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] < args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:leq"), new Operator("<=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] <= args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:eq"), new Operator("==", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] == args[1] ? 1 : -1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:neq"), new Operator("!=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
            @Override
            public double apply(double... args) {
                return args[0] == args[1] ? -1 : 1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:and"), new Operator("&", 2, true, Operator.PRECEDENCE_ADDITION - 2) {
            @Override
            public double apply(double... args) {
                return args[0] >= 0 && args[1] >= 0 ? 1 : -1;
            }
        });
        OPERATORS.register(new ResourceLocation("pullup:or"), new Operator("|", 2, true, Operator.PRECEDENCE_ADDITION - 3) {
            @Override
            public double apply(double... args) {
                return args[0] >= 0 || args[1] >= 0 ? 1 : -1;
            }
        });
    }

    public static void registerFunctions() {
        FUNCTIONS.register(new ResourceLocation("pullup:pitched_distance"), new Function("pDistance") {
            @Override
            public double apply(double... args) {
                return 0;
            }
        });
    }

    private final HashMap<ResourceLocation, T> registries = new HashMap<>();

    public void register(ResourceLocation id, T content) {
        this.registries.put(id, content);
    }

    public T get(ResourceLocation name) {
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

    public boolean has(ResourceLocation id) {
        return this.registries.containsKey(id);
    }
}
