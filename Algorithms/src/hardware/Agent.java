package hardware;

import map.Map;

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

    private ArrayList<Sensor> sensorLst;
    private final Sensor SR1;     // SRFrontLeft
    private final Sensor SR2;     // SRFrontCenter
    private final Sensor SR3;     // SRFrontRight
    private final Sensor LR1;     // LRLeftTop
    private final Sensor SR4;     // SRRightTop
    private final Sensor SR5;     // SRRightBtm

    private boolean sim;

    public Agent(int centreY, int centreX, AgentSettings.Direction agtDir, boolean sim) {
        this.ctrY = centreY; this.ctrX = centreX; this.agtDir = agtDir;
        this.enteredGoal = false;
        this.setSim(sim);

        sensorLst = new ArrayList<Sensor>();

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

        // Add sensors to SensorLst
        sensorLst.add(SR1); sensorLst.add(SR2); sensorLst.add(SR3);
        sensorLst.add(LR1);
        sensorLst.add(SR4); sensorLst.add(SR5);
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
    public void setAgtCtrCoord(int row, int col) {
        int xDispl = ctrX - col; int yDispl = ctrY - row;
        this.ctrY = row; this.ctrX = col;
        for (Sensor s : sensorLst) {
            s.setSensorBoardPos(s.getBoardX() + xDispl, s.getBoardY() + yDispl);
        }

    }
    public void setAgtCtrCoord(Point newCentrePt) {
        int xDispl = ctrX - newCentrePt.x; int yDispl = ctrY - newCentrePt.y;
        this.ctrY = newCentrePt.y; this.ctrX = newCentrePt.x;
        for (Sensor s : sensorLst) {
            s.setSensorBoardPos(s.getBoardX() + xDispl, s.getBoardY() + yDispl);
        }

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

    /**
     * Flexible agent action Methods
     */
    public void takeAction(AgentSettings.Actions action) {
        switch (action) {
            case END_EXP:
            case END_FAST:
                endTask(action); break;

            case START_EXP:
            case START_FAST:
                startTask(action); break;

            case FACE_LEFT:
            case FACE_RIGHT:
            case FACE_REVERSE:
                changeDir(action); break;

            case ALIGN_FRONT:
            case ALIGN_RIGHT:
                calibrate(action); break;

            case ERROR:
            default:
                break;
        }
    }

    public void takeAction(AgentSettings.Actions action, int steps, Map explorationMap) {
        switch (action) {
            case FORWARD:
            case BACKWARD:
            case MOVE_LEFT:
            case MOVE_RIGHT:
                move(action, steps, explorationMap); break;

            case ERROR:
            default:
                break;
        }
    }

    /**
     * Agent action component Methods
     */
    public void endTask(AgentSettings.Actions action) {
        switch (action) {
            case END_EXP:
                break;
            case END_FAST:
                break;
        }
    }
    public void startTask(AgentSettings.Actions action) {
        switch (action) {
            case START_EXP:
                break;
            case START_FAST:
                break;
        }
    }
    public void changeDir(AgentSettings.Actions action) {
        switch (action) {
            case FACE_LEFT:
                this.agtDir = AgentSettings.Direction.antiClockwise90(agtDir); break;
            case FACE_RIGHT:
                this.agtDir = AgentSettings.Direction.clockwise90(agtDir); break;
            case FACE_REVERSE:
                this.agtDir = AgentSettings.Direction.reverse(agtDir); break;
        }
    }
    public void calibrate(AgentSettings.Actions action) {
        switch(action) {
            case ALIGN_FRONT:
            case ALIGN_RIGHT:
                break;
        }
    }

    public void move(AgentSettings.Actions action, int steps, Map explorationMap) {
        switch (action) {
            case FORWARD:
                break;
            case BACKWARD:
                break;
            case MOVE_LEFT:
                break;
            case MOVE_RIGHT:
                break;
        }
    }

    /**
     * Agent environment sensing Method
     * (with the help of sensors)
     */
    public void senseEnv(Map explorationMap, Map map) {
        int[] result = new int[sensorLst.size()];
        int sensorCount = 0;

        if (sim) {
            for (Sensor s : sensorLst) {
                result[sensorCount] = s.simDetect(explorationMap, map);
                sensorCount++;
            }
        } else {
            // Get Sensor readings from Network Manager.
        }

        sensorCount = 0;
        for (Sensor s : sensorLst) {
            s.realDetect(explorationMap, result[sensorCount]);
            sensorCount++;
        }

//        String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
//        comm.sendMsg(mapStrings[0] + " " + mapStrings[1], CommMgr.MAP_STRINGS);
    }

    /**
     * !FIXME For real run, Network interface with Android Methods
     */
    public void transmitAction(AgentSettings.Actions action) {
        takeAction(action);
    }
}
