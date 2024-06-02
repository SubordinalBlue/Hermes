package earth.terrarium.hermes.api.defaults;

import com.teamresourceful.resourcefullib.common.color.Color;
import earth.terrarium.hermes.api.Alignment;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.TagProvider;
import earth.terrarium.hermes.api.text.ChildTextTagElement;
import earth.terrarium.hermes.api.text.TextTagProvider;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public class TextTagElement extends FillAndBorderElement implements TagElement {

    protected record RangeSpec (int start, int end) {}

    protected RangeSpec fitWidthTo = new RangeSpec(0, 0);
    protected MutableComponent component = Component.empty();
    protected Alignment align;
    protected boolean shadowed;
    protected Font font = Minecraft.getInstance().font;

    public TextTagElement(Map<String, String> parameters) {
        super(parameters);
        this.component.setStyle(Style.EMPTY
                .withBold(ElementParsingUtils.parseBoolean(parameters, "bold", false))
                .withItalic(ElementParsingUtils.parseBoolean(parameters, "italic", false))
                .withUnderlined(ElementParsingUtils.parseBoolean(parameters, "underline", false))
                .withObfuscated(ElementParsingUtils.parseBoolean(parameters, "obfuscated", false))
                .withStrikethrough(ElementParsingUtils.parseBoolean(parameters, "strikethrough", false))
                .withColor(ElementParsingUtils.parseColor(parameters, "color", Color.DEFAULT).getValue())
        );
        this.align = ElementParsingUtils.parseAlignment(parameters, "align", Alignment.MIN);
        this.shadowed = ElementParsingUtils.parseBoolean(parameters, "shadowed", true);

        if (parameters.containsKey("fit")) {
            // One argument is "end", with assumed start at beginning
            // Two arguments is "start end"
            String fitArgs = parameters.get("fit");
            String[] args = fitArgs.split(" ");
            int start = (args.length >= 2) ? Integer.parseInt(args[0]) : 1;
            int end = (args.length >= 2) ? Integer.parseInt(args[1]) : Integer.parseInt(args[0]);
            fitWidthTo = new RangeSpec(start, end);
        }

        if (component.getStyle().isItalic()) {
            // Using FillAndBorder to accommodate italic's width
            xSurround += 1;
        }
    }

    @Override
    public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        x = x + xSurround;
        y = y + ySurround;

        List<FormattedCharSequence> lines = font.split(component, width + 1 - (2 * xSurround));
        int maxWidth = lines.stream().mapToInt(font::width).max().orElse(0) - 1;
        int maxHeight = (lines.size() * font.lineHeight) + (lines.size() - 2);
        int offsetX = Alignment.getOffset(width, maxWidth + (2 * xSurround), align);

        drawFillAndBorder(graphics, x + offsetX, y, maxWidth, maxHeight);

        int actMouseX = mouseX - x;
        int actMouseY = mouseY - y;
        int height = 0;
        for (FormattedCharSequence line : lines) {
            int textOffset = getOffsetForTextTag(width, line);
            theme.drawText(graphics, line, x + textOffset, y + height, Color.DEFAULT, this.shadowed);

            if (actMouseX >= textOffset && actMouseX <= width && actMouseY >= height && actMouseY <= height + font.lineHeight) {
                graphics.renderComponentHoverEffect(
                    font,
                    font.getSplitter().componentStyleAtWidth(line, Mth.floor(actMouseX - textOffset)),
                    mouseX, mouseY
                );
            }
            height += font.lineHeight + 1;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        int height = 0;
        for (FormattedCharSequence sequence : font.split(component, width + 1 - (2 * xSurround))) {
            int textOffset = getOffsetForTextTag(width, sequence);
            if (mouseX >= textOffset && mouseX <= width && mouseY >= height && mouseY <= height + font.lineHeight) {
                Style style = font.getSplitter().componentStyleAtWidth(sequence, Mth.floor(mouseX - textOffset));
                if (Minecraft.getInstance().screen != null) {
                    Minecraft.getInstance().screen.handleComponentClicked(style);
                }
                return true;
            }
            height += Minecraft.getInstance().font.lineHeight + 1;
        }
        return false;
    }

    @Override
    public int getHeight(int width) {
        int lineCount = font.split(component, width + 1 - (2 * xSurround)).size();
        int lineHeight = font.lineHeight;
        // explain this formula
        return ((lineCount * lineHeight) + (lineCount - 2)) + (2 * ySurround);
    }

    @Override
    public int getWidth() {
        var contentString = component.getString();
        // Convert from 1-indexing and clamp extremes; `end`  can be left alone, due to String.substring's use of it.
        int start = Math.max(fitWidthTo.start - 1, 0);
        int end = Math.min(fitWidthTo.end, contentString.length());
        var subString = contentString.substring(start, end);
        var subWidth = font.width(subString);

        List<FormattedCharSequence> lines = font.split(component, subWidth);
        int maxWidth = lines.stream().mapToInt(font::width).max().orElse(0);
        return maxWidth + (2 * xSurround) - 1; // -1 to trim trailing empty space
    }

    @Override
    public void addText(String content) {
        this.component.append(content);
    }

    @Override
    public void addChild(TagElement element) {
        if (element instanceof TextTagElement textTag) {
            this.component.append(textTag.component);
        } else if (element instanceof ChildTextTagElement textTag) {
            this.component.append(textTag.component());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public TagProvider getChildTagProvider(TagProvider parent) {
        return TextTagProvider.INSTANCE;
    }

    public int getOffsetForTextTag(int width, FormattedCharSequence text) {
        int textWidth = font.width(text) + (2 * xSurround) - 1; // -1 to trim trailing empty space
        return Alignment.getOffset(width, textWidth, align);
    }
}
