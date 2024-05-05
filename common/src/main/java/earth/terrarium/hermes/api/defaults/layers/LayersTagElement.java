package earth.terrarium.hermes.api.defaults.layers;

import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.hermes.api.Alignment;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LayersTagElement implements TagElement {

    // <layers>'s purpose is to have its children layout/render directly on top of each other.
    // Child <layer>s will be rendered _last_ to _first_, with respect to the order they were added.
    // Thus matching visually their order of appearance in the xml.

    protected final List<TagElement> children = new ArrayList<>();
    public LayersTagElement (Map<String, String> ignoredParameters) { }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        int offsetY;
        int offsetZ = 0;
        int areaHeight = this.getHeight(width);
        for (TagElement child : children) {
            if (child instanceof LayerTagElement layer) {
                offsetY = Alignment.getOffset(areaHeight, child.getHeight(width), layer.vAlign);
                offsetZ++;
                try (var pose = new CloseablePoseStack(graphics)) {
                    pose.translate(0, 0, 100 * offsetZ++);
                    child.render(theme, graphics, x, y + offsetY, width, mouseX, mouseY, hovered, partialTicks);
                }
            }
        }
    }

    @Override
    public int getHeight(int width) {
        return children.stream().mapToInt((child) -> child.getHeight(width)).max().orElse(0);
    }

    // getWidth()
    // return children.stream().mapToInt(::getWidth).max().orElse(0);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        // Only have top child receive clicks?
        return children.get(children.size() - 1).mouseClicked(mouseX, mouseY, button, width);
        // -Maybe- deal with covered/not covered?
        // Vertical text would suck, but maybe ignore that corner case?
    }

    @Override
    public void addChild(TagElement element) {
        if (!(element instanceof LayerTagElement)) {
            throw new IllegalArgumentException("LayersTagElement can only contain LayerElements");
        }
        this.children.add(0, element);
    }

    @Override
    public @NotNull List<TagElement> getChildren() {
        return this.children;
    }

}