package in.fortytwo42.adapter.cam.util;



public class TimeUtil {
    private Long currentValue;

    public long start() {
        long value = System.currentTimeMillis();
        this.setCurrentValue(value);
        return value;
    }

    public long stop() {
        return System.currentTimeMillis() - getCurrentValue();
    }

    private void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }

    private Long getCurrentValue() {
        return currentValue;
    }
}
