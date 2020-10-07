package map;

import java.awt.*;

/**
 * @author Wilson Thurman Teng
 */

public class Cell {
    private Point coord;

    private boolean virtualWall;
    private boolean moveThru;

    private boolean obstacle;
    private boolean explored;

    private boolean path;
    private boolean isWayPoint;

    public Cell(Point coord) {
        this.coord = coord;
        this.explored = false; this.moveThru = false;
        this.virtualWall = false; this.obstacle = false;
    }

    // Methods
    public boolean isExplorableCell() {
        return !obstacle && !virtualWall;
    }
    public boolean isMovableCell() {
        return explored && this.isExplorableCell();
    }

    // Getters & Setters
    public boolean isExplored() {
        return explored;
    }
    public void setExplored(boolean explored) {
        this.explored = explored;
    }
    public boolean isObstacle() {
        return obstacle;
    }
    public void setObstacle(boolean obstacle) {
        this.obstacle = obstacle;
    }
    public Point getCoord() {
        return coord;
    }
    public int getX() { return (int)coord.getX(); } // return coord[0] in integer
    public int getY() { return (int)coord.getY(); } // return coord[1] in integer
    public int getRow() { return getY(); }
    public int getCol() { return getX(); }
    public void setCoord(Point coord) {
        this.coord = coord;
    }
    public boolean isPath() {
        return path;
    }
    public void setPath(boolean path) {
        this.path = path;
    }
    public boolean isMoveThru() {
        return moveThru;
    }
    public void setMoveThru(boolean moveThru) {
        this.moveThru = moveThru;
    }
    public boolean isVirtualWall() {
        return virtualWall;
    }
    public void setVirtualWall(boolean virtualWall) {
        this.virtualWall = virtualWall;
    }
    public boolean isWayPoint() {
        return isWayPoint;
    }
    public boolean setWayPoint(boolean isWayPoint) {
        if (isMovableCell()) {
            this.isWayPoint = isWayPoint;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cell@[" + coord + "], explored=" + explored + ", obstacle=" + obstacle + ", virtualWall=" + virtualWall
                + ", isWayPoint=" + isWayPoint + ", moveThru=" + moveThru + ", path=" + path + "]";
    }
}
