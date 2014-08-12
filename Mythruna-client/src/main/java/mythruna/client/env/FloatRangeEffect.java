package mythruna.client.env;

public class FloatRangeEffect extends RangeEffect<Float> {
    public FloatRangeEffect(Float[] values) {
        super(values);
    }

    protected Float interpolate(Float val1, Float val2, float ratio) {
        return Float.valueOf(val1.floatValue() + (val2.floatValue() - val1.floatValue()) * ratio);
    }
}