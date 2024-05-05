package earth.terrarium.hermes.api.defaults.layers;

import earth.terrarium.hermes.api.Alignment;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LayerTagElement implements TagElement {

    protected record Shift(int x, int y) {}
    protected Shift shift;
    protected Alignment vAlign;
    protected final List<TagElement> children = new ArrayList<>();

    public LayerTagElement (Map<String, String> parameters) {
        this.vAlign = ElementParsingUtils.parseAlignment(parameters, "valign", Alignment.MIDDLE);
        if (parameters.containsKey("shift")) {
            String shiftSpec = parameters.get("shift");
            String[] shifts = shiftSpec.split(" ");
            int x = (shifts.length > 0) ? Integer.parseInt(shifts[0]) : 0;
            int y = (shifts.length > 1) ? Integer.parseInt(shifts[1]) : 0;
            this.shift = new Shift(x, y);
        } else {
            this.shift = new Shift(0, 0);
        }
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        x += this.shift.x();
        y += this.shift.y();
        int height = 0;
        for (TagElement element : this.children) {
            element.render(theme, graphics, x, y + height, width, mouseX, mouseY, hovered, partialTicks);
            height += element.getHeight(width);
        }
    }

    @Override
    public int getHeight(int width) {
        return children.stream().mapToInt((child) -> child.getHeight(width)).sum();
    }

    @Override
    public int getWidth() { return children.stream().mapToInt(TagElement::getWidth).max().orElse(0); }

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
