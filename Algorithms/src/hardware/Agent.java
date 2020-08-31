package hardware;

import map.Map;
import map.MapSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Represents the Agent moving on the map.
 *
 * ============ Limitations ============
 * 5 Short Range IR Sensors, 2 Long Range IR Sensors, 2 Ultrasonic Sensors
 *
 * ========== Sensor Positions (Assuming robot faces NORTH) ==========
 *             ^     ^     ^
 *            SR1   SR2   SR3
 *   <<< LR1 [20]  [21]  [22] SR4 >
 *           [10]  [11]  [12]
 *           [00]  [01]  [02] SR5 >
 *
 * [01] represents position at 0 row, 1 col
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
    private final Sensor SR1;     // SR1
    private final Sensor SR2;     // SR2
    private final Sensor SR3;     // SR3
    private final Sensor LR1;     // LRLeftTop
    private final Sensor SR4;     // SRRightTop
    private final Sensor SR5;     // SRRightBtm

    private boolean sim;

    public Agent(int centreY, int centreX, boolean sim) {
        this.ctrY = centreY; this.ctrX = centreX; this.agtDir = AgentSettings.START_DIR;
        this.enteredGoal = false;
        this.speed = AgentSettings.SPEED;
        this.setSim(sim);

        sensorLst = new ArrayList<Sensor>();

        // 3 Front SR Sensors same direction (Initialized with respect to Agent's Direction)
        SR1 = new Sensor("SR1", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX - 1,
                this.agtDir);
        SR2 = new Sensor("SR2", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX,
                this.agtDir);
        SR3 = new Sensor("SR3", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX + 1,
                this.agtDir);

        // 1 Top Left LR Sensor
        LR1 = new Sensor("LR1", AgentSettings.LONG_MIN, AgentSettings.LONG_MAX, ctrY + 1, ctrX - 1,
                AgentSettings.Direction.antiClockwise90(agtDir));

        // 2 Right SR Sensors, 1 Top & 1 Bottom
        SR4 = new Sensor("SR4", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY + 1, ctrX + 1,
                AgentSettings.Direction.clockwise90(agtDir));
        SR5 = new Sensor("SR5", AgentSettings.SHORT_MIN, AgentSettings.SHORT_MAX, ctrY - 1, ctrX + 1,
                AgentSettings.Direction.clockwise90(agtDir));

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
    public int getAgtRow() { return ctrY; }
    public int getAgtCol() { return ctrX; }
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
    public void updateEnteredGoal() {
        if (this.getAgtRow() == MapSettings.GOAL_ROW && this.getAgtCol() == MapSettings.GOAL_COL)
            this.enteredGoal = true;
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
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public int getSpeed() {
        return this.speed;
    }


    /**
     * Initial Agent direction initialisation
     */
//    private AgentSettings.Direction referenceAgtDir(AgentSettings.Actions action) {
//        switch (action) {
//            case FACE_LEFT:
//                this.agtDir = AgentSettings.Direction.antiClockwise90(agtDir);
//            case FACE_RIGHT:
//                this.agtDir = AgentSettings.Direction.clockwise90(agtDir);
//            case FACE_REVERSE:
//                this.agtDir = AgentSettings.Direction.reverse(agtDir);
//        }
//        return agtDir;
//        if (action == AgentSettings.Actions.FACE_RIGHT) {
//            return AgentSettings.Direction.clockwise90(agtDir);
//        } else if (action == AgentSettings.Actions.FACE_LEFT) {
//            return AgentSettings.Direction.antiClockwise90(agtDir);
//        } else {
//            return agtDir;
//        }
//    }

    /**
     * Flexible agent action Methods
     * @return
     */
    public AgentSettings.Direction takeAction(AgentSettings.Actions action) {
        switch (action) {
            case END_EXP:
            case END_FAST:
                return endTask(action);

            case START_EXP:
            case START_FAST:
                return startTask(action);

            case ERROR:
            default:
                break;
        }
        return agtDir;
    }

    public AgentSettings.Direction takeAction(AgentSettings.Actions action, int steps, Map explorationMap, Map map) {
        switch (action) {
            case FORWARD:
            case BACKWARD:
            case MOVE_LEFT:
            case MOVE_RIGHT:
                agtDir = move(action, steps, explorationMap, map); break;

            case FACE_LEFT:
            case FACE_RIGHT:
            case FACE_REVERSE:
                agtDir = changeDir(action, explorationMap, map); break;

            case ALIGN_FRONT:
            case ALIGN_RIGHT:
                agtDir = calibrate(action, explorationMap, map); break;

            case ERROR:
            default:
                break;
        }
        return agtDir;
    }

    /**
     * Agent action component Methods
     * @returns original changed direction or original direction if it is unchanged.
     */
    // TODO
    public AgentSettings.Direction endTask(AgentSettings.Actions action) {
        switch (action) {
            case END_EXP:
                break;
            case END_FAST:
                break;
        }
        return agtDir;
    }

    // TODO
    public AgentSettings.Direction startTask(AgentSettings.Actions action) {
        switch (action) {
            case START_EXP:
                break;
            case START_FAST:
                break;
        }
        return agtDir;
    }

    // TODO
    public AgentSettings.Direction calibrate(AgentSettings.Actions action, Map explorationMap, Map map) {
        switch(action) {
            case ALIGN_FRONT:
            case ALIGN_RIGHT:
                break;
        }
        this.setSensors();
        this.senseEnv(explorationMap, map);
        return agtDir;
    }

    public AgentSettings.Direction changeDir(AgentSettings.Actions action, Map explorationMap, Map map) {
        switch (action) {
            case FACE_LEFT:
                this.agtDir = AgentSettings.Direction.antiClockwise90(agtDir); break;
            case FACE_RIGHT:
                this.agtDir = AgentSettings.Direction.clockwise90(agtDir); break;
            case FACE_REVERSE:
                this.agtDir = AgentSettings.Direction.reverse(agtDir); break;
        }
//        this.setSensors();
//        this.senseEnv(explorationMap, map);
        return agtDir;
    }

    // TODO MOVE_LEFT & MOVE_RIGHT
    public AgentSettings.Direction move(AgentSettings.Actions action, int steps, Map explorationMap, Map map) {
        System.out.printf("[Function executed] move(%s)\n", action);
        if (sim) {
            // Emulate real AgentSettings.Direction by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }

        switch (action) {
            case FORWARD:
                switch (agtDir) {
                    case NORTH:
                        ctrY += steps;
                        break;
                    case EAST:
                        ctrX += steps;
                        break;
                    case SOUTH:
                        ctrY -= steps;
                        break;
                    case WEST:
                        ctrX -= steps;
                        break;
                }
                break;
            case BACKWARD:
                switch (agtDir) {
                    case NORTH:
                        ctrY -= steps;
                        break;
                    case EAST:
                        ctrX -= steps;
                        break;
                    case SOUTH:
                        ctrY += steps;
                        break;
                    case WEST:
                        ctrX += steps;
                        break;
                }
                break;
            default:
                System.out.println("Error in Agent.move()!" + action + " " + agtDir);
                break;
        }
//        setSensors();
//        senseEnv(explorationMap, map);
//        System.out.println("br");

        // TODO real bot: send AgentSettings.Direction
        if (!sim) {}

        updateEnteredGoal();
        System.out.printf("[Function completed] move(%s)\n", action);
        return agtDir;
    }

    /**
     * Agent environment sensing Method
     * (with the help of sensors)
     */
    public int[] senseEnv(Map explorationMap, Map map) {
        System.out.println("[Function executed] senseEnv");
        int[] result = new int[sensorLst.size()];
        int sensorCount = 0;

        if (sim) {
            System.out.println(" -> Is simulator");
            for (Sensor s : sensorLst) {
                System.out.println(" -> 1st loop executed | " + s.getId());
                result[sensorCount] = s.simDetect(explorationMap, map);
                sensorCount++;
            }
        } else {
            // Get Sensor readings from Network Manager.
        }
        sensorCount = 0;
        for (Sensor s : sensorLst) {
            System.out.println(" -> 2nd loop executed | [" + s.getId() + "]");
            s.realDetect(explorationMap, result[sensorCount]);
            sensorCount++;
        }
        System.out.println("Sensor Readings -> " + result[0] + " | " +  + result[1] + " | " +  + result[2] + " | " +  + result[3] + " | " +  + result[4] + " | " +  + result[5] + " | ");

//        String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
//        comm.sendMsg(mapStrings[0] + " " + mapStrings[1], CommMgr.MAP_STRINGS);
        return result;
    }

    /**
     * Sets the sensors' position and direction values according to the robot's current position and direction.
     */
    public void setSensors() {
        AgentSettings.Direction dirAgtLeft = AgentSettings.Direction.antiClockwise90(agtDir);
        AgentSettings.Direction dirAgtRight = AgentSettings.Direction.clockwise90(agtDir);

        switch (agtDir) {
            case NORTH:
                SR1.setSensor(this.ctrY + 1, this.ctrX - 1, this.agtDir);
                SR2.setSensor(this.ctrY + 1, this.ctrX, this.agtDir);
                SR3.setSensor(this.ctrY + 1, this.ctrX + 1, this.agtDir);
                LR1.setSensor(this.ctrY + 1, this.ctrX - 1, dirAgtLeft);
                SR4.setSensor(this.ctrY + 1, this.ctrX + 1, dirAgtRight);
                SR5.setSensor(this.ctrY - 1, this.ctrX + 1, dirAgtRight);
                break;
            case EAST:
                SR1.setSensor(this.ctrY + 1, this.ctrX + 1, this.agtDir);
                SR2.setSensor(this.ctrY, this.ctrX + 1, this.agtDir);
                SR3.setSensor(this.ctrY - 1, this.ctrX + 1, this.agtDir);
                LR1.setSensor(this.ctrY + 1, this.ctrX + 1, dirAgtLeft);
                SR4.setSensor(this.ctrY - 1, this.ctrX + 1, dirAgtRight);
                SR5.setSensor(this.ctrY - 1, this.ctrX - 1, dirAgtRight);
                break;
            case SOUTH:
                SR1.setSensor(this.ctrY - 1, this.ctrX + 1, this.agtDir);
                SR2.setSensor(this.ctrY - 1, this.ctrX, this.agtDir);
                SR3.setSensor(this.ctrY - 1, this.ctrX - 1, this.agtDir);
                LR1.setSensor(this.ctrY - 1, this.ctrX + 1, dirAgtLeft);
                SR4.setSensor(this.ctrY - 1, this.ctrX - 1, dirAgtRight);
                SR5.setSensor(this.ctrY + 1, this.ctrX - 1, dirAgtRight);
                break;
            case WEST:
                SR1.setSensor(this.ctrY - 1, this.ctrX - 1, this.agtDir);
                SR2.setSensor(this.ctrY, this.ctrX - 1, this.agtDir);
                SR3.setSensor(this.ctrY + 1, this.ctrX - 1, this.agtDir);
                LR1.setSensor(this.ctrY - 1, this.ctrX - 1, dirAgtLeft);
                SR4.setSensor(this.ctrY + 1, this.ctrX - 1, dirAgtRight);
                SR5.setSensor(this.ctrY + 1, this.ctrX + 1, dirAgtRight);
                break;
        }
    }
    /**
     * !FIXME For real run, Network interface with Android Methods
     */
    public void transmitAction(AgentSettings.Actions action) {
        takeAction(action);
    }
}
