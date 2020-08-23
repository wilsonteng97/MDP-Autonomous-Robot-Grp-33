package logic.fastestpath;

import map.Cell;
import map.Map;
import map.MapSettings;
import hardware.Agent;
import hardware.AgentSettings;
import hardware.AgentSettings.Direction;
import hardware.AgentSettings.Actions;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;



public class AStarHeuristicSearch {
    private ArrayList<Cell> toVisit;        // array of Cells to be visited
    private ArrayList<Cell> visited;        // array of visited Cells
    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
    private Cell current;                   // current Cell
    private Cell[] neighbors;               // array of neighbors of current Cell
    private Direction curDir;               // current direction of robot
    private double[][] gCosts;              // array of real cost from START to [row][col] i.e. g(n)
    private Agent bot;
    private Map exploredMap;
    private final Map realMap;
    private int loopCount;
    private boolean explorationMode;

    public AStarHeuristicSearch(Map exploredMap, Agent bot) {
        this.realMap = null;
        initObject(exploredMap, bot);
    }

    public AStarHeuristicSearch(Map exploredMap, Agent bot, Map realMap) {
        this.realMap = realMap;
        this.explorationMode = true;
        initObject(exploredMap, bot);
    }

    /**
     * Initialise the FastestPathAlgo object.
     */
    private void initObject(Map map, Agent bot) {
        this.bot = bot;
        this.exploredMap = map;
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.current = map.getCell(bot.getAgtX(), bot.getAgtY());
        this.curDir = bot.getAgtDir();
        this.gCosts = new double[MapSettings.MAP_ROWS][MapSettings.MAP_COLS];

        // Initialise gCosts array
        for (int i = 0; i < MapSettings.MAP_ROWS; i++) {
            for (int j = 0; j < MapSettings.MAP_COLS; j++) {
                Cell cell = map.getCell(i, j);
                if (!canBeVisited(cell)) {
                    gCosts[i][j] = AgentSettings.INFINITE_COST;
                } else {
                    gCosts[i][j] = 0;
                }
            }
        }
        toVisit.add(current); // add START to toVisit

        // Initialise starting point
        gCosts[bot.getAgtX()][bot.getAgtY()] = 0;
        this.loopCount = 0;
    }


    /**
     * Returns true if the cell can be visited.
     */
    private boolean canBeVisited(Cell c) {
        return c.isExplored() && !c.isObstacle() && !c.isVirtualWall();
    }

    /**
     * Returns the Cell inside toVisit with the minimum g(n) + h(n).
     * TODO a heap implementation for better performance, not sure it's necessary since toVisit should be small
     */
    private Cell minimumCostCell(int goalRow, int getCol) {
        int size = toVisit.size();
        double minCost = AgentSettings.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            double gCost = gCosts[(toVisit.get(i).getX())][(toVisit.get(i).getY())];
            double cost = gCost + costH(toVisit.get(i), goalRow, getCol);
            if (cost < minCost) {
                minCost = cost;
                result = toVisit.get(i);
            }
        }

        return result;
    }

    /**
     * Returns the heuristic cost i.e. h(n) from a given Cell to a given [goalRow, goalCol] in the maze.
     */
    private double costH(Cell b, int goalRow, int goalCol) {
        // Heuristic: The no. of moves will be equal to the difference in the row and column values.
        double movementCost = (Math.abs(goalCol - b.getY()) + Math.abs(goalRow - b.getX())) * AgentSettings.MOVE_COST;

        if (movementCost == 0) return 0;

        // Heuristic: If b is not in the same row or column, one turn will be needed.
        double turnCost = 0;
        if (goalCol - b.getY() != 0 || goalRow - b.getX() != 0) {
            turnCost = AgentSettings.TURN_COST;
        }

        return movementCost + turnCost;
    }

    /**
     * Returns the target direction of the bot from [botR, botC] to target Cell.
     * Assuming bot position and target position are neighbors
     */
    private Direction getTargetDir(int botR, int botC, Direction botDir, Cell target) {
        if (botC - target.getY() > 0) {
            return Direction.WEST;
        } else if (target.getY() - botC > 0) {
            return Direction.EAST;
        } else {
            if (botR - target.getX() > 0) {
                return Direction.SOUTH;
            } else if (target.getX() - botR > 0) {
                return Direction.NORTH;
            } else {
                return botDir;
            }
        }
    }

    /**
     * Get the actual turning cost from one DIRECTION to another.
     * TODO need to test
     */
    private double getTurnCost(Direction a, Direction b) {
        int numOfTurn = Math.abs(a.ordinal() - b.ordinal()) / 2;    // need to test
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * AgentSettings.TURN_COST);
    }

    /**
     * Calculate the actual cost of moving from Cell a to Cell b (assuming both are neighbors).
     */
    private double costG(Cell a, Cell b, Direction aDir) {
        double moveCost = AgentSettings.MOVE_COST; // one movement to neighbor

        double turnCost;
        Direction targetDir = getTargetDir(a.getX(), a.getY(), aDir, b);
        turnCost = getTurnCost(aDir, targetDir);

        return moveCost + turnCost;
    }


    /**
     * Find the fastest path from the robot's current position to [goalRow, goalCol].
     */
    public String runFastestPath(int goalRow, int goalCol) {
        System.out.println("Calculating fastest path from (" + current.getX() + ", " + current.getY() + ") to goal (" + goalRow + ", " + goalCol + ")...");

        Stack<Cell> path;
        do {
            loopCount++;

            // Get cell with minimum cost from toVisit and assign it to current.
            current = minimumCostCell(goalRow, goalCol);

            // Point the robot in the direction of current from the previous cell.
            if (parents.containsKey(current)) {
                curDir = getTargetDir(parents.get(current).getX(), parents.get(current).getY(), curDir, current);
            }

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            // Terminating condition
            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("Goal visited. Path found!");
                path = getPath(goalRow, goalCol);
                printFastestPath(path);
                return executePath(path, goalRow, goalCol);
            }

            // Setup neighbors of current cell. [Top, Bottom, Left, Right].
            // Top
            if (exploredMap.checkValidCell(current.getX() + 1, current.getY())) {
                neighbors[0] = exploredMap.getCell(current.getX() + 1, current.getY());
                if (!canBeVisited(neighbors[0])) {
                    neighbors[0] = null;
                }
            }
            // Bottom
            if (exploredMap.checkValidCell(current.getX() - 1, current.getY())) {
                neighbors[1] = exploredMap.getCell(current.getX() - 1, current.getY());
                if (!canBeVisited(neighbors[1])) {
                    neighbors[1] = null;
                }
            }
            // Left
            if (exploredMap.checkValidCell(current.getX(), current.getY() - 1)) {
                neighbors[2] = exploredMap.getCell(current.getX(), current.getY() - 1);
                if (!canBeVisited(neighbors[2])) {
                    neighbors[2] = null;
                }
            }
            // Right
            if (exploredMap.checkValidCell(current.getX(), current.getY() + 1)) {
                neighbors[3] = exploredMap.getCell(current.getX(), current.getY() + 1);
                if (!canBeVisited(neighbors[3])) {
                    neighbors[3] = null;
                }
            }

            // Iterate through neighbors and update the g(n) values of each.
            for (int i = 0; i < 4; i++) {
                if (neighbors[i] != null) {
                    if (visited.contains(neighbors[i])) {
                        continue;
                    }

                    if (!(toVisit.contains(neighbors[i]))) {
                        parents.put(neighbors[i], current);
                        gCosts[neighbors[i].getX()][neighbors[i].getY()] = gCosts[current.getX()][current.getY()] + costG(current, neighbors[i], curDir);
                        toVisit.add(neighbors[i]);
                    } else {
                        double currentGScore = gCosts[neighbors[i].getX()][neighbors[i].getY()];
                        double newGScore = gCosts[current.getX()][current.getY()] + costG(current, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            gCosts[neighbors[i].getX()][neighbors[i].getY()] = newGScore;
                            parents.put(neighbors[i], current);
                        }
                    }
                }
            }
        } while (!toVisit.isEmpty());

        System.out.println("Path not found!");
        return null;
    }

    /**
     * Generates path in reverse using the parents HashMap.
     */
    private Stack<Cell> getPath(int goalRow, int goalCol) {
        Stack<Cell> actualPath = new Stack<>();
        Cell temp = exploredMap.getCell(goalRow, goalCol);

        while (true) {
            actualPath.push(temp);
            temp = parents.get(temp);
            if (temp == null) {
                break;
            }
        }

        return actualPath;
    }

    /**
     * Executes the fastest path and returns a StringBuilder object with the path steps.
     */
    private String executePath(Stack<Cell> path, int goalRow, int goalCol) {
        StringBuilder outputString = new StringBuilder();

        Cell temp = path.pop();
        Direction targetDir;

        ArrayList<Actions> movements = new ArrayList<>();

        Agent tempBot = new Agent(1, 1, Direction.NORTH, true);
        tempBot.setSpeed(0);
        while ((tempBot.getAgtX() != goalRow) || (tempBot.getAgtY() != goalCol)) {
            if (tempBot.getAgtX() == temp.getX() && tempBot.getAgtY() == temp.getY()) {
                temp = path.pop();
            }

            targetDir = getTargetDir(tempBot.getAgtX(), tempBot.getAgtY(), tempBot.getAgtDir(), temp);

            Actions m;
            if (tempBot.getAgtDir() != targetDir) {
                m = getTargetMove(tempBot.getAgtDir(), targetDir);
            } else {
                m = Actions.FORWARD;
            }

            System.out.println("Movement " + Actions.print(m) + " from (" + tempBot.getAgtX() + ", " + tempBot.getAgtY() + ") to (" + temp.getX() + ", " + temp.getY() + ")");

            tempBot.takeAction(m, false);
            movements.add(m);
            outputString.append(Actions.print(m));
        }

        if (bot.isSim() || explorationMode) {
            for (Actions x : movements) {
                if (x == Actions.FORWARD) {
                    if (!canMoveForward()) {
                        System.out.println("Early termination of fastest path execution.");
                        return "T";
                    }
                }

                bot.takeAction(x, false);
                this.exploredMap.repaint();

                // During exploration, use sensor data to update exploredMap.
                // TODO get update sensor
//                if (explorationMode) {
//                    bot.setSensors();
//                    bot.sense(this.exploredMap, this.realMap);
//                    this.exploredMap.repaint();
//                }
            }
        }
        else {
            // TODO real bot
//            int fCount = 0;
//            for (Actions x : movements) {
//                if (x == Actions.FORWARD) {
//                    fCount++;
//                    if (fCount == 10) {
//                        bot.moveForwardMultiple(fCount);
//                        fCount = 0;
//                        exploredMap.repaint();
//                    }
//                } else if (x == MOVEMENT.RIGHT || x == MOVEMENT.LEFT) {
//                    if (fCount > 0) {
//                        bot.moveForwardMultiple(fCount);
//                        fCount = 0;
//                        exploredMap.repaint();
//                    }
//
//                    bot.move(x);
//                    exploredMap.repaint();
//                }
//            }
//
//            if (fCount > 0) {
//                bot.moveForwardMultiple(fCount);
//                exploredMap.repaint();
//            }
        }

        System.out.println("\nMovements: " + outputString.toString());
        return outputString.toString();
    }

    /**
     * Returns true if the robot can move forward one cell with the current heading.
     */
    private boolean canMoveForward() {
        int row = bot.getAgtX();
        int col = bot.getAgtY();

        switch (bot.getAgtDir()) {
            case NORTH:
                if (!exploredMap.isObstacleCell(row + 2, col - 1) && !exploredMap.isObstacleCell(row + 2, col) && !exploredMap.isObstacleCell(row + 2, col + 1)) {
                    return true;
                }
                break;
            case EAST:
                if (!exploredMap.isObstacleCell(row + 1, col + 2) && !exploredMap.isObstacleCell(row, col + 2) && !exploredMap.isObstacleCell(row - 1, col + 2)) {
                    return true;
                }
                break;
            case SOUTH:
                if (!exploredMap.isObstacleCell(row - 2, col - 1) && !exploredMap.isObstacleCell(row - 2, col) && !exploredMap.isObstacleCell(row - 2, col + 1)) {
                    return true;
                }
                break;
            case WEST:
                if (!exploredMap.isObstacleCell(row + 1, col - 2) && !exploredMap.isObstacleCell(row, col - 2) && !exploredMap.isObstacleCell(row - 1, col - 2)) {
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Returns the movement to execute to get from one direction to another.
     */
    private Actions getTargetMove(Direction a, Direction b) {
        switch (a) {
            case NORTH:
                switch (b) {
                    case NORTH:
                        return Actions.ERROR;
                    case SOUTH:
                        return Actions.FACE_LEFT;
                    case WEST:
                        return Actions.FACE_LEFT;
                    case EAST:
                        return Actions.FACE_RIGHT;
                }
                break;
            case SOUTH:
                switch (b) {
                    case NORTH:
                        return Actions.FACE_LEFT;
                    case SOUTH:
                        return Actions.ERROR;
                    case WEST:
                        return Actions.FACE_RIGHT;
                    case EAST:
                        return Actions.FACE_LEFT;
                }
                break;
            case WEST:
                switch (b) {
                    case NORTH:
                        return Actions.FACE_RIGHT;
                    case SOUTH:
                        return Actions.FACE_LEFT;
                    case WEST:
                        return Actions.ERROR;
                    case EAST:
                        return Actions.FACE_LEFT;
                }
                break;
            case EAST:
                switch (b) {
                    case NORTH:
                        return Actions.FACE_LEFT;
                    case SOUTH:
                        return Actions.FACE_RIGHT;
                    case WEST:
                        return Actions.FACE_LEFT;
                    case EAST:
                        return Actions.ERROR;
                }
        }
        return Actions.ERROR;
    }

    /**
     * Prints the fastest path from the Stack object.
     */
    private void printFastestPath(Stack<Cell> path) {
        System.out.println("\nLooped " + loopCount + " times.");
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getX() + ", " + temp.getY() + ") --> ");
            else System.out.print("(" + temp.getX() + ", " + temp.getY() + ")");
        }

        System.out.println("\n");
    }

    /**
     * Prints all the current g(n) values for the cells.
     */
    public void printGCosts() {
        for (int i = 0; i < MapSettings.MAP_ROWS; i++) {
            for (int j = 0; j < MapSettings.MAP_COLS; j++) {
                System.out.print(gCosts[MapSettings.MAP_ROWS - 1 - i][j]);
                System.out.print(";");
            }
            System.out.println("\n");
        }
    }
}
