package earth.terrarium.hermes.api.defaults.carousel;

import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CarouselItemTagElement implements TagElement {

    protected List<TagElement> children = new ArrayList<>();

    public CarouselItemTagElement(Map<String, String> ignored) {}

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        int height = 0;
        for (TagElement element : this.children) {
            element.render(theme, graphics, x, y + height, width, mouseX, mouseY, hovered, partialTicks);
            height += element.getHeight(width);
        }
    }

    @Override
    public int getHeight(int width) {
        int height = 0;
        for (TagElement element : this.children) {
            height += element.getHeight(width);
        }
        return height;
    }

    @Override
    public int getWidth() {
        return this.children.stream().mapToInt(TagElement::getWidth).max().orElse(0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        for (TagElement element : this.children) {
            if (element.mouseClicked(mouseX, mouseY, button, width)) {
                return true;
            }
            mouseY -= element.getHeight(width);
        }
        return false;
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
