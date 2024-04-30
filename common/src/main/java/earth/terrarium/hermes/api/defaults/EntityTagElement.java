package earth.terrarium.hermes.api.defaults;

import earth.terrarium.hermes.api.Alignment;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class EntityTagElement extends FillAndBorderElement implements TagElement {

    private final static int BLOCK_SIZE = 24;
    private final int blockScale;
    private final float layoutHeight;
    private final float layoutWidth;
    private final float vShift;
    private final Alignment align;
    private Entity entity;

    public EntityTagElement(Map<String, String> parameters) {
        super(parameters);
        var type = ElementParsingUtils.parseEntityType(parameters, "type", null);
        var tag = ElementParsingUtils.parseTag(parameters, "tag", null);
        var scale = ElementParsingUtils.parseFloat(parameters, "scale", 1.0f);
        this.align = ElementParsingUtils.parseAlignment(parameters, "align", Alignment.MIDDLE);
        this.vShift = ElementParsingUtils.parseFloat(parameters, "vshift", 0.0f);
        var layoutBlocksHigh = Math.abs(ElementParsingUtils.parseFloat(parameters, "height", 0.0f));
        var layoutBlocksWide = Math.abs(ElementParsingUtils.parseFloat(parameters, "width", 0.0f));

        if (type != null) {
            this.entity = type.create(Minecraft.getInstance().level);
            if (entity != null && tag != null) {
                entity.load(tag);
            }
        }
        if (entity instanceof LivingEntity living) {
            if (layoutBlocksHigh == 0.0f) layoutBlocksHigh = living.getBbHeight();
            if (layoutBlocksWide == 0.0f) layoutBlocksWide = living.getBbWidth();
        }

        this.blockScale = Math.round(scale * BLOCK_SIZE);
        this.layoutHeight = blockScale * layoutBlocksHigh;
        this.layoutWidth = blockScale * layoutBlocksWide;
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        /*
        Note on layout and rendering of living entities:
        Only the bounding box's dimensions are straight-forwardly available; the render call refers only to it.
        However, several mobs' models visually exceed their bounding box. The horse and squid are two examples.
        The 'width' and 'height' tag attributes affect the conceptual box that is implied by the layout.
        The 'vshift' attribute shifts the render vertically in relation to actual bounding box; + is up, - is down.
        The fill-able background area should correspond to the implied-by-layout box.
        */

        if (this.entity != null && entity instanceof LivingEntity living) {

            int layoutX = x + xSurround + Alignment.getOffset(width, layoutWidth + (2 * xSurround), align);
            int renderX = Math.round(layoutX + (layoutWidth / 2f));

            int layoutY = y + ySurround;
            int renderY = Math.round(layoutY + layoutHeight - (vShift * blockScale));

            int eyeY = layoutY + Math.round(layoutHeight - (living.getEyeHeight() * blockScale));
            int lookX = renderX - mouseX;
            int lookY = eyeY - mouseY;

            drawFillAndBorder(graphics, layoutX, layoutY, layoutWidth, layoutHeight);
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, renderX, renderY, blockScale, lookX, lookY, living);
        }
    }

    @Override
    public int getHeight(int width) {
        return Math.round(layoutHeight + (2 * ySurround));
    }

    @Override
    public int getWidth() {
        return Math.round(layoutWidth);
    }
}
