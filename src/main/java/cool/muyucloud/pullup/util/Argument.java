package cool.muyucloud.pullup.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

public interface Argument {
    double compute(LocalPlayer player, Level world);
}
