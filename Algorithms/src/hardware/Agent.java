package hardware;

import java.awt.*;
import java.util.ArrayList;

/**
 * Represents the Agent moving on the map.
 *
 * ============ Limitations ============
 * 5 Short Range IR Sensors, 2 Long Range IR Sensors, 2 Ultrasonic Sensors
 *
 * ========== Sensor Positions ==========
 *            ^   ^   ^
 *           SR1 SR2 SR3
 *   <<< LR1 [X] [X] [X] SR4 >
 *           [X] [X] [X]
 *           [X] [X] [X] SR5 >
 *
 * SR = Short Range Sensor, LR = Long Range Sensor, US = Ultrasonic Sensor
 *
 * @author Wilson Thurman Teng
 */


public class Agent {
    private int ctrY; private int ctrX;
    private int speed;
    private int detectCount;
    private boolean enteredGoal;
    private boolean rightDistAlign = false;
    private boolean frontDistAlign = false;
    private boolean fastPathMode = false;
    private AgentSettings.Direction agtDir;
    private AgentSettings.Actions prevAction;

    private final Sensor SR1;     // SRFrontLeft
    private final Sensor SR2;     // SRFrontCenter
    private final Sensor SR3;     // SRFrontRight
    private final Sensor LR1;     // LRLeftTop
    private final Sensor SR4;     // SRRightTop
    private final Sensor SR5;     // SRRightBtm

    private boolean sim;

    public Agent(int centreY, int centreX, AgentSettings.Direction agtDir, boolean sim) {
        this.ctrY = centreY; this.ctrX = centreX; this.agtDir = agtDir;
        this.setSim(sim);

        this.enteredGoal = false;

        // 3 Front SR Sensors same direction (Initialized with respect to Agent's Direction)
        SR1 = new Sensor("SR1", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX + 1,
                this.agtDir);
        SR2 = new Sensor("SR2", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX,
                this.agtDir);
        SR3 = new Sensor("SR3", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX - 1,
                this.agtDir);

        // 1 Top Left LR Sensor
        LR1 = new Sensor("LR1", AgentSettings.LONG_MIN, AgentSettings.LONG_MAX, ctrY + 1, ctrX - 1,
                referenceAgtDir(AgentSettings.Actions.FACE_LEFT));

        // 2 Right SR Sensors, 1 Top & 1 Bottom
        SR4 = new Sensor("SR4", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY - 1, ctrX + 1,
                referenceAgtDir(AgentSettings.Actions.FACE_RIGHT));
        SR5 = new Sensor("SR5", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX + 1,
                referenceAgtDir(AgentSettings.Actions.FACE_RIGHT));
    }

    /**
     * Getters & Setters
     */
    public int getAgtX() {
        return ctrX;
    }
    public int getAgtY() {
        return ctrY;
    }
    public void setAgtCtrCoord(Point centrePt) {
        this.ctrY = centrePt.y;
        this.ctrX = centrePt.x;
    }
    public AgentSettings.Direction getAgtDir() {
        return agtDir;
    }
    public void setAgtDir(AgentSettings.Direction agtDir) {
        this.agtDir = agtDir;
    }
    public boolean hasEnteredGoal() {
        return enteredGoal;
    }
    public void setEnteredGoal(boolean enteredGoal) {
        this.enteredGoal = enteredGoal;
    }
    public boolean isFastPathMode() {
        return fastPathMode;
    }
    public void setFastPathMode(boolean fastPathMode) {
        this.fastPathMode = fastPathMode;
    }
    public boolean isSim() {
        return sim;
    }
    public void setSim(boolean sim) {
        this.sim = sim;
    }

    /**
     * Change direction Methods
     */
    private AgentSettings.Direction referenceAgtDir(AgentSettings.Actions action) {
        if (action == AgentSettings.Actions.FACE_RIGHT) {
            return AgentSettings.Direction.clockwise90(agtDir);
        } else if (action == AgentSettings.Actions.FACE_LEFT) {
            return AgentSettings.Direction.antiClockwise90(agtDir);
        } else {
            return agtDir;
        }
    }
}
