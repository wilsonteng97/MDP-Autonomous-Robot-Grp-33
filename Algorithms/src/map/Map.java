package map;

import hardware.Agent;
import hardware.AgentSettings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Wilson Thurman Teng
 * Adapted from @author Suyash Lakhotia
 */

public class Map extends JPanel {
    private Cell[][] grid;

    private static Agent agt;
    private ArrayList<Point> detectedImg;

    public Map(Agent agt) {
        Map.agt = agt;
        grid = new Cell[MapSettings.MAP_ROWS][MapSettings.MAP_COLS];
        detectedImg = new ArrayList<Point>();
        initGrid();
    }

    /**
     * Init Map Methods
     */
    public void setAgt(Agent agt) {
        this.agt = agt;
    }
    public Agent getAgt() {
        return this.agt;
    }
    private void initGrid() {
        for (int row = 0; row < MapSettings.MAP_ROWS; row++) {
            for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                grid[row][col] = new Cell(new Point(col, row));
                // Create Virtual Wall
                createVirtualWalls(row, col);
            }
        }
    }

    /**
     * Set virtual wall if the given coordinate is a true border
     * @param row
     * @param col
     */
    public void setVirtualWallIfBorder(int row, int col) {
        if ((row == 0) || (row == MapSettings.MAP_ROWS - 1) || (col == 0) || (col == MapSettings.MAP_COLS - 1)) {
            this.grid[row][col].setVirtualWall(true);
        }
    }

    public void createVirtualWalls(int row, int col) {
//        System.out.println("[Function executed] createVirtualWalls() " + row + ", " + col);
        // Set true walls
        setVirtualWallIfBorder(row, col);
        // Set obstacle virtual walls
        if (this.grid[row][col].isObstacle()) {
//            System.out.printf(" ->grid[%d][%d] is obstacle\n", row, col);
//            if (inStartZone(row, col) || inGoalZone(row, col)) return;
            for (int r = row - 1; r <= row + 1; r++)
                for (int c = col - 1; c <= col + 1; c++)
                    if (checkValidCell(r, c)) {
//                        System.out.println("Setting Virtual wall at " + "[" + r + ", " + c + "]");
                        this.grid[r][c].setVirtualWall(true);
                    }

        }
    }
    public void resetVirtualWalls(int row, int col) {
        grid[row][col].setObstacle(false);

        if (row > 1) {
            // never set row 0
            grid[row - 1][col].setVirtualWall(false);            // bottom cell
            setVirtualWallIfBorder(row - 1, col);
            if (col < MapSettings.MAP_COLS - 2) {
                grid[row - 1][col + 1].setVirtualWall(false);    // bottom-right cell
                setVirtualWallIfBorder(row - 1, col + 1);
            }
            if (col > 1) {
                grid[row - 1][col - 1].setVirtualWall(false);    // bottom-left cell
                setVirtualWallIfBorder(row - 1, col - 1);
            }
        }

        if (row < MapSettings.MAP_ROWS - 2) {
            // never set row 19
            grid[row + 1][col].setVirtualWall(false);            // top cell
            setVirtualWallIfBorder(row + 1, col);
            if (col < MapSettings.MAP_COLS - 2) {
                grid[row + 1][col + 1].setVirtualWall(false);    // top-right cell
                setVirtualWallIfBorder(row + 1, col + 1);
            }
            if (col > 1) {
                grid[row + 1][col - 1].setVirtualWall(false);    // top-left cell
                setVirtualWallIfBorder(row + 1, col - 1);
            }
        }

        if (col > 1) {
            grid[row][col - 1].setVirtualWall(false);            // left cell
            setVirtualWallIfBorder(row, col - 1);
        }

        if (col < MapSettings.MAP_COLS - 2) {
            grid[row][col + 1].setVirtualWall(false);            // right cell
            setVirtualWallIfBorder(row, col + 1);
        }
    }

    /**
     * Grid Methods
     */
    public void removePaths() {
        for (int row = 0; row < MapSettings.MAP_ROWS; row++) {
            for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                grid[row][col].setPath(false);
            }
        }
    }
    public void setAllExplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setExplored(true);
            }
        }
    }
    public void setAllUnexplored() {
        if (this == null) return;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setExplored(inStartZone(row, col) || inGoalZone(row, col));
            }
        }
    } // Reset entire grid to be unexplored with the exception of start & goal zones.
    public void setAllPassedThru(boolean moveThru) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setMoveThru(moveThru);
            }
        }
    }

    /**
     * Cell Methods
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }
    public Cell getCell(Point pos) {
        return grid[pos.y][pos.x];
    }

    private boolean inStartZone(int row, int col) {
        int startRow = MapSettings.START_ROW; int startCol = MapSettings.START_COL;
        return (row <= startRow + 1 && row >= startRow - 1 && col <= startCol + 1 && col >= startCol - 1);
    }
    private boolean inGoalZone(int row, int col) {
        int goalRow = MapSettings.GOAL_ROW; int goalCol = MapSettings.GOAL_COL;
        return (row <= goalRow + 1 && row >= goalRow - 1 && col <= goalCol + 1 && col >= goalCol - 1);
    }

    public boolean checkValidCell(int row, int col) {
        return row >= 0 && col >= 0 && row < MapSettings.MAP_ROWS && col < MapSettings.MAP_COLS;
    }
    public boolean isObstacleCell(int row, int col) {
        return grid[row][col].isObstacle();
    }
    public boolean isWallOrObstacleCell(int row, int col) {
        return !checkValidCell(row, col) || getCell(row, col).isObstacle();
    }
    public boolean checkValidCellAndNotObs(int row, int col) {
        return checkValidCell(row, col) && !getCell(row, col).isObstacle();
    }
    public boolean checkWayPointClear(int row, int col) {
        return checkValidCell(row, col) && getCell(row, col).isExplorableCell();
    }

    /**
     * Exploration Methods
     */
    public void passedThru(int row, int col) {
        for(int r = row-1; r <= row+1; r++) {
            for(int c = col-1; c <= col+1; c++) {
                grid[r][c].setMoveThru(true);
            }
        }
    }
    public double exploredPercentage() {
        double explored = 0; double total = MapSettings.MAP_AREA;

        for (int row = 0; row < MapSettings.MAP_ROWS; row++) {
            for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                if (grid[row][col].isExplored())
                    explored++;
            }
        }
        return explored / total * 100;
    }
    public ArrayList<Cell> getNeighbours(Cell c) {
        ArrayList<Cell> neighbours = new ArrayList<Cell>();
        // UP
        if (checkValidCellAndNotObs(c.getY() + 1, c.getX())) {
            neighbours.add(getCell(c.getY() + 1, c.getX()));
        }
        // DOWN
        if (checkValidCellAndNotObs( c.getY() - 1, c.getX())) {
            neighbours.add(getCell(c.getY() - 1, c.getX()));
        }
        // RIGHT
        if (checkValidCellAndNotObs(c.getY(), c.getX() + 1)) {
            neighbours.add(getCell(c.getY(), c.getX() + 1));
        }
        // LEFT
        if (checkValidCellAndNotObs( c.getY(), c.getX() - 1)) {
            neighbours.add(getCell(c.getY(), c.getX() - 1));
        }
        return neighbours;
    }
    public HashMap<AgentSettings.Direction, Cell> getNeighboursHashMap(Cell c) {
        HashMap<AgentSettings.Direction, Cell> neighbours = new HashMap<AgentSettings.Direction, Cell>();
        // UP
        if (checkValidCellAndNotObs(c.getY() + 1, c.getX())) {
//            System.out.println("neighbours up");
            neighbours.put(AgentSettings.Direction.SOUTH, getCell(c.getY() + 1, c.getX()));
        }
        // DOWN
        if (checkValidCellAndNotObs(c.getY() - 1, c.getX())) {
//            System.out.println("neighbours down");
            neighbours.put(AgentSettings.Direction.NORTH, getCell(c.getY() - 1, c.getX()));
        }
        // RIGHT
        if (checkValidCellAndNotObs(c.getY(), c.getX() + 1)) {
//            System.out.println("neighbours right");
            neighbours.put(AgentSettings.Direction.WEST, getCell(c.getY(), c.getX() + 1));
        }
        // LEFT
        if (checkValidCellAndNotObs(c.getY(), c.getX() - 1)) {
//            System.out.println("neighbours left");
            neighbours.put(AgentSettings.Direction.EAST, getCell(c.getY(), c.getX() - 1));
        }

//        System.out.println(neighbours.size());
        return neighbours;
    }
    // Check if 3x3 robot fits if centered at a specific cell
    public boolean checkRobotFitsCell(int row, int col) {
        for(int r = row-1; r <= row+1; r++) {
            for(int c = col-1; c <= col+1; c++) {
                if(!checkValidCell(r, c) || !grid[r][c].isExplored() || grid[r][c].isObstacle())
                    return false;
            }
        }
        return true;
    }
    public void recreateVirtualWalls() {
        for (int row = 0; row < MapSettings.MAP_ROWS; row++) {
            for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                createVirtualWalls(row, col); // Create Virtual Wall
            }
        }
    }
    public boolean isMapExplored() {
        for (int row = 0; row < MapSettings.MAP_ROWS; row++) {
            for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                if (!this.getCell(row, col).isExplored()) return false;
            }
        }
        return true;
    }
    
//    public boolean checkCanMoveThruCell(int row, int col) {
//        for(int r = row-1; r <= row+1; r++) {
//            for(int c = col-1; c <= col+1; c++) {
//                if(!grid[r][c].isMoveThru())
//                    return true;
//            }
//        }
//        return false;
//    }

    /**
     * Reset Map Method
     */
    public void resetMap() {
        grid = new Cell[MapSettings.MAP_ROWS][MapSettings.MAP_COLS];
        detectedImg = new ArrayList<Point>();
        initGrid();
        agt.resetAgt();
        this.setAllUnexplored();
    }

    /**
     * Graphics Methods
     * @author Suyash Lakhotia
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Create a two-dimensional array of _DisplayCell objects for rendering.
        _DisplayCell[][] _mapCells = new _DisplayCell[MapSettings.MAP_ROWS][MapSettings.MAP_COLS];
        for (int mapRow = 0; mapRow < MapSettings.MAP_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapSettings.MAP_COLS; mapCol++) {
                _mapCells[mapRow][mapCol] = new _DisplayCell(mapCol * GraphicsSettings.CELL_SIZE, mapRow * GraphicsSettings.CELL_SIZE, GraphicsSettings.CELL_SIZE);
            }
        }
        // Paint the cells with the appropriate colors.
        for (int mapRow = 0; mapRow < MapSettings.MAP_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapSettings.MAP_COLS; mapCol++) {
                Color cellColor;

                if (inStartZone(mapRow, mapCol))
                    cellColor = GraphicsSettings.C_START;
                else if (inGoalZone(mapRow, mapCol))
                    cellColor = GraphicsSettings.C_GOAL;
                else {
                    if (!grid[mapRow][mapCol].isExplored())
                        cellColor = GraphicsSettings.C_UNEXPLORED;
                    else if (grid[mapRow][mapCol].isObstacle())
                        cellColor = GraphicsSettings.C_OBSTACLE;
                    else if (grid[mapRow][mapCol].isVirtualWall())
                        cellColor = GraphicsSettings.C_VIRTUALWALL;
                    else
                        cellColor = GraphicsSettings.C_FREE;
                }

                g.setColor(cellColor);
                g.fillRect(_mapCells[mapRow][mapCol].cellX + GraphicsSettings.MAP_X_OFFSET, _mapCells[mapRow][mapCol].cellY, _mapCells[mapRow][mapCol].cellSize, _mapCells[mapRow][mapCol].cellSize);

            }
        }

        // Paint the robot on-screen.
        g.setColor(GraphicsSettings.C_ROBOT);
        int r = agt.getAgtY();
        int c = agt.getAgtX();
        g.fillOval((c - 1) * GraphicsSettings.CELL_SIZE + GraphicsSettings.ROBOT_X_OFFSET + GraphicsSettings.MAP_X_OFFSET, GraphicsSettings.MAP_H - (r * GraphicsSettings.CELL_SIZE + GraphicsSettings.ROBOT_Y_OFFSET), GraphicsSettings.ROBOT_W, GraphicsSettings.ROBOT_H);

        // Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsSettings.C_ROBOT_DIR);
        AgentSettings.Direction d = agt.getAgtDir();
        switch (d) {
            case NORTH:
                g.fillOval(c * GraphicsSettings.CELL_SIZE + 10 + GraphicsSettings.MAP_X_OFFSET, GraphicsSettings.MAP_H - r * GraphicsSettings.CELL_SIZE - 15, GraphicsSettings.ROBOT_DIR_W, GraphicsSettings.ROBOT_DIR_H);
                break;
            case EAST:
                g.fillOval(c * GraphicsSettings.CELL_SIZE + 35 + GraphicsSettings.MAP_X_OFFSET, GraphicsSettings.MAP_H - r * GraphicsSettings.CELL_SIZE + 10, GraphicsSettings.ROBOT_DIR_W, GraphicsSettings.ROBOT_DIR_H);
                break;
            case SOUTH:
                g.fillOval(c * GraphicsSettings.CELL_SIZE + 10 + GraphicsSettings.MAP_X_OFFSET, GraphicsSettings.MAP_H - r * GraphicsSettings.CELL_SIZE + 35, GraphicsSettings.ROBOT_DIR_W, GraphicsSettings.ROBOT_DIR_H);
                break;
            case WEST:
                g.fillOval(c * GraphicsSettings.CELL_SIZE - 15 + GraphicsSettings.MAP_X_OFFSET, GraphicsSettings.MAP_H - r * GraphicsSettings.CELL_SIZE + 10, GraphicsSettings.ROBOT_DIR_W, GraphicsSettings.ROBOT_DIR_H);
                break;
        }
        setBackground(GraphicsSettings.C_BACKGROUND);
    }

    private class _DisplayCell {
        public final int cellX; public final int cellY;
        public final int cellSize;

        public _DisplayCell(int borderX, int borderY, int borderSize) {
            this.cellX = borderX + GraphicsSettings.CELL_LINE_WEIGHT;
            this.cellY = GraphicsSettings.MAP_H - (borderY - GraphicsSettings.CELL_LINE_WEIGHT);
            this.cellSize = borderSize - (GraphicsSettings.CELL_LINE_WEIGHT * 2);
        }
    }



}
