package mythruna.client.env;

public abstract class RangeEffect<T> implements TimeEffect {

    private T[] values;
    private T lastValue;

    public RangeEffect(T[] values) {
        this.values = values;
        if (values.length != 24)
            throw new IllegalArgumentException("There must be 24 range values.");
    }

    public T getLastValue() {
        return this.lastValue;
    }

    protected void update(T value) {
    }

    protected abstract T interpolate(T paramT1, T paramT2, float paramFloat);

    protected void rangeUpdate(int hour1, int hour2, float ratio) {
        T val1 = this.values[hour1];
        T val2 = this.values[hour2];

        T result = interpolate(val1, val2, ratio);
        if ((result == this.lastValue) || (result.equals(this.lastValue))) {
            return;
        }
        update(result);
        this.lastValue = result;
    }

    public void timeUpdate(float hours, double totalTime) {
        int hour1 = (int) Math.floor(hours);
        int hour2 = hour1 + 1;

        float ratio = hours - hour1;

        if (hour2 >= 24) {
            hour2 %= 24;
        }
        rangeUpdate(hour1, hour2, ratio);
    }
}
