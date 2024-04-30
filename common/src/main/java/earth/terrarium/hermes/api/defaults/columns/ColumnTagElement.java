package earth.terrarium.hermes.api.defaults.columns;

import earth.terrarium.hermes.api.Alignment;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.defaults.FillAndBorderElement;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColumnTagElement extends FillAndBorderElement implements TagElement {

    protected List<TagElement> children = new ArrayList<>();
    protected Alignment vAlign;

    public ColumnTagElement(Map<String, String> parameters) {
        super(parameters);
        this.vAlign = ElementParsingUtils.parseAlignment(parameters, "valign", Alignment.MIDDLE);
    }

    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {

        int contentWidth = width - (2 * xSurround);
        int contentHeight = height - (2 * ySurround);
        drawFillAndBorder(graphics, x + xSurround, y + ySurround, contentWidth, contentHeight);

        y += ySurround + Alignment.getOffset(contentHeight, this.getChildrenHeight(contentWidth), vAlign);
        x += xSurround;
        for (TagElement element : this.children) {
            element.render(theme, graphics, x, y, contentWidth, mouseX, mouseY, hovered, partialTicks);
            y += element.getHeight(contentWidth);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        int height = 0;
        for (TagElement element : this.children) {
            if (element.mouseClicked(mouseX, mouseY - height, button, width)) {
                return true;
            }
            height += element.getHeight(width);
        }
        return false;
    }

    public int getChildrenHeight(int width) {
        int height = 0;
        for (TagElement element : this.children) {
            height += element.getHeight(width);
        }
        return height;
    }

    @Override
    public int getHeight(int width) {
        return getChildrenHeight(width) + (2 * ySurround);
    }

    public int getWidth() {
        return this.children.stream().mapToInt(TagElement::getWidth).max().orElse(0);
    }

    @Override
    public void addChild(TagElement element) {
        this.children.add(element);
    }

    @Override
    public @NotNull List<TagElement> getChildren() {
        return this.children;
    }

}
