package logic.exploration;

import hardware.Agent;
import hardware.AgentSettings;
import logic.fastestpath.AStarHeuristicSearch;
import map.Cell;
import map.Map;
import map.MapSettings;
import network.NetworkMgr;
import utils.SimulatorSettings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Queue;
import java.util.HashSet;

abstract public class ExplorationAlgo {
    protected final Map exploredMap;
    protected final Map realMap;
    protected final Agent bot;
    protected int coverageLimit = 300;
    protected int timeLimit = 3600;    // in second
    protected int areaExplored;
    protected long startTime; // in millisecond
    protected long currentTime;
    protected long endTime;   // in millisecond
    Scanner scanner = new Scanner(System.in);

    public ExplorationAlgo(Map exploredMap, Map realMap, Agent bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;

        System.out.println("[coverageLimit && timeLimit] " + coverageLimit + " | " + timeLimit);
    }

    public void runExploration() {
        // FIXME check for real bot connection
//        System.out.println("[DEBUG: runExploration] executed");
        if (!bot.isSim()) {
            System.out.println("Starting calibration...");

//            NetworkMgr.getInstance().receiveMsg();

            // TODO initial calibration
//            if (!bot.isSim()) {
//                bot.takeAction(AgentSettings.Actions.FACE_LEFT, 0, exploredMap, realMap);
//                NetworkMgr.getInstance().receiveMsg();
//                bot.takeAction(AgentSettings.Actions.CALIBRATE);
//                NetworkMgr.getInstance().receiveMsg();
//                bot.takeAction(AgentSettings.Actions.FACE_LEFT, 0, exploredMap, realMap);
//                NetworkMgr.getInstance().receiveMsg();
//                bot.takeAction(AgentSettings.Actions.CALIBRATE);
//                NetworkMgr.getInstance().receiveMsg();
//                bot.takeAction(AgentSettings.Actions.FACE_RIGHT, 0, exploredMap, realMap);
//                NetworkMgr.getInstance().receiveMsg();
//                bot.takeAction(AgentSettings.Actions.CALIBRATE);
//                NetworkMgr.getInstance().receiveMsg();
//                bot.takeAction(AgentSettings.Actions.FACE_RIGHT, 0, exploredMap, realMap);
//            }

            while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = NetworkMgr.getInstance().receiveMsg();
//                String[] msgArr = msg.split(";");
//                if (msgArr[0].equals(NetworkMgr.EXP_START)) break;
                if (msg.equals(NetworkMgr.EXP_START)) break;
            }
        }

        // Explore
        System.out.println("Starting exploration...");

        // prepare for timing
        startTime = System.currentTimeMillis();
        endTime = getEndTime(startTime, timeLimit);         // startTime + (timeLimit * 1000);

        areaExplored = calculateAreaExplored();
        System.out.println("Starting state - area explored: " + areaExplored);
        System.out.println();

        explorationLoop(bot.getAgtY(), bot.getAgtX());

        if (!bot.isSim()) {
            NetworkMgr.getInstance().sendMsg(null, NetworkMgr.BOT_START);
        }
        senseAndRepaint();
    }


    abstract protected void nextMove();

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    protected void explorationLoop(int r, int c) {

        do {
            nextMove();
            System.out.printf("Current Bot Pos: [%d, %d]\n", bot.getAgtX(), bot.getAgtY());

            areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);
            System.out.println();

            if (bot.getAgtY() == r && bot.getAgtX() == c) {
                if (areaExplored >= 100) {
                    System.out.println("Exploration finished in advance!");
                    break;
                }
            }
            currentTime = System.currentTimeMillis();
//            scanner.nextLine();

        } while (areaExplored <= coverageLimit && currentTime <= endTime);

        if (areaExplored == 300) {
//            System.out.println("[explorationLoop()] goHome()");
            goHome();
        } else if ((areaExplored >= coverageLimit && areaExplored < 300) || (currentTime >= endTime && areaExplored < 300)) {
            // Exceed coverage or time limit
//            System.out.println("[explorationLoop()] Exceed coverage or time limit");
            if (areaExplored == coverageLimit) System.out.printf("Reached coverage limit, successfully explored %d grids\n", areaExplored);
            if (currentTime > endTime) System.out.printf("Reached time limit, exploration has taken %d millisecond(ms)\n", getElapsedTime(currentTime, startTime));
            System.out.println("Arena not fully explored, goHome() may incur errors, enter \"yes\" to continue: ");
            String userInput = scanner.nextLine();
            if (userInput.equals("yes")) goHome();
        } else {
//            System.out.println("[explorationLoop()] Not breaking limit, but arena not fully explored");
//            System.out.println("areaExplored " + areaExplored + " | CoverageLimit " + coverageLimit + " | currentTime " + currentTime + " | endTime " + endTime);
            goHome(); // reset bot
            System.out.printf("Current Bot Pos: [%d, %d]\n", bot.getAgtX(), bot.getAgtY());

            // visit unvisited(blocked) cells
            AStarHeuristicSearch keepExploring;
            ArrayList<Cell> unExploredCells = findUnexploredAndCanVisit();
            int targetRow, targetCol;
            for (Cell targetCell : unExploredCells) {
                targetRow = targetCell.getRow();
                targetCol = targetCell.getCol();
                keepExploring = new AStarHeuristicSearch(exploredMap, bot, realMap);
                keepExploring.runFastestPath(targetRow, targetCol);
            }

            // visit surrounding cells of those unvisited cells
            Cell destCell;
            unExploredCells = findUnexplored();
            System.out.println("Unexplored cells: " + unExploredCells.size());
            for (Cell targetCell : unExploredCells) {
                targetRow = targetCell.getRow();
                targetCol = targetCell.getCol();
                destCell = findSurroundingReachable(targetRow, targetCol);
                System.out.println(destCell);
                keepExploring = new AStarHeuristicSearch(exploredMap, bot, realMap);
                keepExploring.runFastestPath(destCell.getRow(), destCell.getCol());
            }
            System.out.println("Visited all cells!");

            goHome();
        }
        System.out.println("Exploration Completed!");
    }

    /**
     * Returns true if the right side of the robot is free to move into.
     */
    protected boolean lookRight() {
//        System.out.println("[DEBUG: Function executed] lookRight()");
        switch (bot.getAgtDir()) {
            case NORTH:
                return eastFree();
            case EAST:
                return southFree();
            case SOUTH:
                return westFree();
            case WEST:
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    protected boolean lookForward() {
//        System.out.println("[DEBUG: Function executed] lookForward()");
        switch (bot.getAgtDir()) {
            case NORTH:
                return northFree();
            case EAST:
                return eastFree();
            case SOUTH:
                return southFree();
            case WEST:
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move into.
     */
    protected boolean lookLeft() {
//        System.out.println("[DEBUG: Function executed] lookLeft()");
        switch (bot.getAgtDir()) {
            case NORTH:
                return westFree();
            case EAST:
                return northFree();
            case SOUTH:
                return eastFree();
            case WEST:
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    protected boolean northFree() {
//        System.out.println("[DEBUG: Function executed] northFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    protected boolean eastFree() {
//        System.out.println("[DEBUG: Function executed] eastFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    protected boolean southFree() {
//        System.out.println("[DEBUG: Function executed] southFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    protected boolean westFree() {
//        System.out.println("[DEBUG: Function executed] westFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Send the bot to START and points the bot northwards
     */
    protected void goHome() {
        if (!bot.hasEnteredGoal() && coverageLimit == 300 && timeLimit == 3600) {
            AStarHeuristicSearch goToGoal = new AStarHeuristicSearch(exploredMap, bot, realMap);
            goToGoal.runFastestPath(AgentSettings.GOAL_ROW, AgentSettings.GOAL_COL);
        }

        AStarHeuristicSearch returnToStart = new AStarHeuristicSearch(exploredMap, bot, realMap);
        returnToStart.runFastestPath(AgentSettings.START_ROW, AgentSettings.START_COL);

        System.out.println("Exploration complete!");
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        // realbot
        if (!bot.isSim()) {
            turnBotDirection(AgentSettings.Direction.WEST);
            moveBot(AgentSettings.Actions.CALIBRATE);
            turnBotDirection(AgentSettings.Direction.SOUTH);
            moveBot(AgentSettings.Actions.CALIBRATE);
            turnBotDirection(AgentSettings.Direction.WEST);
            moveBot(AgentSettings.Actions.CALIBRATE);
        }
        turnBotDirection(AgentSettings.Direction.NORTH);
        System.out.println("Went home");
    }

    /**
     * Do the BFS from start position and find those cells that have not been visited and can be reached by bot
     * @return ArrayList of qualified cells
     */
    protected ArrayList<Cell> findUnexploredAndCanVisit() {
        Cell curCell, topCell, rightCell;
        int curRow, curCol;
        Queue<Cell> queue= new LinkedList<>();
        HashSet<Cell> hasSeen = new HashSet<>();
        ArrayList<Cell> result = new ArrayList<>();

        curCell = exploredMap.getCell(1, 1);
        queue.add(curCell);
        hasSeen.add(curCell);
        while (queue.size() != 0) {
            curCell = queue.remove();
            curRow = curCell.getRow(); curCol = curCell.getCol();
            if (!curCell.isObstacle() && !curCell.isExplored() && !curCell.isVirtualWall()) result.add(curCell);

            if (curRow + 1 < MapSettings.MAP_ROWS && curCol < MapSettings.MAP_COLS) {
                topCell = exploredMap.getCell(curRow + 1, curCol);
                if (!hasSeen.contains(topCell)) {
                    hasSeen.add(topCell);
                    queue.add(topCell);
                }
            }

            if (curRow < MapSettings.MAP_ROWS && curCol + 1 < MapSettings.MAP_COLS) {
                rightCell = exploredMap.getCell(curRow, curCol + 1);
                if (!hasSeen.contains(rightCell)) {
                    hasSeen.add(rightCell);
                    queue.add(rightCell);
                }
            }
        }
        return result;
    }

    /**
     * Find all unexplored cell (can or cannot be reached by bot)
     * @return ArrayList of all unexplored cells
     */
    protected ArrayList<Cell> findUnexplored() {
        Cell curCell, topCell, rightCell;
        int curRow, curCol;
        Queue<Cell> queue= new LinkedList<>();
        HashSet<Cell> hasSeen = new HashSet<>();
        ArrayList<Cell> result = new ArrayList<>();

        curCell = exploredMap.getCell(1, 1);
        queue.add(curCell);
        hasSeen.add(curCell);
        while (queue.size() != 0) {
            curCell = queue.remove();
            curRow = curCell.getRow(); curCol = curCell.getCol();
            if (!curCell.isExplored() ) result.add(curCell);

            if (curRow + 1 < MapSettings.MAP_ROWS && curCol < MapSettings.MAP_COLS) {
                topCell = exploredMap.getCell(curRow + 1, curCol);
                if (!hasSeen.contains(topCell)) {
                    hasSeen.add(topCell);
                    queue.add(topCell);
                }
            }

            if (curRow < MapSettings.MAP_ROWS && curCol + 1 < MapSettings.MAP_COLS) {
                rightCell = exploredMap.getCell(curRow, curCol + 1);
                if (!hasSeen.contains(rightCell)) {
                    hasSeen.add(rightCell);
                    queue.add(rightCell);
                }
            }
        }
        return result;
    }

    /**
     * find the closest reachable cell that is not blocked by obstacle near the target cell
     * @return
     */
    protected Cell findSurroundingReachable(int row, int col) {
        boolean leftClear = true, rightClear = true, topClear = true, botClear = true;
        int offset = 1;
        Cell tmpCell;
        while (true) {
            // bot
            if (row - offset >= 0) {
                tmpCell = exploredMap.getCell(row - offset, col);
                if (tmpCell.isObstacle()) botClear = false;
                else if (botClear && !tmpCell.isObstacle() && !tmpCell.isVirtualWall()) return tmpCell;
            }

            // left
            if (col - offset >= 0) {
                tmpCell = exploredMap.getCell(row, col - offset);
                if (tmpCell.isObstacle()) leftClear = false;
                else if (leftClear && !tmpCell.isObstacle() && !tmpCell.isVirtualWall()) return tmpCell;
            }

            // right
            if (row + offset < MapSettings.MAP_ROWS) {
                tmpCell = exploredMap.getCell(row + offset, col);
                if (tmpCell.isObstacle()) rightClear = false;
                else if (rightClear && !tmpCell.isObstacle() && !tmpCell.isVirtualWall()) return tmpCell;
            }

            // top
            if (col + offset < MapSettings.MAP_COLS) {
                tmpCell = exploredMap.getCell(row, col + offset);
                if (tmpCell.isObstacle()) topClear = false;
                else if (topClear && !tmpCell.isObstacle() && !tmpCell.isVirtualWall()) return tmpCell;
            }

            offset++;
        }
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    protected boolean isExploredNotObstacle(int r, int c) {
//        System.out.println(exploredMap.getCell(r, c));
        if (exploredMap.checkValidCell(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.isExplored() && !tmp.isObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    protected boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkValidCell(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.isExplored() && !b.isVirtualWall() && !b.isObstacle());
        }
        return false;
    }

    /**
     * Returns the number of cells explored in the grid.
     */
    protected int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < MapSettings.MAP_ROWS; r++) {
            for (int c = 0; c < MapSettings.MAP_COLS; c++) {
                if (exploredMap.getCell(r, c).isExplored()) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Moves the bot, repaints the map and calls senseAndRepaint().
     */
    protected void moveBot(AgentSettings.Actions m) {
//        System.out.println("[Agent Dir] " + bot.getAgtDir());
        System.out.println("Action executed: " + m);
        bot.takeAction(m, 1, exploredMap, realMap);
        senseAndRepaint();

        // TODO calibration
//        if (m != MOVEMENT.CALIBRATE) {
//            senseAndRepaint();
//        } else {
//            CommMgr commMgr = CommMgr.getCommMgr();
//            commMgr.recvMsg();
//        }
//        if (bot.getRealBot() && !calibrationMode) {
//            calibrationMode = true;
//
//            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
//                lastCalibrate = 0;
//                moveBot(MOVEMENT.CALIBRATE);
//            } else {
//                lastCalibrate++;
//                if (lastCalibrate >= 5) {
//                    DIRECTION targetDir = getCalibrationDirection();
//                    if (targetDir != null) {
//                        lastCalibrate = 0;
//                        calibrateBot(targetDir);
//                    }
//                }
//            }
//
//            calibrationMode = false;
//        }
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    protected void senseAndRepaint() {
        bot.setSensors();
        bot.senseEnv(exploredMap, realMap);
        exploredMap.repaint();
    }

    // TODO
    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
//    protected boolean canCalibrateOnTheSpot(Direction botDir) {
//        int row = bot.getRobotPosRow();
//        int col = bot.getRobotPosCol();
//
//        switch (botDir) {
//            case NORTH:
//                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
//            case EAST:
//                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
//            case SOUTH:
//                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
//            case WEST:
//                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
//        }
//
//        return false;
//    }

    // TODO
    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
//    protected Direction getCalibrationDirection() {
//        DIRECTION origDir = bot.getRobotCurDir();
//        DIRECTION dirToCheck;
//
//        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        return null;
//    }

    // TODO
    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
//    protected void calibrateBot(Direction targetDir) {
//        DIRECTION origDir = bot.getRobotCurDir();
//
//        turnBotDirection(targetDir);
//        moveBot(MOVEMENT.CALIBRATE);
//        turnBotDirection(origDir);
//    }

    // TODO
    /**
     * Turns the robot to the required direction.
     */
    protected void turnBotDirection(AgentSettings.Direction targetDir) {
        int numOfTurn = Math.abs(bot.getAgtDir().ordinal() - targetDir.ordinal()) / 2;
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (AgentSettings.Direction.clockwise90(bot.getAgtDir()) == targetDir) {
                moveBot(AgentSettings.Actions.FACE_RIGHT);
            } else {
                moveBot(AgentSettings.Actions.FACE_LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(AgentSettings.Actions.FACE_RIGHT);
            moveBot(AgentSettings.Actions.FACE_RIGHT);
        }
    }

    protected long getElapsedTime(long startTime, long currentTime) {
        if (bot.isSim()) return (currentTime - startTime) * SimulatorSettings.SIM_ACCELERATION;
        else return (currentTime - startTime);
    }

    protected long getEndTime(long startTime, int timeLimit) {
        if (bot.isSim()) return startTime + (timeLimit / SimulatorSettings.SIM_ACCELERATION * 1000);
        else return startTime + (timeLimit * 1000);
    }
}
