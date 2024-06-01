package earth.terrarium.hermes.api.defaults;

import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.hermes.api.Alignment;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class ItemTagElement extends RotateElement implements TagElement {

    protected final ItemStack output;
    protected final float scale;
    protected final Alignment align;

    public ItemTagElement(Map<String, String> parameters) {
        super(parameters);
        Item item = ElementParsingUtils.parseItem(parameters, "id", Items.AIR);
        CompoundTag tag = ElementParsingUtils.parseTag(parameters, "tag", null);
        ItemStack stack = new ItemStack(item);
        if (tag != null) stack.setTag(tag);
        this.output = stack;
        this.scale = ElementParsingUtils.parseFloat(parameters, "scale", 1.0F);
        this.align = ElementParsingUtils.parseAlignment(parameters, "align", Alignment.MIDDLE);
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        try (var pose = new CloseablePoseStack(graphics)) {
            float scaleSize = scale * 16;
            int offsetX = xSurround + Alignment.getOffset(width, scaleSize + (2 * xSurround), align);
            int offsetY = ySurround;
            pose.translate(x + offsetX + (scaleSize/2), y + offsetY + (scaleSize/2), 0);
            pose.scale(scale, scale, 1.0F);
            pose.mulPose(Axis.ZP.rotationDegrees(w));
            drawFillAndBorder(graphics, -8, -8, 16, 16);
            graphics.renderFakeItem(output, -8, -8);
        }
    }

    @Override
    public int getHeight(int width) {
        return Mth.ceil(16 * scale) + (2 * ySurround);
    }
}
