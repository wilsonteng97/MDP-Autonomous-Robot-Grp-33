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
        for (int i = this.lowerLimit; i <= this.upperLimit; i++) {
            int row = this.getBoardY() + (rowDisplacement * i);
            int col = this.getBoardX() + (colDisplacement * i);

            if (!explorationMap.checkValidCell(row, col)) {
                return i;
            }
            explorationMap.getCell(row, col).setExplored(true);
            if (simMap.getCell(row, col).isObstacle()) {
                explorationMap.getCell(row, col).setObstacle(true);
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
                detectObstacle(explorationMap, sensorVal, 1, 0);
            case EAST:
                detectObstacle(explorationMap, sensorVal, 0, 1);
            case SOUTH:
                detectObstacle(explorationMap, sensorVal, -1, 0);
            case WEST:
                detectObstacle(explorationMap, sensorVal, 0, -1);
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

        // Update map according to sensor's value.
        for (int i = this.lowerLimit; i <= this.upperLimit; i++) {
            int row = this.getBoardY() + (rowDispl * i);
            int col = this.getBoardX() + (colDispl * i);

            if (!explorationMap.checkValidCell(row, col)) continue;

            explorationMap.getCell(row, col).setExplored(true);

            if (sensorVal == i) {
                explorationMap.createVirtualWalls(row, col);
                break;
            }

            // Override previous obstacle value if front sensors detect no obstacle.
            if (explorationMap.getCell(row, col).isObstacle()) {
                if (id.equals("SR1") || id.equals("SR2") || id.equals("SR3")) {
                    explorationMap.resetVirtualWalls(row, col);
                } else {
                    break;
                }
            }
        }
    }
}
