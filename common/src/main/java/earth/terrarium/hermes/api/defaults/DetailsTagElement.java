package earth.terrarium.hermes.api.defaults;

import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailsTagElement implements TagElement {

    protected List<TagElement> children = new ArrayList<>();

    private final String summary;

    private boolean open;

    public DetailsTagElement(Map<String, String> parameters) {
        this.summary = parameters.getOrDefault("summary", "");
        this.open = ElementParsingUtils.parseBoolean(parameters, "open", false);
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        String text = (this.summary.isEmpty() ? "Details" : this.summary);
        boolean dropdownHovered = hovered && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 22;
        theme.drawDropdown(graphics, x, y + 2, width, dropdownHovered, open, text);
        if (open) {
            int height = 24;
            for (TagElement element : this.children) {
                element.render(theme, graphics, x + 7, y + height, width - 7, mouseX, mouseY, hovered, partialTicks);
                height += element.getHeight(width);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        if (mouseX < 0 || mouseX > width || mouseY < 0 || mouseY > 22) {
            mouseY -= 22;
            for (TagElement child : this.children) {
                if (child.mouseClicked(mouseX, mouseY, button, width)) {
                    return true;
                }
                mouseY -= child.getHeight(width);
            }
            return false;
        }
        this.open = !this.open;
        return true;
    }

    @Override
    public int getHeight(int width) {
        int height = 24;
        if (!open) return height;
        for (TagElement element : this.children) {
            height += element.getHeight(width);
        }
        return height + 2;
    }

    @Override
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
