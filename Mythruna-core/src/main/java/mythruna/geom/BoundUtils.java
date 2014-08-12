package mythruna.geom;

import com.jme3.math.Vector3f;

public class BoundUtils {

    public BoundUtils() {
    }

    public static Vector3f repulse(Vector3f aMin, Vector3f aMax, Vector3f bMin, Vector3f bMax, Vector3f result) {
        result.set(0.0F, 0.0F, 0.0F);
        int component = -1;
        float force = (1.0F / 1.0F);
        float forceMagnitude = force;

        for (int i = 0; i < 3; i++) {
            float a1 = aMin.get(i);
            float a2 = aMax.get(i);
            float b1 = bMin.get(i);
            float b2 = bMax.get(i);

            if ((a2 < b1) || (b2 < a1)) {
                return result;
            }

            float aUp = b2 - a1;
            float aDown = b1 - a2;

            float upMagnitude = Math.abs(aUp);
            float downMagnitude = Math.abs(aDown);
            float best = aUp;
            float bestMagnitude = upMagnitude;
            if (downMagnitude < bestMagnitude) {
                best = aDown;
                bestMagnitude = downMagnitude;
            }

            if (bestMagnitude < forceMagnitude) {
                component = i;
                force = best;
                forceMagnitude = bestMagnitude;
            }

        }

        if (component < 0) {
            return result;
        }

        result.set(component, force);

        return result;
    }
}