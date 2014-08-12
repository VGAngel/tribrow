package mythruna.client.env;

import com.jme3.math.ColorRGBA;

public class ColorRangeEffect extends RangeEffect<ColorRGBA> {
    public ColorRangeEffect(ColorRGBA[] values) {
        super(values);
    }

    protected ColorRGBA interpolate(ColorRGBA val1, ColorRGBA val2, float ratio) {
        ColorRGBA result = val1.clone();
        result.interpolate(val2, ratio);
        return result;
    }
}