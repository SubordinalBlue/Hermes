package earth.terrarium.hermes.api.defaults;

import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
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

import java.util.List;
import java.util.Map;

public class TextTagElement extends FillAndBorderElement implements TagElement {

    protected record RangeSpec (int start, int end) {}

    protected RangeSpec fitWidthTo = new RangeSpec(0, 0);
    protected MutableComponent component = Component.empty();
    protected Alignment align;
    protected boolean shadowed;
    protected float scale;
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
        this.scale = ElementParsingUtils.parseFloat(parameters, "scale", 1);

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

        float translationFactor = (scale - 1) / scale;
        int scaledWidth = Math.round(width/scale);
        x += xSurround;
        y += ySurround;

        // text setup
        List<FormattedCharSequence> lines = font.split(component, scaledWidth + 1 - (2 * xSurround));
        int textWidth = lines.stream().mapToInt(font::width).max().orElse(0) - 1;
        int textHeight = (lines.size() * font.lineHeight) + (lines.size() - 2);

        // alignment of the full element
        var elementWidth = textWidth + (2 * xSurround);
        int elementOffsetX = Alignment.getOffset(scaledWidth, elementWidth, align);

        // draw fill & border
        try (var pose = new CloseablePoseStack(graphics)) {
            pose.scale(scale, scale, 0);
            pose.translate(-x * translationFactor, -y * translationFactor, 0);
            drawFillAndBorder(graphics, x + elementOffsetX, y, textWidth, textHeight);
        }

        // draw text, line by line
        var lineHeight = scale * (font.lineHeight - 1); // strict line-height, no spacing
        int actMouseX = mouseX - x;
        int actMouseY = mouseY - y;
        int height = 0;
        for (FormattedCharSequence line : lines) {
            int lineOffset = Alignment.getOffset(scaledWidth, getLineWidth(line), align);

            // draw the line
            try (var pose = new CloseablePoseStack(graphics)) {
                pose.scale(scale, scale, 0);
                pose.translate(-x * translationFactor, -y * translationFactor, 0);
                theme.drawText(graphics, line, x + lineOffset, y + height, Color.DEFAULT, this.shadowed);
            }

            // deal with hovers
            if (lineOffset <= actMouseX && actMouseX <= width
                && height <= actMouseY && actMouseY <= height + lineHeight) {
                Style style = font.getSplitter().componentStyleAtWidth(line, Math.round((actMouseX - (lineOffset * scale)) / scale));
                graphics.renderComponentHoverEffect(font, style, mouseX, mouseY);
            }
            height += Math.round(lineHeight + 2); // add back 2 for inter-line spacing
        }
    }

//    interface useStyle {
//        public void use(Style style);
//    }
//
//    public boolean withStyleAt(useStyle func, int mouseX, int mouseY, int width, int height) {
//        var lines = font.split(component, scaledWidth + 1 - (2 * xSurround));
//        for (var line : lines) {
//            var lineOffset = ...;d
//            if (...) {
//                Style style = font.getSplitter().componentStyleAtWidth(line, ...);
//                func.doTheThing(style);
//                return true;
//            }
//            return false;
//        }
//    }
//
//    (style, mouseX, mouseY) -> { graphics.renderComponentHoverEffect(font, style, mouseX, mouseY) }
//
//    (style, mouseX, mouseY) -> { handleComponentClicked(style) }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int width) {
        var scaledWidth = Math.round(width/scale);
        var lines = font.split(component, scaledWidth + 1 - (2 * xSurround));
        var lineHeight = scale * (font.lineHeight - 1); // strict line-height, no spacing
        var actMouseX = (float) mouseX - xSurround;
        int height = 0;
        for (FormattedCharSequence line : lines) {
            //int textOffset = scaledOffsetForLine(width, sequence);
            int lineOffset = Alignment.getOffset(scaledWidth, getLineWidth(line), align);
            if (mouseX >= lineOffset && mouseX <= width && mouseY >= height && mouseY <= height + lineHeight) {
                //Style style = font.getSplitter().componentStyleAtWidth(sequence, Mth.floor(mouseX - textOffset));
                Style style = font.getSplitter().componentStyleAtWidth(line, Math.round((actMouseX - (lineOffset * scale)) / scale));

                if (Minecraft.getInstance().screen != null) {
                    Minecraft.getInstance().screen.handleComponentClicked(style);
                }
                return true;
            }
            height += Math.round(lineHeight + 2); // add back 2 for inter-line spacing
        }
        return false;
    }

    @Override
    public int getHeight(int width) {
        int lineCount = font.split(component, width + 1 - (2 * xSurround)).size();
        int lineHeight = font.lineHeight;
        //      lineCount * lineHeight  : clear enough
        //      (lineCount - 2)         : plus 1 for each internal line--not the first or last
        //      (2 * ySurround)         : plus the y surroundings fill & border
        return Math.round(scale * ((lineCount * lineHeight) + (lineCount - 2) + (2 * ySurround)));
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
        return Math.round(scale * (maxWidth + (2 * xSurround) - 1)); // -1 to trim trailing empty space
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

    public int getLineWidth(FormattedCharSequence line) {
        return font.width(line) + (2 * xSurround) - 1; // -1 to trim trailing empty space
    }

    public int scaledOffsetForLine(int width, FormattedCharSequence text) {
        int lineWidth = font.width(text) + (2 * xSurround) - 1; // -1 to trim trailing empty space
        return Alignment.getOffset(width/scale, lineWidth, align);
    }

}
