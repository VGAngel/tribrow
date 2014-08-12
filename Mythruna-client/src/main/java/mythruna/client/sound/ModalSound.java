package mythruna.client.sound;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ModalSound extends AbstractAmbientSound {
    private static final long MIN_FRAME = 20L;
    private SoundEntry current;
    private ConcurrentLinkedQueue<SoundEntry> queue = new ConcurrentLinkedQueue();
    private float pitch = 1.0F;

    public ModalSound() {
    }

    protected void adjustVolume(float v) {
        if (this.current != null)
            this.current.setVolume(v);
    }

    protected void adjustEnabled(boolean enabled) {
    }

    public void setPitch(float pitch) {
        if (this.pitch == pitch)
            return;
        this.pitch = pitch;
        if (this.current != null)
            this.current.setPitch(pitch);
    }

    public void update(float f) {
    }

    public void update(long time) {
        if (this.queue.isEmpty()) {
            return;
        }

        if (this.current != null) {
            if (this.current.stop(time))
                this.current = null;
            else {
                return;
            }
        }
        if (!isEnabled()) {
            return;
        }
        this.current = ((SoundEntry) this.queue.poll());
        if (this.current == null) {
            return;
        }

        this.current.setVolume(getEffectiveVolume());
        this.current.setPitch(this.pitch);
        this.current.play(time);
    }

    public void stop() {
        nextSound(null, 0L, 0L);
    }

    public void nextSound(SoundNode sound, long minTime, long interval) {
        this.queue.clear();
        if ((this.current != null) && (this.current.sound == sound)) {
            this.current.resetStop();
            return;
        }

        this.queue.add(new SoundEntry(sound, minTime, interval));
    }

    protected class SoundEntry {
        private long startTime = -1L;
        private SoundNode sound;
        private long adjustedMinTime;
        private long adjustedInterval;
        private long minTime;
        private long interval;
        private long stopTime = -1L;

        public SoundEntry(SoundNode sound, long minTime, long interval) {
            this.sound = sound;
            this.minTime = minTime;
            this.adjustedMinTime = minTime;
            this.interval = interval;
        }

        public void setVolume(float vol) {
            if (this.sound != null)
                this.sound.setVolume(vol);
        }

        public void setPitch(float vol) {
            if (this.sound != null)
                this.sound.setPitch(vol);
            this.adjustedMinTime = Math.round((float) this.minTime * (1.0F / vol));
            this.adjustedInterval = Math.round((float) this.interval * (1.0F / vol));
        }

        public void play(long time) {
            this.startTime = time;
            if (this.sound != null)
                this.sound.play();
        }

        public void resetStop() {
            this.stopTime = -1L;
        }

        public boolean stop(long time) {
            if (this.startTime == -1L) {
                return true;
            }
            if (this.stopTime != -1L) {
                if (time >= this.stopTime) {
                    if (this.sound != null)
                        this.sound.stop();
                    return true;
                }

                return false;
            }

            long duration = time - this.startTime;

            if (duration < this.adjustedMinTime) {
                this.stopTime = (time + (this.adjustedMinTime - duration));
                return false;
            }

            if (this.adjustedInterval != 0L) {
                long part = duration % this.adjustedInterval;
                long left = this.adjustedInterval - part;

                if ((left > 200L) && (part > 20L)) {
                    this.stopTime = (time + left);
                    return false;
                }

            }

            if (this.sound != null)
                this.sound.stop();
            return true;
        }
    }
}