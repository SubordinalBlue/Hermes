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
        // Valid column specifications (colSpecs) are:
        // - An integer, possibly followed by a '%'. Always interpreted as a percent of the document width.
        // - '*': All columns specified this way share equally in any remaining, unspecified width.
        // - '@': fit (shrink) to contents.
        //      Items, images, and entities have determinable widths.
        //      TextTag elements (and therefore <p>, <h1>, and <h2>) specify a width with the 'minWidth' attribute.
        //      All other cases default to a 0 width; providing visual feedback in a likely 'wrong', layout result.
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {

        x += xSurround; // xSurround / 2
        y += ySurround;

        int contentWidth = width - (2 * xSurround); // xSurround
        int contentHeight = maxChildrenHeight(contentWidth);
        drawFillAndBorder(graphics, x, y, contentWidth, contentHeight);

        if (!colWidthsCalculated) {
            calculateColWidths(contentWidth);
            colWidthsCalculated = true;
        }

        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            int columnWidth = colWidths.get(i);
            if (child instanceof ColumnTagElement column) {
                column.render(theme, graphics, x, y, columnWidth, contentHeight, mouseX, mouseY, hovered, partialTicks);
            }
            x += columnWidth;
        }
    }

    public void calculateColWidths (int contentWidth) {
        // colWidths set or calculated from their corresponding colSpecs.
        // We do this here once; not all needed information is available beforehand.
        // '@' specs necessitate looking up children's widths.
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
                        colWidths.set(i, Math.round((percent / 100f) * contentWidth));
                    }
                }
            }
            sumKnown += colWidths.get(i);
        }
        int expandWidth = Math.round((contentWidth - sumKnown) / (float) countExpands);
        for (int i = 0; i < colWidths.size(); i++) {
            if (colSpecs.get(i).equals("*")) {
                colWidths.set(i, expandWidth);
            }
        }
    }

    public int maxChildrenHeight(int width) {
        int height = 0;
        for (int i = 0; i < children.size(); i++) {
            int columnWidth = colWidths.get(i);
            height = Math.max(height, children.get(i).getHeight(columnWidth));
        }
        return height;
    }

    @Override
    public int getHeight(int width) {
        return maxChildrenHeight(width) + (2 * ySurround);
    }

    @Override
    public int getWidth() {
        // Return sum of child <column>s' widths.
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
        // colSpecs (default '*'), and colWidths (default 0), should be the same size as children
        if (colSpecs.size() < children.size()) { colSpecs.add("*"); }
        colWidths.add(0);
    }

    @Override
    public @NotNull List<TagElement> getChildren() {
        return this.children;
    }

}
