package logic;

import hardware.Agent;
import hardware.AgentSettings;

import java.awt.*;
import java.util.ArrayList;
import java.io.*;

import map.*;

/**
 * @author Wilson Thurman Teng
 * Adapted from @author Suyash Lakhotia
 */

public class TestMap extends Map {
    private int[][] gridInput = new int[20][15];
    private final Cell[][] grid;

    private static Agent agt;
    private ArrayList<Point> detectedImg;

    public TestMap(Agent agent) throws Exception {
        super(agent);
        agt = agent;
        grid = new Cell[MapSettings.MAP_ROWS][MapSettings.MAP_COLS];
        detectedImg = new ArrayList<Point>();

        File file = new File("/Users/guomukun/mdp/MDP-Autonomous-Robot-Grp-33/Algorithms/src/logic/testmap/map0");

        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        int i = 0;
        while ((st = br.readLine()) != null) {
            String[] tmp = st.split(" ");
            for (int j = 0; j < 15; j++) {
                gridInput[i][j] = Integer.parseInt(tmp[j]);
            }
            i++;
        }
//        for (int[] row : gridInput) {
//            for (int val : row) {
//                if (val == 1)
//                    System.out.format("*");
//                else
//                    System.out.format(" ");
//            }
//            System.out.println();
//        }
    initGrid();
    }

    /**
     * Init Map Methods
     */
    private void initGrid() {
        for (int row = 0; row < MapSettings.MAP_ROWS; row++) {
            for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                grid[row][col] = new Cell(new Point(col, row));
                if (gridInput[row][col] == 1) {
                    grid[row][col].setObstacle(true);
                }
                // Create Virtual Wall
                createVirtualWalls(row, col);
            }
        }
    }
    private void createVirtualWalls(int row, int col) {
        // Set true walls
        if ((row == 0) || (row == MapSettings.MAP_ROWS - 1) || (col == 0) || (col == MapSettings.MAP_COLS - 1)) {
            grid[row][col].setVirtualWall(true);
        }
        // Set obstacle virtual walls
        if (grid[row][col].isObstacle()) {
            for (int r = row - 1; r <= row + 1; r++)
                for (int c = col - 1; c <= col + 1; c++)
                    if (checkValidCell(r, c)) {
                        grid[row][col].setVirtualWall(true);
                    }
        }
    }

    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean getIsObstacleOrWall(int row, int col) {
        return !checkValidCell(row, col) || getCell(row, col).isObstacle();
    }

    public String toString() {
        int row = MapSettings.MAP_ROWS;
        int col = MapSettings.MAP_COLS;
        String st = "";
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (grid[i][j].isObstacle()) {
                    st = st.concat("*");
                } else if (grid[i][j].isVirtualWall()){
                    st = st.concat("o");
                } else {
                    st = st.concat(" ");
                }
                System.out.println(grid[i][j]);
            }
            st = st.concat("\n");
        }
        return st;
    }
}
