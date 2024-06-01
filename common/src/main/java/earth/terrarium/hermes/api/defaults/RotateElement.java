package earth.terrarium.hermes.api.defaults;

import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.utils.ElementParsingUtils;

import java.util.Map;

// maybe just add this to FaB, and rename that to... "FillBorderAndRotateElement"?
public class RotateElement extends FillAndBorderElement implements TagElement {

  protected Float w;

  protected RotateElement(Map<String, String> parameters) {
    super(parameters);
    this.w = ElementParsingUtils.parseFloat(parameters, "rot", 0f);
  }

}
