package earth.terrarium.hermes.api.defaults.columns;

import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.defaults.FillAndBorderElement;
import earth.terrarium.hermes.api.themes.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ColumnsTagElement extends FillAndBorderElement implements TagElement {

    protected final List<TagElement> children = new ArrayList<>();
    List<String> colSpecs = new ArrayList<>();
    List<Integer> colWidths = new ArrayList<>();
    protected boolean colWidthsCalculated = false;

    public ColumnsTagElement(Map<String, String> parameters) {
        super(parameters);
        if (parameters.containsKey("template")) {
            String template = parameters.get("template");
            this.colSpecs = new ArrayList<>(Arrays.asList(template.split(" ")));
        }
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {

        // Valid column specifications (colSpecs) are:
        // - An integer, possibly followed by a '%'. Always interpreted as a percent of the document width.
        // - '@': fit (shrink) to contents.
        //      Currently only items, images, and entities have determinable widths.
        //      TextTag elements (and therefore <p>, <h1>, and <h2>) specify a width with the 'minWidth' attribute.
        //      All other cases default to a 0 width; providing visual feedback in likely 'wrong', layout result.
        // - '*': All columns specified this way share equally in the remaining space.

        // colSpecs, colSpecs, and children are all assumed to be the same size at this point; thanks to addChild().
        // colSpecs have been default set to '*', unless user-supplied via the 'template' attribute.
        // colWidths have been default set to 0
        // Now we set all colWidths according to their corresponding colSpecs.
        // '@' specs necessitate looking up matching children's widths.

        if (!colWidthsCalculated) {
            int sumKnown = 0;
            int countExpands = 0;
            for (int i = 0; i < children.size(); i++) {
                String colSpec = colSpecs.get(i);
                switch (colSpec) {
                    case "*" -> countExpands++;
                    case "@" -> colWidths.set(i, children.get(i).getWidth());
                    default -> {
                        if (colSpec.matches("\\d+%?")) {
                            int percent = colSpec.endsWith("%")
                                    ? Integer.parseInt(colSpec.substring(0, colSpec.length() - 1))
                                    : Integer.parseInt(colSpec);
                            colWidths.set(i, Math.round((percent / 100f) * width));
                        }
                    }
                }
                sumKnown += colWidths.get(i);
            }
            int expandWidth = Math.round((width - sumKnown) / (float) countExpands);
            for (int i = 0; i < colWidths.size(); i++) {
                if (colSpecs.get(i).equals("*")) {
                    colWidths.set(i, expandWidth);
                }
            }
            colWidthsCalculated = true;
        }

        int contentWidth = width - (2 * xSurround);
        int contentHeight = this.getHeight(contentWidth) - (2 * ySurround);
        drawFillAndBorder(graphics, x + xSurround, y + ySurround, contentWidth, contentHeight);

        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            int columnWidth = colWidths.get(i);
            if (child instanceof ColumnTagElement column) {
                column.render(theme, graphics, x + xSurround, y + ySurround, columnWidth, contentHeight, mouseX, mouseY, hovered, partialTicks);
            }
            x += columnWidth;
        }
    }

    @Override
    public int getHeight(int width) {
        int height = 0;
        for (int i = 0; i < children.size(); i++) {
            int columnWidth = colWidths.get(i);
            height = Math.max(height, children.get(i).getHeight(columnWidth));
        }
        return height;
    }

    @Override
    public int getWidth() {
        return this.children.stream().mapToInt(TagElement::getWidth).sum();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        int columnWidth = width / this.children.size();
        int index = 0;
        for (TagElement element : children) {
            if (element.mouseClicked(mouseX - (columnWidth * index), mouseY, button, columnWidth)) {
                return true;
            }
            index++;
        }
        return false;
    }

    @Override
    public void addChild(TagElement element) {
        if (!(element instanceof ColumnTagElement)) {
            throw new IllegalArgumentException("<columns> can only contain <column> elements.");
        }
        this.children.add(element);
        if (colSpecs.size() < children.size()) { colSpecs.add("*"); }
        colWidths.add(0);
    }

    @Override
    public @NotNull List<TagElement> getChildren() {
        return this.children;
    }

}
