package logic.fastestpath;

import hardware.Agent;
import hardware.AgentSettings;
import hardware.AgentSettings.Actions;
import hardware.AgentSettings.Direction;
import map.Cell;
import map.Map;
import map.MapSettings;
import network.NetworkMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;



public class AStarHeuristicSearch extends FastestPathAlgo {

    private ArrayList<Cell> toVisit;        // array of Cells to be visited
    private ArrayList<Cell> visited;        // array of visited Cells
    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
    private Cell[] neighbors;               // array of neighbors of current Cell
    private double[][] gCosts;              // array of real cost from START to [row][col] i.e. g(n)
    private int loopCount;

    private Scanner scanner = new Scanner(System.in);

    /**
     * Constructor called during Fastest Path leaderboard.
     * explorationMode not used here
     */
    public AStarHeuristicSearch(Map exploredMap, Agent bot) {
        super(exploredMap, bot);
    }

    /**
     * Constructor called during Exploration/ImageReg leaderboard (to use fastest path for ImageReg leaderboard)
     * explorationMode = true
     */
    public AStarHeuristicSearch(Map exploredMap, Agent bot, Map realMap) {
        super(exploredMap, bot, realMap);
    }

    /**
     * Initialise the FastestPathAlgo object.
     */
    protected void initObject(Map map, Agent bot) {
        super.initObject(map, bot);
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
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
        toVisit.add(current);

        // Initialise starting point
        gCosts[bot.getAgtRow()][bot.getAgtCol()] = 0;
        this.loopCount = 0;
    }

    /**
     * Returns the Cell inside toVisit with the minimum g(n) + h(n).
     */
    private Cell minimumCostCell(int goalRow, int getCol) {
        int size = toVisit.size();
        double minCost = AgentSettings.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            double gCost = gCosts[(toVisit.get(i).getRow())][(toVisit.get(i).getCol())];
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
     * TODO costH could be stored to avoid duplicated calculation
     */
    private double costH(Cell b, int goalRow, int goalCol) {
        // Heuristic: The no. of moves will be equal to the difference in the row and column values.
        double movementCost = (Math.abs(goalCol - b.getCol()) + Math.abs(goalRow - b.getRow())) * AgentSettings.MOVE_COST;

        if (movementCost == 0) return 0;

        // Heuristic: If b is not in the same row or column, one turn will be needed.
        double turnCost = 0;
        if (goalCol - b.getCol() != 0 || goalRow - b.getRow() != 0) {
            turnCost = AgentSettings.TURN_COST;
        }

        return movementCost + turnCost;
    }


    /**
     * Calculate the actual cost of moving from Cell a to Cell b (assuming both are neighbors).
     */
    private double costG(Cell a, Cell b, Direction aDir) {
        double moveCost = AgentSettings.MOVE_COST; // one movement to neighbor

        double turnCost;
        Direction targetDir = getTargetDir(a.getRow(), a.getCol(), aDir, b);
        turnCost = getTurnCost(aDir, targetDir);

        return moveCost + turnCost;
    }

    /**
     * Use a software bot to find the fastest path from the robot's current position to [goalRow, goalCol].
     */
    @Override
    public String runFastestPath(int goalRow, int goalCol) {
        System.out.println("Calculating fastest path from (" + current.getRow() + ", " + current.getCol() + ") to goal (" + goalRow + ", " + goalCol + ")...");

        Stack<Cell> path;
        do {
            loopCount++;

            // Get cell with minimum cost from toVisit and assign it to current.
            current = minimumCostCell(goalRow, goalCol);

            // Point the robot in the direction of current from the previous cell.
            if (parents.containsKey(current)) {
                curDir = getTargetDir(parents.get(current).getRow(), parents.get(current).getCol(), curDir, current);
            }

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("Goal visited. Path found!");
                path = getPath(goalRow, goalCol);
                printFastestPath(path);
                return executePath(path, goalRow, goalCol);
            }

            // Setup neighbors of current cell. [Top, Bottom, Left, Right].
            if (exploredMap.checkValidCell(current.getRow() + 1, current.getCol())) {
                neighbors[0] = exploredMap.getCell(current.getRow() + 1, current.getCol());
                if (!canBeVisited(neighbors[0])) {
                    neighbors[0] = null;
                }
            }
            if (exploredMap.checkValidCell(current.getRow() - 1, current.getCol())) {
                neighbors[1] = exploredMap.getCell(current.getRow() - 1, current.getCol());
                if (!canBeVisited(neighbors[1])) {
                    neighbors[1] = null;
                }
            }
            if (exploredMap.checkValidCell(current.getRow(), current.getCol() - 1)) {
                neighbors[2] = exploredMap.getCell(current.getRow(), current.getCol() - 1);
                if (!canBeVisited(neighbors[2])) {
                    neighbors[2] = null;
                }
            }
            if (exploredMap.checkValidCell(current.getRow(), current.getCol() + 1)) {
                neighbors[3] = exploredMap.getCell(current.getRow(), current.getCol() + 1);
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
                        gCosts[neighbors[i].getRow()][neighbors[i].getCol()] = gCosts[current.getRow()][current.getCol()] + costG(current, neighbors[i], curDir);
                        toVisit.add(neighbors[i]);
                    } else {
                        double currentGScore = gCosts[neighbors[i].getRow()][neighbors[i].getCol()];
                        double newGScore = gCosts[current.getRow()][current.getCol()] + costG(current, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            gCosts[neighbors[i].getRow()][neighbors[i].getCol()] = newGScore;
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
    @Override
    protected Stack<Cell> getPath(int goalRow, int goalCol) {
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
    @Override
    protected String executePath(Stack<Cell> path, int goalRow, int goalCol) {
        StringBuilder outputString = new StringBuilder();

        Cell temp = path.pop();
        Direction targetDir;

        ArrayList<Actions> movements = new ArrayList<>();

        Agent tempBot = new Agent(bot.getAgtY(), bot.getAgtX(), true);
        tempBot.setAgtDir(bot.getAgtDir());
        tempBot.setSpeed(0);
        while ((tempBot.getAgtRow() != goalRow) || (tempBot.getAgtCol() != goalCol)) {
//            System.out.println("Checking " + temp);
            if (tempBot.getAgtRow() == temp.getRow() && tempBot.getAgtCol() == temp.getCol()) {
                temp = path.pop();
//                System.out.println(" -> Update to " + temp);
            }

            targetDir = getTargetDir(tempBot.getAgtRow(), tempBot.getAgtCol(), tempBot.getAgtDir(), temp);

            Actions m;
            if (tempBot.getAgtDir() != targetDir) {
                m = getTargetMove(tempBot.getAgtDir(), targetDir);
            } else {
                m = Actions.FORWARD;
            }

            System.out.println("Movement " + Actions.print(m) + " from (" + tempBot.getAgtCol() + ", " + tempBot.getAgtRow() + ") to (" + temp.getCol() + ", " + temp.getRow() + ")");

            tempBot.takeAction(m, 1, exploredMap, realMap);
            movements.add(m);
            outputString.append(Actions.print(m));
//            scanner.nextLine();
        }

        if (bot.isSim() || explorationMode) {
            if (explorationMode) System.out.println("[ASTAR] in exploration mode");
            for (Actions x : movements) {
                if (x == Actions.FORWARD) {
                    if (!canMoveForward()) {
                        System.out.println("Early termination of fastest path execution.");
                        return "T";
                    }
                }

                bot.takeAction(x, 1, exploredMap, realMap);
                this.exploredMap.repaint();

                // During exploration, use sensor data to update exploredMap.
                if (explorationMode) {
                    bot.setSensors();
                    bot.senseEnv(this.exploredMap, this.realMap);
                    this.exploredMap.repaint();
                }
            }
        }
        else {
            // TODO real bot
//            int fCount = 0;
//            for (Actions x : movements) {
//                if (x == Actions.FORWARD) {
//                    fCount++;
//                    if (fCount == 10) {
//                        bot.takeAction(Actions.FORWARD, fCount, exploredMap, realMap);
//                        fCount = 0;
//                        exploredMap.repaint();
//                    }
//                } else if (x == Actions.FACE_RIGHT || x == Actions.FACE_LEFT) {
//                    if (fCount > 0) {
//                        bot.takeAction(Actions.FORWARD, fCount, exploredMap, realMap);
//                        fCount = 0;
//                        exploredMap.repaint();
//                    }
//
//                    bot.takeAction(x, 1, exploredMap, realMap);
//                    exploredMap.repaint();
//                }
//            }
//
//            if (fCount > 0) {
//                bot.takeAction(Actions.FORWARD, fCount, exploredMap, realMap);
//                exploredMap.repaint();
//            }
//            NetworkMgr.getInstance().sendMsg(outputString + "", NetworkMgr.INSTRUCTIONS);

        }

        System.out.println("\nMovements: " + outputString.toString());
        return outputString.toString();
    }

    /**
     * Prints the fastest path from the Stack object.
     */
    @Override
    protected void printFastestPath(Stack<Cell> path) {
        System.out.println("\nLooped " + loopCount + " times.");
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getCol() + ", " + temp.getRow() + ") --> ");
            else System.out.print("(" + temp.getCol() + ", " + temp.getRow() + ")");
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
