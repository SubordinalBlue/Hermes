package earth.terrarium.hermes.api.defaults;

import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.hermes.api.themes.Theme;
import earth.terrarium.hermes.utils.ElementParsingUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;

public abstract class HeadingTagElement extends TextTagElement {

  public final int scale;

  public HeadingTagElement(Map<String, String> parameters, int scale) {
    super(parameters);
    this.scale = scale;
    this.shadowed = ElementParsingUtils.parseBoolean(parameters, "shadowed", false);
  }

  @Override
  public void render(Theme theme, GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, boolean hovered, float partialTicks) {
    try (var ignored = new CloseablePoseStack(graphics)) {
      graphics.pose().scale(scale, scale, scale);
      float translationFactor = (float) (scale - 1) / scale;
      graphics.pose().translate(-x * translationFactor, -y * translationFactor, 0);

      super.render(theme, graphics, x, y, width / scale, mouseX, mouseY, hovered, partialTicks); // check for hover and mouse click stuff
    }
  }

  @Override
  public int getHeight(int width) {
    return scale * super.getHeight(Math.round(width / (float) scale));
  }

  @Override
  public int getWidth() {
    return scale * super.getWidth();
  }

}
