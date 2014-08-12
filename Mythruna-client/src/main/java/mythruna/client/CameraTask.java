package mythruna.client;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockTypeIndex;
import mythruna.Coordinates;
import mythruna.MovementState;
import mythruna.World;
import mythruna.db.BlueprintData;
import mythruna.db.WorldDatabase;
import mythruna.mathd.Vec3d;
import mythruna.phys.CollisionMesh;
import mythruna.phys.Contact;
import mythruna.phys.MaskStrategies;
import mythruna.phys.MaskStrategy;
import mythruna.sim.FrameTransition;
import mythruna.sim.TimeBuffer;

import java.util.ArrayList;
import java.util.List;

public abstract class CameraTask implements PhysicsTask {

    private Vector3f position = new Vector3f();
    private Vector3f working = new Vector3f();
    private Vector3f external;
    private MovementState movement = new MovementState();
    private MovementState workingMovement = new MovementState();
    protected float rotationSpeed;
    protected Vector3f initialUpVec = new Vector3f(0.0F, 1.0F, 0.0F);

    protected boolean walkOnWater = false;
    protected boolean headInWater = false;

    protected Vector3f moveVector = new Vector3f();
    protected Vector3f leftVector = new Vector3f();
    protected World world;
    protected WorldDatabase worldDb;
    protected GameClient gameClient;
    protected TimeBuffer moveHistory = new TimeBuffer(4);

    protected MaskStrategy maskStrat = MaskStrategies.ALL;

    protected CameraTask(World world, GameClient gameClient) {
        this.gameClient = gameClient;
        this.world = world;
        this.worldDb = world.getWorldDatabase();
    }

    public void initialize() {
    }

    public void setCollisionMaskStrategy(MaskStrategy strat) {
        this.maskStrat = strat;
    }

    public void setDirection(Quaternion rotation) {
        this.moveVector.set(rotation.getRotationColumn(2));
        this.leftVector.set(rotation.getRotationColumn(0));
        this.movement.setFacing(rotation);
    }

    public void setMoveState(byte flags) {
        this.movement.setMovementFlags(flags);
    }

    public MovementState getMoveState() {
        return this.movement;
    }

    protected CollisionMesh createPersonMesh() {
        if (!BlockTypeIndex.isInitialized()) {
            throw new RuntimeException("Block types have not been initialized.");
        }
        BlueprintData bp = new BlueprintData();
        bp.id = -1L;
        bp.name = "Person";

        bp.xSize = 1;
        bp.ySize = 1;
        bp.zSize = 5;
        bp.scale = 0.4F;
        bp.cells = new int[bp.xSize][bp.ySize][bp.zSize];

        for (int i = 0; i < bp.xSize; i++) {
            for (int j = 0; j < bp.ySize; j++) {
                for (int k = 0; k < bp.zSize; k++) {
                    bp.cells[i][j][k] = 1;
                }

            }

        }

        CollisionMesh cm = new CollisionMesh(null, bp, new Vec3d(0.2D, 0.0D, 0.2D));

        return cm;
    }

    public void setPosition(float x, float y, float z) {
        this.external = new Vector3f(x, y, z);
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setWorldPosition(float x, float y, float z) {
        setPosition(x, z, y);
    }

    public Vector3f getWorldPosition(long time) {
        long t = time;

        FrameTransition ft = this.moveHistory.getFrame(t);
        if (ft == null) {
            System.out.println("**** Time buffer accessed while uninitialized. ***");
            return new Vector3f(this.position.x, this.position.z, this.position.y);
        }
        Vector3f pos = ft.getPosition(t, true);

        float flip = pos.y;
        pos.y = pos.z;
        pos.z = flip;
        return pos;
    }

    public Quaternion getFacing(long time) {
        long t = time;

        FrameTransition ft = this.moveHistory.getFrame(t);
        if (ft == null) {
            System.out.println("**** Time buffer accessed while uninitialized. ***");
            return Quaternion.DIRECTION_Z;
        }
        Quaternion rot = ft.getRotation(t, true);

        return rot;
    }

    public Vector3f getVelocity(long time) {
        long t = time;

        FrameTransition ft = this.moveHistory.getFrame(t);
        if (ft == null) {
            System.out.println("**** Time buffer accessed while uninitialized. ***");
            return new Vector3f(this.position.x, this.position.z, this.position.y);
        }

        Vector3f vel = ft.getFrameVelocity();

        float flip = vel.y;
        vel.y = vel.z;
        vel.z = flip;
        return vel;
    }

    public boolean isHeadInWater() {
        return this.headInWater;
    }

    protected int getBlockType(float x, float y, float z) {
        return this.worldDb.getCellType(Coordinates.worldToCell(x), Coordinates.worldToCell(y), Coordinates.worldToCell(z));
    }

    protected void swapWorking() {
        if (this.external != null) {
            this.position = this.external.clone();
            this.working = this.external;
            this.external = null;
        } else {
            this.position = this.working;
            this.working = this.position.clone();
        }
    }

    protected boolean isOn(byte type) {
        return this.workingMovement.isOn(type);
    }

    protected abstract void doMovement(PhysicsThread paramPhysicsThread, Vector3f paramVector3f, double paramDouble);

    protected boolean isMoving() {
        return this.workingMovement.isMoving();
    }

    public boolean updatePhysics(PhysicsThread anim, double seconds) {
        this.workingMovement = new MovementState(this.movement);

        if (this.external != null) {
            swapWorking();
        }

        long time = this.gameClient.getRawTime();

        if (isMoving()) {
            doMovement(anim, this.working, seconds * 0.25D);
            swapWorking();
            doMovement(anim, this.working, seconds * 0.25D);
            swapWorking();
            doMovement(anim, this.working, seconds * 0.25D);
            swapWorking();
            doMovement(anim, this.working, seconds * 0.25D);
            swapWorking();
        }

        this.moveHistory.addFrame(time, this.position, this.movement.getFacing());

        this.gameClient.getPlayerState().setRunning(this.movement.isOn((byte) -128));

        return true;
    }

    public static class WalkCameraTask extends CameraTask {
        private float baseGravity = 20.0F;
        private float baseFallSpeed = 0.0F;

        private float movementSpeed = 3.0F;
        private float moveAcceleration = 25.0F;
        private float moveDeceleration = 15.0F;
        private float speedForward = 0.0F;
        private float speedSide = 0.0F;

        private float fallAcceleration = this.baseGravity;
        private float fallSpeed = 0.0F;
        private float terminalVelocity = 40.0F;
        private double period = 0.0D;
        private double periodSpeed = 0.0D;
        private double periodAcceleration = Math.toRadians(600.0D);
        private double maxPeriodSpeed = Math.toRadians(720.0D);
        private double yBobScale = 0.02999999932944775D;
        private double xBobScale = 0.03999999910593033D;
        private double yBob = 0.0D;
        private double xBob = 0.0D;
        private float headHeight = 1.7F;
        private Vector3f location = new Vector3f();
        private float epsilon = 1.0E-004F;
        private CollisionMesh collisionMesh;
        private float jumpImpulse = 12.0F;
        private float jumpDecay = 40.0F;
        private float jumpVelocity = this.jumpImpulse;
        private boolean jumping = false;
        private boolean jumpReady = true;
        private boolean swimming = false;
        private boolean allInWater = false;

        private float autoClimbSpeed = 3.0F;

        private float correctionSpeed = 2.5F;

        public WalkCameraTask(World world, GameClient gameClient) {
            super(world, gameClient);
        }

        public void initialize() {
            this.collisionMesh = createPersonMesh();
        }

        public void setPosition(float x, float y, float z) {
            this.location.set(x, y - this.headHeight, z);
            super.setPosition(x, y, z);
        }

        private void move(float value, Vector3f working) {
            Vector3f vel = this.moveVector.clone();
            float y = vel.y;
            if (!this.allInWater) {
                vel.y = 0.0F;
            }

            vel.multLocal(value * this.speedForward);
            working.addLocal(vel);
        }

        private void strafe(float value, Vector3f working) {
            Vector3f vel = this.leftVector.clone();
            vel.multLocal(value * this.speedSide);
            working.addLocal(vel);
        }

        protected boolean isMoving() {
            return true;
        }

        protected void doMovement(PhysicsThread anim, Vector3f working, double seconds) {
            if (seconds > 1.0D) {
                return;
            }
            float value = (float) seconds;

            Vector3f pos = this.location.clone();

            this.movementSpeed = (isOn((byte) -128) ? 4.5F : 3.0F);

            if (isOn((byte) 1))
                this.speedForward = Math.min(this.movementSpeed, this.speedForward + this.moveAcceleration * value);
            if (isOn((byte) 2))
                this.speedForward = Math.max(-this.movementSpeed, this.speedForward + this.moveAcceleration * -value);
            if (isOn((byte) 8))
                this.speedSide = Math.max(-this.movementSpeed, this.speedSide + this.moveAcceleration * -value);
            if (isOn((byte) 4)) {
                this.speedSide = Math.min(this.movementSpeed, this.speedSide + this.moveAcceleration * value);
            }
            if (value * this.speedForward > 1.0F)
                System.out.println("************* YIKES THAT'S MOVING FAST ***********  seconds:" + seconds);
            move(value, pos);
            strafe(value, pos);

            int typeAtFeet = getBlockType(pos.x, pos.z, pos.y);
            int typeAtHead = getBlockType(pos.x, pos.z, pos.y + this.headHeight);
            boolean inWater = false;
            boolean oldHeadInWater = this.headInWater;
            if ((!this.walkOnWater) && ((typeAtFeet == 7) || (typeAtFeet == 8))) {
                if (typeAtHead == 7) {
                    this.headInWater = true;
                    inWater = true;
                } else if (typeAtHead == 8) {
                    inWater = true;

                    float yHead = pos.y + this.headHeight;
                    float yHeadDelta = yHead - (float) Math.floor(yHead);

                    this.headInWater = (yHeadDelta < 0.89F);
                } else {
                    this.headInWater = false;
                }

            } else {
                this.headInWater = ((typeAtHead == 7) || (typeAtHead == 8));
            }

            if (this.headInWater) {
                if (!this.swimming)
                    this.fallAcceleration = 0.5F;
                this.terminalVelocity = 2.0F;
            } else if (inWater) {
                if (!this.swimming)
                    this.fallAcceleration = 2.0F;
                this.terminalVelocity = 10.0F;
            } else {
                this.fallAcceleration = this.baseGravity;
                this.terminalVelocity = 40.0F;
            }

            this.allInWater = ((this.headInWater) && (inWater));

            if (isOn((byte) 64)) {
                if (this.jumpReady) {
                    this.jumping = true;
                    this.jumpVelocity = Math.max(0.0F, this.jumpVelocity - this.jumpDecay * value);
                    pos.y += this.jumpVelocity * value;
                    this.fallSpeed = 0.0F;
                    if (this.jumpVelocity == 0.0F) {
                        this.jumpReady = false;
                        this.jumping = false;
                    }

                }

            } else if (this.jumping) {
                this.jumpVelocity = 0.0F;
                pos.y += this.jumpVelocity * value;
                this.fallSpeed = 0.0F;
                if (this.jumpVelocity == 0.0F) {
                    this.jumpReady = false;
                    this.jumping = false;
                }

            }

            this.collisionMesh.position.set(pos.x, pos.y, pos.z);

            long start = System.nanoTime();
            List<Contact> contacts = this.gameClient.getCollisions().getCollisions(this.collisionMesh, this.maskStrat, new ArrayList<Contact>());

            boolean fallStopped = false;
            boolean canJumpIfNotFalling = true;

            Vec3d min = new Vec3d();
            Vec3d max = new Vec3d();

            double highestPushback = 0.0D;
            Vector3f moveVec = pos.subtract(this.location);

            for (Contact c : contacts) {
                Vec3d adjust = c.contactNormal.mult(c.penetration);
                Vec3d normal = c.contactNormal;
                boolean up;
                if (c.mesh1 != this.collisionMesh) {
                    adjust.multLocal(-1.0D);
                    normal = normal.mult(-1.0D);
                    up = c.contactNormal.y < 0.0D;
                } else {
                    up = c.contactNormal.y > 0.0D;
                }

                Vector3f temp = new Vector3f((float) normal.x, (float) normal.y, (float) normal.z);
                float dot = moveVec.dot(temp);

                if (dot < -0.01D) {
                    if (c.contactPoint.y > highestPushback) {
                        highestPushback = c.contactPoint.y;
                    }
                }
                min.x = (min.x < adjust.x ? min.x : adjust.x);
                min.y = (min.y < adjust.y ? min.y : adjust.y);
                min.z = (min.z < adjust.z ? min.z : adjust.z);

                max.x = (max.x > adjust.x ? max.x : adjust.x);
                max.y = (max.y > adjust.y ? max.y : adjust.y);
                max.z = (max.z > adjust.z ? max.z : adjust.z);

                if (up) {
                    fallStopped = true;
                }
            }
            Vec3d posd = new Vec3d(pos.x, pos.y, pos.z);

            if (highestPushback > 0.0D) {
                highestPushback -= pos.y;

                if (highestPushback < 0.6D) {
                    fallStopped = true;
                    posd.y += seconds * this.autoClimbSpeed;
                } else if (highestPushback < 1.0D) {
                    fallStopped = true;
                    posd.y += seconds * this.autoClimbSpeed * 0.5D;
                    canJumpIfNotFalling = false;
                } else if (highestPushback < 1.5D) {
                    fallStopped = true;
                    posd.y += seconds * this.autoClimbSpeed * 0.2D;
                    canJumpIfNotFalling = false;
                } else if (highestPushback < 1.71D) {
                    fallStopped = true;
                    posd.y += seconds * this.autoClimbSpeed * 0.1D;
                    canJumpIfNotFalling = false;
                }
            }

            posd.addLocal(min);
            posd.addLocal(max);

            pos.set((float) posd.x, (float) posd.y, (float) posd.z);

            long end2 = System.nanoTime();

            if (end2 - start > 10000000L) {
                System.out.println("Total contact resolution time:" + (end2 - start / 1000000.0D) + " ms");
            }
            if (fallStopped) {
                this.fallSpeed = 0.0F;
                if (canJumpIfNotFalling) {
                    this.jumpReady = true;
                    this.jumpVelocity = this.jumpImpulse;
                }
            } else {
                this.fallSpeed = Math.max(-this.terminalVelocity, this.fallSpeed - this.fallAcceleration * value);
                if (!this.jumping) {
                    this.jumpReady = false;
                }

            }

            if (this.speedForward < 0.0F)
                this.speedForward = Math.min(0.0F, this.speedForward + this.moveDeceleration * (float) seconds);
            else {
                this.speedForward = Math.max(0.0F, this.speedForward - this.moveDeceleration * (float) seconds);
            }
            if (this.speedSide < 0.0F)
                this.speedSide = Math.min(0.0F, this.speedSide + this.moveDeceleration * (float) seconds);
            else {
                this.speedSide = Math.max(0.0F, this.speedSide - this.moveDeceleration * (float) seconds);
            }

            if ((isOn((byte) 64)) && (inWater)) {
                this.swimming = true;
                if ((this.fallSpeed < 0.0F) && (this.fallSpeed > -1.5D))
                    this.fallSpeed = 0.0F;
                this.fallAcceleration = -4.0F;
            } else if (this.swimming) {
                this.swimming = false;
                if (inWater) {
                    if (this.fallSpeed > 0.0F) {
                        this.fallSpeed = 0.0F;
                    }
                }
            }

            if (this.fallSpeed * value > 1.0F) {
                System.out.println("************* YIKES THAT'S FALLING FAST ***********  seconds:" + seconds);
            }
            pos.y += this.fallSpeed * value;

            if (this.speedForward != 0.0F) {
                this.periodSpeed = Math.min(this.maxPeriodSpeed, this.periodSpeed + this.periodAcceleration * value);
            } else {
                this.periodSpeed = 0.0D;

                this.periodSpeed = 0.0D;

                float xInterpSpeed = Math.max(0.1F, (float) Math.abs(this.xBob)) * 2.0F;
                float yInterpSpeed = Math.max(0.1F, (float) Math.abs(this.yBob)) * 2.0F;
                this.period = 0.0D;
                if (this.yBob < 0.0D)
                    this.yBob = Math.min(this.yBob + yInterpSpeed * value, 0.0D);
                else if (this.yBob > 0.0D)
                    this.yBob = Math.max(this.yBob - yInterpSpeed * value, 0.0D);
                if (this.xBob < 0.0D)
                    this.xBob = Math.min(this.xBob + xInterpSpeed * value, 0.0D);
                else if (this.xBob > 0.0D) {
                    this.xBob = Math.max(this.xBob - xInterpSpeed * value, 0.0D);
                }
            }
            this.period += this.periodSpeed * value;

            Vector3f bobOffset = new Vector3f();

            if (this.period != 0.0D) {
                if (this.period >= 12.566370964050293D) {
                    this.period -= 12.566370964050293D;
                }
                this.yBob = (Math.sin(this.period) * this.yBobScale);
                this.xBob = (Math.cos(this.period * 0.5D + Math.toRadians(90.0D)) * this.xBobScale);
            }

            if ((this.xBob != 0.0D) || (this.yBob != 0.0D)) {
                bobOffset.set(this.leftVector);
                bobOffset.multLocal((float) this.xBob);
                bobOffset.y += (float) this.yBob;
            }

            if (pos.y < 0.0F) {
                pos.y = 0.0F;
            }
            this.location.set(pos.x, pos.y, pos.z);
            working.set(pos.x + bobOffset.x, pos.y + this.headHeight + bobOffset.y, pos.z + bobOffset.z);
        }
    }

    public static class HoverCameraTask extends CameraTask {
        private float movementSpeed = 10.0F;

        public HoverCameraTask(World world, GameClient gameClient) {
            super(world, gameClient);
        }

        private void elevate(float value, Vector3f working) {
            Vector3f vel = new Vector3f(0.0F, value * this.movementSpeed, 0.0F);
            working.addLocal(vel);
        }

        private void move(float value, Vector3f working) {
            Vector3f vel = this.moveVector.clone();
            vel.multLocal(value * this.movementSpeed);
            working.addLocal(vel);
        }

        private void strafe(float value, Vector3f working) {
            Vector3f vel = this.leftVector.clone();

            vel.multLocal(value * this.movementSpeed);
            working.addLocal(vel);
        }

        protected boolean isMoving() {
            return true;
        }

        protected void doMovement(PhysicsThread anim, Vector3f working, double seconds) {
            if (isOn((byte) 1))
                move((float) seconds, working);
            if (isOn((byte) 2))
                move(-(float) seconds, working);
            if (isOn((byte) 16))
                elevate((float) seconds, working);
            if (isOn((byte) 32))
                elevate(-(float) seconds, working);
            if (isOn((byte) 4))
                strafe((float) seconds, working);
            if (isOn((byte) 8))
                strafe(-(float) seconds, working);
        }
    }
}