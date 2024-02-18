package cool.muyucloud.pullup.common.access;

import cool.muyucloud.pullup.common.condition.Condition;

import java.util.List;

/**
 * Duck-types for ClientPlayerEntityMixin
 * To provide access when only ClientPlayerEntity is present but CLientPlayerEntityMixin is not.
 */
public interface ClientPlayerEntityAccess {
    /**
     * Get distance to a block or fluid in front of a player horizontally.
     * Ignoring the pitch.
     */
    double pullup$getPitchedDistanceAhead(float pitch);

    /**
     * Get distance to a block or fluid in front of a player horizontally.
     * Considering the pitch.
     */
    double pullup$getDistanceHorizontal();

    /**
     * Get distance from the player to the block right below.
     */
    double pullup$getRelativeHeight();

    /**
     * Get change of the yaw.
     * Compare current yaw with yaw at last client tick.
     */
    double pullup$getDeltaYaw();

    /**
     * Get change of the pitch.
     * Compare current pitch with pitch at last client tick.
     */
    double pullup$getDeltaPitch();

    /**
     * Get ticks since last beginning of flight.
     * 1 tick = 50 ms
     */
    double pullup$getFlightTicks();

    /**
     * Get pullup texts to be displayed on the flight hud
     */
    List<Condition.ColoredText> pullup$getHudTexts();
}
