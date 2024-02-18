package cool.muyucloud.pullup.common.compat.flighthud.components;

import com.plr.flighthud.api.HudComponent;
import com.plr.flighthud.common.Dimensions;
import cool.muyucloud.pullup.PullUp;
import cool.muyucloud.pullup.common.access.ClientPlayerEntityAccess;
import cool.muyucloud.pullup.common.condition.Condition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class PullUpIndicator extends HudComponent {
    private final Dimensions dims;

    public PullUpIndicator(Dimensions dims) {
        this.dims = dims;
    }

    @Override
    public void render(GuiGraphics drawContext, float v, Minecraft mc) {
        if (mc.player == null) return;
        final ClientPlayerEntityAccess access = (ClientPlayerEntityAccess) mc.player;
        final List<Condition.ColoredText> texts = access.pullup$getHudTexts();
        if (texts.isEmpty()) return;
        final float x = dims.wScreen * PullUp.getConfig().getAsFloat("hudTextDisplayX");
        final float y = dims.hScreen * PullUp.getConfig().getAsFloat("hudTextDisplayY");
        for (int i = 0; i < texts.size(); i++) {
            final Condition.ColoredText text = texts.get(i);
            drawContext.drawString(mc.font, text.text(), (int) x, (int) (y + 10 * i), text.color(), false);
        }
    }
}
