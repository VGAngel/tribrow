package mythruna.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Progress {
    private static ConcurrentMap<String, Progress> map = new ConcurrentHashMap();

    private static volatile int totalMax = 0;
    private static volatile int totalProgress = 0;
    private static volatile String lastMessage = null;
    private String name;
    private String message;
    private int progress;
    private int max = 0;

    private Progress(String name) {
        this.name = name;
        setMax(100);
    }

    public static String getLastMessage() {
        return lastMessage;
    }

    public static int getTotalMax() {
        return totalMax;
    }

    public static int getTotalProgress() {
        return totalProgress;
    }

    public static int getTotalPercent() {
        if (totalMax == 0)
            return 0;
        return totalProgress * 100 / totalMax;
    }

    public static Progress get(String name) {
        Progress p = (Progress) map.get(name);
        if (p == null) {
            map.putIfAbsent(name, new Progress(name));
            p = (Progress) map.get(name);
        }
        return p;
    }

    public int getPercent() {
        return this.progress * 100 / this.max;
    }

    public boolean isDone() {
        return this.progress == this.max;
    }

    public void setMessage(String message) {
        this.message = message;
        lastMessage = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setProgress(String message, int progress) {
        setMessage(message);
        setProgress(progress);
    }

    public void setProgress(int progress) {
        progress = Math.min(this.max, progress);
        if (this.progress == progress) {
            return;
        }
        int change = progress - this.progress;
        this.progress = progress;
        totalProgress += change;
    }

    public void setMax(int max) {
        totalMax -= this.max;
        this.max = max;
        totalMax += max;
    }

    public int getMax() {
        return this.max;
    }

    public int getProgress() {
        return this.progress;
    }

    public String toString() {
        return "Progress[" + this.name + " = " + getPercent() + "%]";
    }
}