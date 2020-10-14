package hardware;

import map.Cell;
import map.ObsSurface;
import map.Map;
import map.MapSettings;
import network.NetworkMgr;
import utils.SimulatorSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static hardware.AgentSettings.CAMERA_DIRECTION;
import static hardware.AgentSettings.Direction.*;

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
 * [RPI Camera Placement] :
 * Camera is overlooking the right side of the robot and has 3 grids-wide vision.
 *
 * @author Wilson Thurman Teng
 */


public class Agent {
    private int ctrY; private int ctrX;
    private int speed;
    private int turnSpeed;
    private int detectCount;
    private boolean enteredGoal;
    private boolean rightDistAlign = false;
    private boolean frontDistAlign = false;
    private boolean fastPathMode = false;
    private AgentSettings.Direction agtDir;
    private AgentSettings.Actions prevAction;

    private ArrayList<Sensor> sensorLst;
    private Sensor SR1;     // SR1
    private Sensor SR2;     // SR2
    private Sensor SR3;     // SR3
    private Sensor LR1;     // LRLeftTop
    private Sensor SR4;     // SRRightTop
    private Sensor SR5;     // SRRightBtm

    // for image taking
    private int imageCount = 0;
    private HashSet<String> imageHashSet = new HashSet<String>();
    private HashMap<String, ObsSurface> surfaceTaken = new HashMap<String, ObsSurface>();

    private boolean sim;

    public Agent(int centreY, int centreX, boolean sim) {
        this.ctrY = centreY; this.ctrX = centreX;
        this.agtDir = AgentSettings.START_DIR; this.enteredGoal = false;

        this.setSim(sim);
        this.initSpeed();
        this.initSensors();
    }

    /**
     * Agent Init Methods
     */
    public void initSpeed() {
        if (sim) {
            this.speed = AgentSettings.SPEED / SimulatorSettings.SIM_ACCELERATION;
            this.turnSpeed = AgentSettings.TURN_SPEED / SimulatorSettings.SIM_ACCELERATION;
        } else {
            this.speed = AgentSettings.SPEED;
            this.turnSpeed = AgentSettings.TURN_SPEED;
        }
    }

    public void initSensors() {
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
     * Reset Agent Method
     */
    public void resetAgt() {
        if (sim) {
            this.ctrY = MapSettings.START_ROW;
            this.ctrX = MapSettings.START_COL;
        }
        this.agtDir = AgentSettings.START_DIR; this.enteredGoal = false;

        this.initSpeed();
        this.initSensors();
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
    public Point getAgtPos() { return new Point(ctrX, ctrY); }
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
                agtDir = move(action, steps, explorationMap, map);
                break;

            case FACE_LEFT:
            case FACE_RIGHT:
            case FACE_REVERSE:
                agtDir = changeDir(action, explorationMap, map);
                break;

            case ALIGN_FRONT:
            case ALIGN_RIGHT:
            case CALIBRATE:
                agtDir = calibrate(action, explorationMap, map);
                break;

            case ERROR:
            default:
                break;
        }
        if (!sim) sendMovement(action);
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
            case CALIBRATE:
                break;
        }
//        this.setSensors();
//        this.senseEnv(explorationMap, map);
        return agtDir;
    }

    public AgentSettings.Direction changeDir(AgentSettings.Actions action, Map explorationMap, Map map) {
        if (sim) {
            // Emulate real AgentSettings.Direction by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(turnSpeed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.changeDir()!");
            }
        }

        switch (action) {
            case FACE_LEFT:
                this.agtDir = AgentSettings.Direction.antiClockwise90(agtDir); break;
            case FACE_RIGHT:
                this.agtDir = AgentSettings.Direction.clockwise90(agtDir); break;
            case FACE_REVERSE:
                this.agtDir = AgentSettings.Direction.reverse(agtDir); break;
        }
        return agtDir;
    }

    // TODO MOVE_LEFT & MOVE_RIGHT
    public AgentSettings.Direction move(AgentSettings.Actions action, int steps, Map explorationMap, Map map) {
//        System.out.printf("[DEBUG: Function executed] move(%s)\n", action);
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

        updateEnteredGoal();
        return agtDir;
    }

    /**
     * Agent environment sensing Method
     * (with the help of sensors)
     */
    public int[] senseEnv(Map explorationMap, Map map) {
        int[] result = new int[sensorLst.size()];
        int sensorCount = 0;

        if (sim) {
            for (Sensor s : sensorLst) {
                result[sensorCount] = s.simDetect(explorationMap, map);
                sensorCount++;
            }
        } else {
            // Get Sensor readings from Network Manager.
            NetworkMgr comm = NetworkMgr.getInstance();
            String msg = comm.receiveMsg();
            System.out.println(msg);
            String[] msgArr = msg.split("\\|");

            result[0] = Integer.parseInt(msgArr[0]);
            result[1] = Integer.parseInt(msgArr[1]);
            result[2] = Integer.parseInt(msgArr[2]);
            result[3] = Integer.parseInt(msgArr[3]);
            result[4] = Integer.parseInt(msgArr[4]);
            result[5] = Integer.parseInt(msgArr[5]);
        }
        sensorCount = 0;
        for (Sensor s : sensorLst) {
            s.realDetect(explorationMap, result[sensorCount]);
            sensorCount++;
        }
//        System.out.println("Sensor Readings -> " + result[0] + " | " +  + result[1] + " | " +  + result[2] + " | " +  + result[3] + " | " +  + result[4] + " | " +  + result[5] + " | ");

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
    private void sendMovement(AgentSettings.Actions m) {
        NetworkMgr comm = NetworkMgr.getInstance();
        comm.sendMsg(AgentSettings.Actions.print(m) + "", NetworkMgr.INSTRUCTIONS);
//        if (m != AgentSettings.Actions.CALIBRATE) {
//            comm.sendMsg(this.getAgtRow() + "," + this.getAgtCol() + "," + AgentSettings.Direction.print(this.getAgtDir()), NetworkMgr.BOT_POS);
//        }
    }


    /**
     * Image Recognition methods
     */
    public AgentSettings.Direction getCameraDirection() {
        switch (CAMERA_DIRECTION) {
            case EAST:
                return AgentSettings.Direction.clockwise90(this.agtDir);
            case WEST:
                return AgentSettings.Direction.antiClockwise90(this.agtDir);
            case NORTH:
                return this.agtDir;
            case SOUTH:
                return AgentSettings.Direction.reverse(this.agtDir);
        }
        return null;
    }

    public HashMap<String, ObsSurface> getSurfaceTaken() {
        return surfaceTaken;
    }

    public ArrayList<ObsSurface> returnSurfacesTakenRight(Map exploredMap) {
        ArrayList<ObsSurface> surfaceTakenList = new ArrayList<ObsSurface>();
        ObsSurface tempObsSurface;
        int rowInc = 0, colInc = 0;
        int camera_row, camera_col, temp_row, temp_col;
        AgentSettings.Direction obsDir = null;
        Cell tempCell;

        switch (agtDir) {
            case NORTH:
                rowInc = 0; colInc = 1; obsDir = EAST;
                break;
            case SOUTH:
                rowInc = 0; colInc = -1; obsDir = WEST;
                break;
            case WEST:
                rowInc = 1; colInc = 0; obsDir = NORTH;
                break;
            case EAST:
                rowInc = -1; colInc = 0; obsDir = SOUTH;
                break;
        }

        camera_row = getAgtY() + rowInc;
        camera_col = getAgtX() + colInc;

        boolean left = false; boolean mid = false; boolean right = false;

//        System.out.println("camera_row|camera_col " + camera_row + "|" + camera_col);
        for (int offset = AgentSettings.CAMERA_MIN; offset <= AgentSettings.CAMERA_MAX; offset++) {
//            System.out.println("offset| " + offset);
            temp_row = camera_row + rowInc * offset;
            temp_col = camera_col + colInc * offset;

//            System.out.println("temp_row|temp_col " + temp_row + "|"  + temp_col);
            if (exploredMap.checkValidCell(temp_row, temp_col)) {
                tempCell = exploredMap.getCell(temp_row, temp_col);
            } else {
//                System.out.println("Not Valid|temp_row|temp_col " + temp_row + "|"  + temp_col);
                break;
            }

            // Left/Right Obs
            if (rowInc==0) temp_row++; if (colInc==0) temp_col++;
            tempCell = exploredMap.getCell(temp_row, temp_col);
            if (tempCell.isObstacle() && !left && tempCell.isExplored() && exploredMap.checkValidCell(temp_row, temp_col)) {
                tempObsSurface = new ObsSurface(new Point(temp_col - colInc, temp_row - rowInc), obsDir);
//                    System.out.println("left tempObsSurface " + tempObsSurface);
                surfaceTaken.put(tempObsSurface.toString(), tempObsSurface);
                surfaceTakenList.add(tempObsSurface);
                left = true;

            }

            // middleObs
            if (rowInc==0) temp_row--; if (colInc==0) temp_col--;
            tempCell = exploredMap.getCell(temp_row, temp_col);
            if (tempCell.isObstacle() && !mid && tempCell.isExplored() && exploredMap.checkValidCell(temp_row, temp_col)) {
                tempObsSurface = new ObsSurface(new Point(temp_col - colInc, temp_row - rowInc), obsDir);
//                    System.out.println("mid tempObsSurface " + tempObsSurface);
                surfaceTaken.put(tempObsSurface.toString(), tempObsSurface);
                surfaceTakenList.add(tempObsSurface);
                mid = true;
            }

            // Left/Right obs
            if (rowInc==0) temp_row--; if (colInc==0) temp_col--;
            tempCell = exploredMap.getCell(temp_row, temp_col);
            if (tempCell.isObstacle() && !right && tempCell.isExplored() && exploredMap.checkValidCell(temp_row, temp_col)) {
                tempObsSurface = new ObsSurface(new Point(temp_col - colInc, temp_row - rowInc), obsDir);
//                    System.out.println("right tempObsSurface " + tempObsSurface);
                surfaceTaken.put(tempObsSurface.toString(), tempObsSurface);
                surfaceTakenList.add(tempObsSurface);
                right = true;
            }
        }

        System.out.println("surfaceTakenList" + surfaceTakenList);
        return surfaceTakenList;
    }

    public HashMap<String, Point> returnObsRight(Map exploredMap) {
        int rowInc = 0, colInc = 0;
        int camera_row, camera_col, temp_row, temp_col;
        HashMap<String, Point> obsList = new HashMap<String, Point>();
        AgentSettings.Direction obsDir = null;
        Cell tempCell;

        switch (agtDir) {
            case NORTH:
                rowInc = 0; colInc = 1; obsDir = EAST;
                break;
            case SOUTH:
                rowInc = 0; colInc = -1; obsDir = WEST;
                break;
            case WEST:
                rowInc = 1; colInc = 0; obsDir = NORTH;
                break;
            case EAST:
                rowInc = -1; colInc = 0; obsDir = SOUTH;
                break;
        }

        camera_row = getAgtY() + rowInc;
        camera_col = getAgtX() + colInc;

        boolean left = false; boolean mid = false; boolean right = false;

//        System.out.println("camera_row|camera_col " + camera_row + "|" + camera_col);
        for (int offset = AgentSettings.CAMERA_MIN; offset <= AgentSettings.CAMERA_MAX; offset++) {
//            System.out.println("offset| " + offset);
            temp_row = camera_row + rowInc * offset;
            temp_col = camera_col + colInc * offset;

//            System.out.println("temp_row|temp_col " + temp_row + "|"  + temp_col);
            if (exploredMap.checkValidCell(temp_row, temp_col)) {
                tempCell = exploredMap.getCell(temp_row, temp_col);
            } else {
//                System.out.println("Not Valid|temp_row|temp_col " + temp_row + "|"  + temp_col);
                break;
            }

            // Left/Right Obs
            if (rowInc==0) temp_row++; if (colInc==0) temp_col++;
            tempCell = exploredMap.getCell(temp_row, temp_col);
            if (tempCell.isObstacle() && !left && tempCell.isExplored() && exploredMap.checkValidCell(temp_row, temp_col)) {
                if ((obsDir == NORTH) || (obsDir == WEST)) obsList.put("R", new Point(temp_col, temp_row));
                if ((obsDir == SOUTH) || (obsDir == EAST)) obsList.put("L", new Point(temp_col, temp_row));
                left = true;
            }

            // middleObs
            if (rowInc==0) temp_row--; if (colInc==0) temp_col--;
            tempCell = exploredMap.getCell(temp_row, temp_col);
            if (tempCell.isObstacle() && !mid && tempCell.isExplored() && exploredMap.checkValidCell(temp_row, temp_col)) {
                obsList.put("M", new Point(temp_col, temp_row));
                mid = true;
            }

            // Left/Right obs
            if (rowInc==0) temp_row--; if (colInc==0) temp_col--;
            tempCell = exploredMap.getCell(temp_row, temp_col);
            if (tempCell.isObstacle() && !right && tempCell.isExplored() && exploredMap.checkValidCell(temp_row, temp_col)) {
                if ((obsDir == NORTH) || (obsDir == WEST)) obsList.put("L", new Point(temp_col, temp_row));
                if ((obsDir == SOUTH) || (obsDir == EAST)) obsList.put("R", new Point(temp_col, temp_row));
                right = true;
            }
        }

        System.out.println("obsList" + obsList + "\n");
        return obsList;
    }
}
