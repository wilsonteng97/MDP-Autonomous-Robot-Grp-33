package logic.exploration;

import hardware.Agent;
import hardware.AgentSettings;
import map.Map;
import map.MapSettings;
import map.Cell;
import hardware.AgentSettings.*;
import logic.fastestpath.AStarHeuristicSearch;

import java.util.Scanner;

public class RightWallHugging extends ExplorationAlgo {
    private final Map exploredMap;
    private final Map realMap;
    private final Agent bot;
    private final int coverageLimit; // TODO check usage
    private final int timeLimit;    // in second
    private int areaExplored;
    private long startTime; // in millisecond
    private long endTime;   // in millisecond

    public RightWallHugging(Map exploredMap, Map realMap, Agent bot, int coverageLimit, int timeLimit) {
        super(exploredMap, realMap, bot, coverageLimit, timeLimit);
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    public void runExploration() {
        // FIXME check for real bot connection
//        if (bot.getRealBot()) {
//            System.out.println("Starting calibration...");
//
//            CommMgr.getCommMgr().recvMsg();
//            if (bot.getRealBot()) {
//                bot.move(MOVEMENT.LEFT, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.LEFT, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.RIGHT, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.RIGHT, false);
//            }
//
//            while (true) {
//                System.out.println("Waiting for EX_START...");
//                String msg = CommMgr.getCommMgr().recvMsg();
//                String[] msgArr = msg.split(";");
//                if (msgArr[0].equals(CommMgr.EX_START)) break;
//            }
//        }

        // Explore
        System.out.println("Starting exploration...");

        // prepare for timing
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        areaExplored = calculateAreaExplored();
        System.out.println("Explored Area: " + areaExplored);

        explorationLoop(bot.getAgtY(), bot.getAgtX());

//        if (bot.getRealBot()) {
//            CommMgr.getCommMgr().sendMsg(null, CommMgr.BOT_START);
//        }
//        senseAndRepaint();
        senseAndRepaint();
    }

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int r, int c) {
        do {
            nextMove();
            System.out.printf("Current Bot Pos: [%d, %d]\n", bot.getAgtX(), bot.getAgtY());

            areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);
            System.out.println();

            if (bot.getAgtY() == r && bot.getAgtX() == c) {
                if (areaExplored >= 100) {
                    break;
                }
            }
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        } while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);

        goHome();
    }

    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    private void nextMove() {
        System.out.println("Bot Direction: " + bot.getAgtDir());
        if (lookRight()) {
            System.out.println("Right Clear");
            moveBot(Actions.FACE_RIGHT);
            if (lookForward()) {
                System.out.println("  ->Forward Clear");
                moveBot(Actions.FORWARD);
            }
        } else if (lookForward()) {
            System.out.println("Forward Clear");
            moveBot(Actions.FORWARD);
        } else if (lookLeft()) {
            System.out.println("Left Clear");
            moveBot(Actions.FACE_LEFT);
            if (lookForward()) {
                System.out.println("  ->Forward Clear");
                moveBot(Actions.FORWARD);
            }
        } else {
            System.out.println("Reverse Direction");
            moveBot(Actions.FACE_RIGHT);
            moveBot(Actions.FACE_RIGHT);
        }
    }

    /**
     * Returns true if the right side of the robot is free to move into.
     */
    private boolean lookRight() {
        System.out.println("[Function executed] lookRight()");
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
    private boolean lookForward() {
        System.out.println("[Function executed] lookForward()");
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
    private boolean lookLeft() {
        System.out.println("[Function executed] lookLeft()");
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
    private boolean northFree() {
        System.out.println("[Function executed] northFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        System.out.println("[Function executed] eastFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        System.out.println("[Function executed] southFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        System.out.println("[Function executed] westFree()");
        int botRow = bot.getAgtY();
        int botCol = bot.getAgtX();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Send the bot to START and points the bot northwards
     */
    private void goHome() {
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

        // TODO realbot
//        if (bot.getRealBot()) {
//            turnBotDirection(DIRECTION.WEST);
//            moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.SOUTH);
//            moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.WEST);
//            moveBot(MOVEMENT.CALIBRATE);
//        }
        turnBotDirection(Direction.NORTH);
        System.out.println("Went home");
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredNotObstacle(int r, int c) {
        System.out.println(exploredMap.getCell(r, c));
        if (exploredMap.checkValidCell(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.isExplored() && !tmp.isObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        System.out.println(exploredMap.getCell(r, c));
        if (exploredMap.checkValidCell(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.isExplored() && !b.isVirtualWall() && !b.isObstacle());
        }
        return false;
    }

    /**
     * Returns the number of cells explored in the grid.
     */
    private int calculateAreaExplored() {
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
    private void moveBot(Actions m) {
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

        // TODO realbot
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
        // TODO
        /**
         * Sets the bot's sensors, processes the sensor data and repaints the map.
         */
    private void senseAndRepaint() {
        bot.setSensors();
        bot.senseEnv(exploredMap, realMap);
        exploredMap.repaint();
    }

        // TODO
        /**
         * Checks if the robot can calibrate at its current position given a direction.
         */
//    private boolean canCalibrateOnTheSpot(Direction botDir) {
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
//    private Direction getCalibrationDirection() {
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
//    private void calibrateBot(Direction targetDir) {
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
    private void turnBotDirection(Direction targetDir) {
        int numOfTurn = Math.abs(bot.getAgtDir().ordinal() - targetDir.ordinal()) / 2;
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (Direction.clockwise90(bot.getAgtDir()) == targetDir) {
                moveBot(Actions.FACE_RIGHT);
            } else {
                moveBot(Actions.FACE_LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(Actions.FACE_RIGHT);
            moveBot(Actions.FACE_RIGHT);
        }
    }
}
