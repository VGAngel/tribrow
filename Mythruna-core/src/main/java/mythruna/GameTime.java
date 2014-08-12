package mythruna;

public class GameTime {

    private static final double TO_MINUTES = 0.01666666666666667D;
    private static final double TO_HOURS = 0.0002777777777777778D;
    private static final double TO_DAYS = 1.157407407407407E-005D;
    private static final int MONTHS_IN_A_YEAR = 4;
    private static final int DAYS_IN_A_WEEK = 7;
    private static final int DAYS_IN_A_MONTH = 28;
    private static final int DAYS_IN_A_YEAR = 112;
    private long offset;
    private double scale = 1.0D;
    private double toGameTime = this.scale / 1000.0D;

    public GameTime() {
    }

    public final int getGameDay() {
        double t = getTime();
        double d = t * 1.157407407407407E-005D;
        return (int) Math.floor(d);
    }

    public static int toGameDay(double time) {
        double d = time * 1.157407407407407E-005D;
        return (int) Math.floor(d);
    }

    public static int dayOfMonth(int gameDay) {
        int m = gameDay / 28;
        return gameDay - m * 28;
    }

    public final int getMonthDay() {
        int d = getGameDay();
        int m = d / 28;
        return d - m * 28;
    }

    public final int getMonthNumber() {
        int m = getGameDay() / 28;
        return m;
    }

    public final Month getMonth() {
        int m = getMonthNumber() % 4;
        switch (m) {
            case 0:
            default:
                return Month.SPRING;
            case 1:
                return Month.SUMMER;
            case 2:
                return Month.FALL;
            case 3:
        }
        return Month.WINTER;
    }

    public final int getYear() {
        int d = getGameDay();

        return d / 112;
    }

    public void setTimeScale(double scale) {
        this.scale = scale;
        this.toGameTime = (scale / 1000.0D);
    }

    public double getTimeScale() {
        return this.scale;
    }

    public void setTime(double seconds) {
        if (seconds < 0.0D) {
            seconds += 86400.0D;
        }

        long ms = (long) (seconds / this.toGameTime);
        long current = System.currentTimeMillis();

        this.offset = (current - ms);
    }

    public double getTime() {
        long current = System.currentTimeMillis() - this.offset;
        return current * this.toGameTime;
    }
}