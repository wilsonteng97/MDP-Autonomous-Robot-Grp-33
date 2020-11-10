package map;

import hardware.AgentSettings;

import java.awt.*;
import java.util.ArrayList;

public class ObsSurface {
    private Point pos;
    private AgentSettings.Direction surface;

    public ObsSurface(int col, int row, AgentSettings.Direction surface) {
        this.pos = new Point(col, row);
        this.surface = surface;
    }


    public ObsSurface(Point pos, AgentSettings.Direction surface) {
        this.pos = pos;
        this.surface = surface;
    }


    public Point getPos() {
        return pos;
    }

    public int getRow() {
        return this.pos.y;
    }

    public int getCol() {
        return this.pos.x;
    }

    public AgentSettings.Direction getSurface() {
        return surface;
    }

    public void setPos(Point blkPos) {
        this.pos = blkPos;
    }

    public void setSurface(AgentSettings.Direction dir) {
        this.surface = dir;
    }

    public void setPos(int row, int col) {
        this.pos = new Point(col, row);
    }

    public ArrayList<ObsSurface> getSideSurfaces(int offset, AgentSettings.Direction agtDir) {
        Point p1; Point p2;
        ArrayList<ObsSurface> obsSurfaces = new ArrayList<ObsSurface>();

        switch (surface) {
            case EAST:
            case WEST:
                p1 = new Point(pos.x, pos.y + 1 * offset);
                p2 = new Point(pos.x, pos.y + -1 * offset);
                break;
            case NORTH:
            case SOUTH:
                p1 = new Point(pos.x + 1 * offset, pos.y);
                p2 = new Point(pos.x + -1 * offset, pos.y);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + surface);
        }

        switch (agtDir) {
            case NORTH:
            case WEST:
                obsSurfaces.add(new ObsSurface(p1, surface));
                obsSurfaces.add(new ObsSurface(p2, surface));
                break;
            case SOUTH:
            case EAST:
                obsSurfaces.add(new ObsSurface(p2, surface));
                obsSurfaces.add(new ObsSurface(p1, surface));
                break;
        }

        return obsSurfaces;
    }

    @Override
    public String toString() {
        return String.format("%d:%d:%s", this.getCol(), this.getRow(), AgentSettings.Direction.print(this.surface)); // col|row|surface
    }
}
