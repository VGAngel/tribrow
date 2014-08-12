package mythruna.mathd;

public class Mathd {
    
    public Mathd() {
    }

    public static double clamp(double v, double min, double max) {
        return v > max ? max : v < min ? min : v;
    }
}