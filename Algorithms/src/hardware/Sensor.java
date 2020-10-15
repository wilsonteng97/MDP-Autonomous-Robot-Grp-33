package hardware;

import map.Map;

import java.awt.*;

/**
 * @author Wilson Thurman Teng
 */

public class Sensor {
    private String id;
    private final int lowerLimit; private final int upperLimit;
    private double prevData; private double prevRawData;

    private Point SensorBoardPos; // Sensor position on the Acrylic Board
    private AgentSettings.Direction sensorDir;

    public Sensor(String id, int lowerLimit, int upperLimit, int row, int col, AgentSettings.Direction sensorDir) {
        this.id = id;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.SensorBoardPos = new Point(col, row);
        this.sensorDir = sensorDir;
        this.prevData = 9;
        this.prevRawData = 99;
    }

    public void setSensor(int row, int col, AgentSettings.Direction dir) {
        this.SensorBoardPos = new Point(col, row);
        this.sensorDir = dir;
    }

    /**
     * Getters & Setters
     */
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getLowerLimit() {
        return lowerLimit;
    }
//    public void setMinRange(int minRange) {
//        this.minRange = minRange;
//    }
    public int getUpperLimit() {
        return upperLimit;
    }
//    public void setMaxRange(int maxRange) {
//        this.maxRange = maxRange;
//    }
    public Point getSensorBoardPos() {
        return SensorBoardPos;
    }
    public void setSensorBoardPos(int col, int row) {
        this.SensorBoardPos.setLocation(col, row);
    }
    public int getBoardY() {
        return SensorBoardPos.y;
    }
    public int getBoardX() {
        return SensorBoardPos.x;
    }
    public AgentSettings.Direction getSensorDir() {
        return sensorDir;
    }
    public void setSensorDir(AgentSettings.Direction sensorDir) {
        this.sensorDir = sensorDir;
    }
    public double getPrevData() {
        return prevData;
    }
    public void setPrevData(double prevData) {
        this.prevData = prevData;
    }
    public double getPrevRawData() {
        return prevRawData;
    }
    public void setPrevRawData(double prevRawData) {
        this.prevRawData = prevRawData;
    }

    /**
     * Simulation detection Methods
     */
    public int simDetect(Map explorationMap, Map simMap) {
        // range in measured in blks
        switch (sensorDir) {
            case NORTH:
                return checkSimMap(explorationMap, simMap, 1, 0);
            case EAST:
                return checkSimMap(explorationMap, simMap, 0, 1);
            case SOUTH:
                return checkSimMap(explorationMap, simMap, -1, 0);
            case WEST:
                return checkSimMap(explorationMap, simMap, 0, -1);
        }
        return -1;
    }
    public int checkSimMap(Map explorationMap, Map simMap, int rowDisplacement, int colDisplacement) {
        // Check if starting point is valid for sensors with lowerRange > 1.
        if (lowerLimit > 1) {
            for (int i = 1; i < this.lowerLimit; i++) {
                int row = this.getBoardY() + (rowDisplacement * i);
                int col = this.getBoardX() + (colDisplacement * i);

                if (!explorationMap.checkValidCell(row, col) || simMap.getCell(row, col).isObstacle()) {
                    return i;
                }
            }
        }

        // If anything is detected by sensor, return range
//        System.out.println("Lower : " + this.lowerLimit + " | Upper : " + this.upperLimit);
        for (int i = this.lowerLimit; i <= this.upperLimit; i++) {
            int row = this.getBoardY() + (rowDisplacement * i);
            int col = this.getBoardX() + (colDisplacement * i);
            if (!explorationMap.checkValidCell(row, col)) {
                return i;
            }
            if (simMap.getCell(row, col).isObstacle()) {
                return i;
            }
        }
        // When there are no obstacles within range, return -1.
        return -1;
    }

    /**
     * Real detection Methods
     */
    public void realDetect(Map explorationMap, int sensorVal) {
        switch (sensorDir) {
            case NORTH:
                detectObstacle(explorationMap, sensorVal, 1, 0); break;
            case EAST:
                detectObstacle(explorationMap, sensorVal, 0, 1); break;
            case SOUTH:
                detectObstacle(explorationMap, sensorVal, -1, 0); break;
            case WEST:
                detectObstacle(explorationMap, sensorVal, 0, -1); break;
        }
    }

    public void detectObstacle(Map explorationMap, int sensorVal, int rowDispl, int colDispl) {
        if (sensorVal == 0) return;  // return value for LR sensor if obstacle before lowerRange

        // If above fails, check if starting point is valid for sensors with lowerRange > 1.
        for (int i = 1; i < this.lowerLimit; i++) {
            int row = this.getBoardY() + (rowDispl * i);
            int col = this.getBoardX() + (colDispl * i);

            if (!explorationMap.checkValidCell(row, col)) return;
            if (explorationMap.getCell(row, col).isObstacle()) return;
        }

        // Update Map according to sensor value
        for (int i = this.lowerLimit; i <= this.upperLimit; i++) {
            int row = this.getBoardY() + (rowDispl * i);
            int col = this.getBoardX() + (colDispl * i);
            if (!explorationMap.checkValidCell(row, col)) {
//                System.out.println("[DEBUG] Cell[" + col + ", " + row + "]" + "is not valid");
                return;
            }

//            System.out.println("[!SETEXPLORED@" + i + "] Cell[" + col + ", " + row + "]");
            explorationMap.setVirtualWallIfBorder(row, col);
            explorationMap.getCell(row, col).setExplored(true);
            if (sensorVal == i) {
//                System.out.println("[!SETOBSTACLE@" + i + "] Cell[" + col + ", " + row + "]");
                explorationMap.getCell(row, col).setObstacle(true);
                explorationMap.createVirtualWalls(row, col);
//                System.out.println("Cell is virtualwall @[" + (row+1) + ", " + col + "]=" + explorationMap.getCell(row+1, col).isVirtualWall());
                return;
            }
            // Override previous obstacle value if front and right sensors detect no obstacle.
            // Override previous obstacle value if long-range sensor detects no obstacle in distance 5
            if (explorationMap.getCell(row, col).isObstacle()) {
                if (id.equals("SR1") || id.equals("SR2") || id.equals("SR3")) {
                    explorationMap.resetVirtualWalls(row, col);
                    explorationMap.setVirtualWallIfBorder(row, col);    // make sure never reset border
                } else if (id.equals("LR1") && (i == 5)) {
                    explorationMap.resetVirtualWalls(row, col);
                    explorationMap.setVirtualWallIfBorder(row, col);    // make sure never reset border
                } else {
                    explorationMap.setVirtualWallIfBorder(row, col);    // make sure never reset border
                    break;
                }
            }
        }
    }
}
