package cool.muyucloud.pullup.common.compat.flighthud;

import com.plr.flighthud.api.HudRegistry;
import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.compat.flighthud.components.PullUpIndicator;

public class FlightHudCompat {
    public static void init() {
        if (!HudRegistry.addComponent(($, dims) -> new PullUpIndicator(dims)))
            PullUp.getLogger().warn("Failed to add component to flight hud for pullup.");
    }
}
