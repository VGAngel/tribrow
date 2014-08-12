package mythruna.client.env;

import com.jme3.math.Vector4f;

public class Vector4fRangeEffect extends RangeEffect<Vector4f> {
    public Vector4fRangeEffect(Vector4f[] values) {
        super(values);
    }

    protected Vector4f interpolate(Vector4f val1, Vector4f val2, float ratio) {
        Vector4f result = val1.clone();
        result.interpolate(val2, ratio);
        return result;
    }
}