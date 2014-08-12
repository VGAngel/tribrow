package mythruna.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import java.util.Arrays;

public class TimeBuffer {

    private FrameTransition[] array;
    private int count;
    private volatile int head = 0;

    private volatile int tail = 0;

    public TimeBuffer(int size) {
        this.array = new FrameTransition[size + 1];
    }

    public void addFrame(long time, Vector3f pos, Quaternion rot) {
        int nextTail = next(this.tail);
        if (nextTail == this.head) {
            this.head = next(this.head);
        }

        if (this.count == 0) {
            this.array[this.tail] = new FrameTransition(time, pos, rot, time, pos, rot);
        } else {
            FrameTransition last = this.array[previous(this.tail)];
            this.array[this.tail] = new FrameTransition(last, time, pos, rot);
        }

        this.tail = next(this.tail);
        if (this.count < this.array.length - 1) {
            this.count += 1;
        }
    }

    public boolean isFilled() {
        return this.count == this.array.length - 1;
    }

    private int next(int index) {
        return (index + 1) % this.array.length;
    }

    private int previous(int index) {
        if (index > 0)
            return index - 1;
        return this.array.length - 1;
    }

    public FrameTransition getFrame(long time) {
        int start = this.head;
        int end = this.tail;

        if ((start == end) && (this.count > 0)) {
            System.out.println("**** TimeBuffer inconsistency.  This shouldn't happen.");
        }

        FrameTransition last = null;
        for (int i = start; i != end; i = next(i)) {
            FrameTransition ft = this.array[i];
            if (time < ft.getStartTime()) {
                return ft;
            }

            if (time <= ft.getEndTime()) {
                return ft;
            }
            last = ft;
        }

        return last;
    }

    public String toString() {
        return "TimeBuffer[ h:" + this.head + ", t:" + this.tail + ", array:" + Arrays.asList(this.array) + "]";
    }

    public static void main(String[] args) {
        TimeBuffer b = new TimeBuffer(4);

        for (int i = 0; i < 4; i++) {
            b.addFrame(i * 10 + 10, new Vector3f(i, 0.0F, 0.0F), null);
            System.out.println("isFilled:" + b.isFilled());
        }

        for (int i = 0; i < 60; i++) {
            FrameTransition t = b.getFrame(i);
            System.out.println("pos[" + i + "] = " + t.getPosition(i));
        }

        System.out.println("Adding some more values...");

        for (int i = 4; i < 7; i++) {
            b.addFrame(i * 10 + 10, new Vector3f(i, 0.0F, 0.0F), null);
            System.out.println("isFilled:" + b.isFilled());
        }

        for (int i = 0; i < 80; i++) {
            FrameTransition t = b.getFrame(i);
            System.out.println("pos[" + i + "] = " + t.getPosition(i));
        }
    }
}