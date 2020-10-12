package map;

import hardware.AgentSettings;

import java.awt.*;

public class ObsSurface {
    private Point blkPos;
    private AgentSettings.Direction surface;

    public ObsSurface(int col, int row, AgentSettings.Direction surface) {
        this.blkPos = new Point(col, row);
        this.surface = surface;
    }


    public ObsSurface(Point blkPos, AgentSettings.Direction surface) {
        this.blkPos = blkPos;
        this.surface = surface;
    }


    public Point getPos() {
        return blkPos;
    }

    public int getRow() {
        return this.blkPos.y;
    }

    public int getCol() {
        return this.blkPos.x;
    }

    public AgentSettings.Direction getSurface() {
        return surface;
    }

    public void setPos(Point blkPos) {
        this.blkPos = blkPos;
    }

    public void setSurface(AgentSettings.Direction dir) {
        this.surface = dir;
    }

    public void setPos(int row, int col) {
        this.blkPos = new Point(col, row);
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%s", this.getRow(), this.getCol(), AgentSettings.Direction.print(this.surface)); // row|col|surface
    }
}
