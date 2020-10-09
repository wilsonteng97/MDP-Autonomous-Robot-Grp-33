package map;

import hardware.AgentSettings;

import java.awt.*;

public class BlkSurface {
    private Point blkPos;
    private AgentSettings.Direction surface;

    public BlkSurface(int row, int col, AgentSettings.Direction surface) {
        this.blkPos = new Point(col, row);
        this.surface = surface;
    }


    public BlkSurface(Point blkPos, AgentSettings.Direction surface) {
        this.blkPos = blkPos;
        this.surface = surface;
    }

    @Override
    public String toString() {
        return String.format("%d|%d|%s", this.blkPos.y, this.blkPos.x, this.surface.toString());   // row|col|surface
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
}
