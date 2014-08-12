package mythruna.client.anim;

import com.jme3.app.state.AppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

public class Animation {
    public Animation() {
    }

    public static AnimationTask move(Spatial s, Vector3f start, Vector3f end, float time) {
        return new Move(s, start, end, time);
    }

    public static AnimationTask scale(Spatial s, float startScale, float endScale, float time) {
        return new Scale(s, startScale, endScale, time);
    }

    public static AnimationTask detach(Spatial s, float time) {
        return new Detach(s, time);
    }

    public static AnimationTask enable(ViewPort view, boolean b, float time) {
        return new EnableViewPort(view, b, time);
    }

    public static AnimationTask enable(AppState state, boolean b, float time) {
        return new EnableState(state, b, time);
    }

    private static class Scale
            implements AnimationTask {
        private Spatial spatial;
        private float start;
        private float end;
        private float delta;
        private float totalTime;
        private float inverseTotal;
        private float time;

        public Scale(Spatial s, float startScale, float endScale, float totalTime) {
            this.spatial = s;
            this.start = startScale;
            this.end = endScale;
            this.totalTime = totalTime;
            this.delta = (this.end - this.start);
            this.inverseTotal = (1.0F / totalTime);
        }

        public boolean animate(AnimationState anim, float seconds) {
            if (this.time >= this.totalTime) {
                this.spatial.setLocalScale(this.end);
                return false;
            }

            float part = this.time * this.inverseTotal;
            float scale = this.start + this.delta * part;

            this.spatial.setLocalScale(scale);

            this.time += seconds;

            return true;
        }
    }

    private static class Move
            implements AnimationTask {
        private Spatial spatial;
        private Vector3f start;
        private Vector3f end;
        private Vector3f delta;
        private float totalTime;
        private float inverseTotal;
        private float time;

        public Move(Spatial s, Vector3f start, Vector3f end, float totalTime) {
            this.spatial = s;
            this.start = start;
            this.end = end;
            this.delta = end.subtract(start);
            this.totalTime = totalTime;
            this.inverseTotal = (1.0F / totalTime);
        }

        public boolean animate(AnimationState anim, float seconds) {
            if (this.time >= this.totalTime) {
                this.spatial.setLocalTranslation(this.end);
                return false;
            }

            float part = this.time * this.inverseTotal;
            Vector3f pos = this.start.add(this.delta.mult(part));

            this.spatial.setLocalTranslation(pos);

            this.time += seconds;

            return true;
        }
    }

    private static class Detach
            implements AnimationTask {
        private Spatial spatial;
        private float totalTime;
        private float time;

        public Detach(Spatial s, float totalTime) {
            this.spatial = s;
            this.totalTime = totalTime;
        }

        public boolean animate(AnimationState anim, float seconds) {
            if (this.time >= this.totalTime) {
                this.spatial.removeFromParent();
                return false;
            }
            this.time += seconds;
            return true;
        }
    }

    private static class EnableState
            implements AnimationTask {
        private AppState state;
        private float totalTime;
        private float time;
        private boolean enabled;

        public EnableState(AppState state, boolean enabled, float totalTime) {
            this.state = state;
            this.enabled = enabled;
            this.totalTime = totalTime;
        }

        public boolean animate(AnimationState anim, float seconds) {
            if (this.time >= this.totalTime) {
                this.state.setEnabled(this.enabled);
                return false;
            }
            this.time += seconds;
            return true;
        }
    }

    private static class EnableViewPort
            implements AnimationTask {
        private ViewPort view;
        private float totalTime;
        private float time;
        private boolean enabled;

        public EnableViewPort(ViewPort view, boolean enabled, float totalTime) {
            this.view = view;
            this.enabled = enabled;
            this.totalTime = totalTime;
        }

        public boolean animate(AnimationState anim, float seconds) {
            if (this.time >= this.totalTime) {
                this.view.setEnabled(this.enabled);
                return false;
            }
            this.time += seconds;
            return true;
        }
    }
}