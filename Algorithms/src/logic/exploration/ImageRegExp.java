package logic.exploration;

import hardware.Agent;
import hardware.AgentSettings;
import hardware.AgentSettings.Actions;
import logic.fastestpath.AStarHeuristicSearch;
import map.ArenaMap;
import map.Cell;
import map.MapSettings;
import map.ObsSurface;
import network.NetworkMgr;
import utils.MapDescriptorFormat;

import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static hardware.AgentSettings.Direction.*;
import static hardware.AgentSettings.Direction.SOUTH;
import static utils.MsgParsingUtils.parseFastestPathString;
import static utils.MsgParsingUtils.parsePictureMsg;
import static utils.SimulatorSettings.GOHOMESLOW_SLEEP;
import static utils.SimulatorSettings.SIM;

public class ImageRegExp extends ExplorationAlgo {
    private static final Logger LOGGER = Logger.getLogger(ImageRegExp.class.getName());

    // for image reg
    private static LinkedHashMap<String, ObsSurface> notYetTaken;

    public ImageRegExp(ArenaMap exploredArenaMap, ArenaMap realArenaMap, Agent bot, int coverageLimit, int timeLimit) {
        super(exploredArenaMap, realArenaMap, bot, coverageLimit, timeLimit);
    }

    @Override
    public void runExploration() {
        System.out.println("[DEBUG: runExploration] executed");
        if (!bot.isSim()) {
            System.out.println("Starting calibration...");

            if (!bot.isSim()) {
                // Facing NORTH
                turnBotDirection(AgentSettings.Direction.WEST);
                moveBot(Actions.ALIGN_FRONT);
                turnBotDirection(AgentSettings.Direction.SOUTH);
                moveBot(Actions.ALIGN_FRONT);
                turnBotDirection(AgentSettings.Direction.EAST);
            }

            while (true) {
                System.out.println("Waiting for ES|...");
                String msg = NetworkMgr.getInstance().receiveMsg();
//                String[] msgArr = msg.split("\\|");
                if (msg.equals(NetworkMgr.EXP_START)) break;
            }
        }

        // Explore
        System.out.println("Starting exploration...");

        // prepare for timing
        startTime = System.currentTimeMillis();

        areaExplored = calculateAreaExplored();
        System.out.println("Starting state - area explored: " + areaExplored);
        System.out.println();

        explorationLoop(bot.getAgtY(), bot.getAgtX());
        System.out.println("Test image start");
        imageExploration();
        LOGGER.info("Test image end | going home now");
//        scanner.nextLine();
        goHome();
        LOGGER.info("Test image end | goHome() end, aligning for FP now");
//        scanner.nextLine();
        alignBeforeFastestPath();
        LOGGER.info("FP alignment end");
//        scanner.nextLine();
        exploredArenaMap.repaint();
    }

    @Override
    protected void explorationLoop(int r, int c) {
        System.out.println("[coverageLimit + timeLimit] " + coverageLimit + " | " + timeLimit);

        // initialization for tracking notYetTaken surfaces
        notYetTaken = getUnTakenSurfaces();
        System.out.println(notYetTaken);

        long elapsedTime = 0;
        do {
            if (!bot.isSim()) NetworkMgr.getInstance().sendMsg("Z", NetworkMgr.INSTRUCTIONS);
            senseAndRepaint();
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
            elapsedTime = getElapsedTime();
//            scanner.nextLine();
            System.out.println("[doWhile loop elapsed time] " + getElapsedTime());
        } while (areaExplored <= coverageLimit && elapsedTime < timeLimit);

        if (areaExplored == 300) {
            goHome();
        } else if ((areaExplored >= coverageLimit && areaExplored < 300) || (elapsedTime >= timeLimit && areaExplored < 300)) {
            // Exceed coverage or time limit

            elapsedTime = getElapsedTime();
            if (areaExplored >= coverageLimit) System.out.printf("Reached coverage limit, successfully explored %d grids\n", areaExplored);
            if (elapsedTime >= timeLimit) System.out.printf("Reached time limit, exploration has taken %d millisecond(ms)\n", elapsedTime);

            if (SIM) {
                System.out.println("Arena not fully explored, goHomeSlow() option can be chosen, enter \"Y\" or \"y\" to continue: ");
                String userInput = scanner.nextLine();
                if (userInput.toLowerCase().equals("y")) {
                    try {
                        System.out.println("[explorationLoop()] Agent sleeping for " + GOHOMESLOW_SLEEP/1000 + " second(s) before executing goHomeSlow()");
                        TimeUnit.MILLISECONDS.sleep(GOHOMESLOW_SLEEP);
                    } catch (InterruptedException e) {
                        System.out.println("[explorationLoop()] Sleeping interruption exception");
                    }
                    goHomeSlow();
                } else {
                    System.out.println("goHomeSlow() option not chosen, robot will be stationary.");
                }
            } else {
                goHomeSlow();
            }

        } else {
            // have unexplored cells, visit surrounding cells of those unvisited cells
            goHome();

            System.out.printf("Current Bot Pos: [%d, %d]\n", bot.getAgtX(), bot.getAgtY());

            AStarHeuristicSearch keepExploring;
            ArrayList<Cell> unExploredCells;
            Cell destCell;
            unExploredCells = findUnexplored();
            int i = 0;
            while (!unExploredCells.isEmpty()) {
                int targetRow, targetCol;
                System.out.println("Unexplored cells: " + unExploredCells.size());
                Cell targetCell = unExploredCells.get(i);
                targetRow = targetCell.getRow();
                targetCol = targetCell.getCol();
                destCell = findSurroundingReachable(targetRow, targetCol);
                if (destCell != null) {
                    System.out.println(destCell);
                    keepExploring = new AStarHeuristicSearch(exploredArenaMap, bot, realArenaMap);
                    keepExploring.runFastestPath(destCell.getRow(), destCell.getCol());
                    i = 0;
                } else {
                    i++;
                }

                elapsedTime = getElapsedTime();
                if (areaExplored >= coverageLimit) {
                    System.out.printf("Reached coverage limit, successfully explored %d grids\n", areaExplored);
                    break;
                }
                if (elapsedTime >= timeLimit) {
                    System.out.printf("Reached time limit, exploration has taken %d millisecond(ms)\n", elapsedTime);
                    break;
                }

                if (i == 0) unExploredCells = findUnexplored();
            }
            goHome();
        }
        System.out.println("Exploration Completed!");
    }

    public void imageExploration() {
        notYetTaken = getUnTakenSurfaces();
        System.out.println(notYetTaken);
        if (notYetTaken.size() == 0) {
            return;
        }

        // get all untaken surfaces
        while (notYetTaken.size() > 0) {
            System.out.println("imageLoop start " + notYetTaken.size() + ":\n" + notYetTaken);
            imageLoop();
            System.out.println("imageLoop end " + notYetTaken.size() + ":\n" + notYetTaken);
        }

        goHome();
    }

    private void imageLoop() {
        boolean success;
        ArrayList<ObsSurface> surfTaken;
        System.out.println("before nearestObsSurface");
//        scanner.nextLine();
        ObsSurface nearestObstacle = nearestObsSurface(bot.getAgtPos(), notYetTaken);
        LOGGER.info("nearestObstacle after " + nearestObstacle);
        Cell nearestCell = findSurfaceSurroundingReachable(nearestObstacle.getRow(), nearestObstacle.getCol(), nearestObstacle.getSurface());
        LOGGER.info("nearestCell | " + nearestCell.toString());
        if (nearestCell != null) {
            // go to nearest cell
            success = goToPointTakePicture(nearestCell.getCoord(), nearestObstacle);
            System.out.println("success: " + success);
        }
        removeFromNotYetTaken(nearestObstacle);
        System.out.println("after nearestObsSurface");
//        scanner.nextLine();
    }

    private boolean goToPointTakePicture(Point loc, ObsSurface obsSurface) {
        Point before = bot.getAgtPos();
        goToPoint(loc);
        Point after = bot.getAgtPos();
        if (before == after) return false;
        LinkedHashMap<String, Point> obsList;

//        System.out.println("imageRecognitionRight before");
//        obsList = imageRecognitionRight(exploredArenaMap);
//        System.out.println("imageRecognitionRight after");

//        scanner.nextLine();


        AgentSettings.Direction desiredDir = AgentSettings.Direction.antiClockwise90(obsSurface.getSurface());
        if (desiredDir == bot.getAgtDir()) {
            LOGGER.info("desiredDir before | no change" + bot.getAgtDir());
        } else {
            LOGGER.info("Not desiredDir before " + bot.getAgtDir());
            LOGGER.info("desiredDir" + desiredDir);
            turnBotDirectionWithoutSense(desiredDir);
        }
        System.out.println("desired dir after " + bot.getAgtDir());
        obsList = imageRecognitionRight(exploredArenaMap, false);
//        scanner.nextLine();

        return true;
    }

    /**
     * Turns the robot to the required direction.
     */
    protected void turnBotDirectionWithoutSense(AgentSettings.Direction targetDir) {
        int numOfTurn = Math.abs(bot.getAgtDir().ordinal() - targetDir.ordinal()) / 2;
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (AgentSettings.Direction.clockwise90(bot.getAgtDir()) == targetDir) {
                bot.takeAction(Actions.FACE_RIGHT, 0, exploredArenaMap, realArenaMap);
//                senseAndRepaint();
            } else {
                bot.takeAction(Actions.FACE_LEFT, 0, exploredArenaMap, realArenaMap);
//                senseAndRepaint();
            }
        } else if (numOfTurn == 2) {
            bot.takeAction(Actions.FACE_RIGHT, 0, exploredArenaMap, realArenaMap);
//            senseAndRepaint();
            bot.takeAction(Actions.FACE_RIGHT, 0, exploredArenaMap, realArenaMap);
//            senseAndRepaint();
        }

    }

    private LinkedHashMap<String, Point> imageRecognitionRight(ArenaMap exploredArenaMap, boolean debug) {
        ArrayList<ObsSurface> surfTaken = bot.returnSurfacesTakenRight(exploredArenaMap);
        updateNotYetTaken(surfTaken);
        LinkedHashMap<String, Point> obsList = bot.returnObsRight(exploredArenaMap);
//        senseAndRepaint();
        if (debug) {
            System.out.println("Before repaintWithoutSense");
            scanner.nextLine();
            repaintWithoutSense();
            scanner.nextLine();
            System.out.println("After repaintWithoutSense");
        }
        takePicture(obsList.getOrDefault("L", null),
                    obsList.getOrDefault("M", null),
                    obsList.getOrDefault("R", null));
        return obsList;
    }

    private void repaintWithoutSense() {
        exploredArenaMap.repaint();
    }

    private LinkedHashMap<String, ObsSurface> getUnTakenSurfaces() {
        LinkedHashMap<String, ObsSurface> notYetTaken;

        // get all possible obstacle surfaces
        notYetTaken = getAllPossibleObsSurfaces();
        for (String tempObsSurfaceStr : bot.getSurfaceTaken().keySet()) {
            if (!notYetTaken.containsKey(tempObsSurfaceStr)) {
                System.out.println("========================================");
                LOGGER.warning("Surface taken not in all possible surfaces. Please check. " + tempObsSurfaceStr);
                System.out.println("========================================");
            }
            else {
                notYetTaken.remove(tempObsSurfaceStr);
            }
        }
        System.out.println("notYetTaken | " + notYetTaken.size() + "\n" + notYetTaken);
        return notYetTaken;
    }

    private void updateNotYetTaken(ArrayList<ObsSurface> surfTaken) {
//        LOGGER.info("[update] method called " + surfTaken);
        for (ObsSurface obsSurface : surfTaken) {
//            LOGGER.info("[update] obsSurface " + obsSurface);
            if (notYetTaken.containsKey(obsSurface.toString())) {
                notYetTaken.remove(obsSurface.toString());
                LOGGER.info("[update] Remove from not yet taken: " + obsSurface.toString());
            }
        }
    }

    private void removeFromNotYetTaken(ObsSurface obsSurface) {
        if (notYetTaken.containsKey(obsSurface.toString())) {
            notYetTaken.remove(obsSurface.toString());
            LOGGER.info("[remove] Remove from not yet taken: " + obsSurface.toString());
        }
    }

    private LinkedHashMap<String, ObsSurface> getAllPossibleObsSurfaces() {
        Cell tempCell; Cell neighTempCell;
        ObsSurface tempObsSurface;
        LinkedHashMap<AgentSettings.Direction, Cell> tempNeighbours;
        LinkedHashMap<String, ObsSurface> allPossibleObsSurfaces = new LinkedHashMap<String, ObsSurface>();

        for (int r = 0; r < MapSettings.MAP_ROWS; r++) {
            for (int c = 0; c < MapSettings.MAP_COLS; c++) {
                tempCell = exploredArenaMap.getCell(r, c);
                if (tempCell.isObstacle()) {
                    System.out.println("tempCell" + tempCell);
                    tempNeighbours = exploredArenaMap.getNeighboursHashMap(tempCell);
                    System.out.println("tempNeighbours" + tempNeighbours);
                    for (AgentSettings.Direction nDir : tempNeighbours.keySet()) {
                        neighTempCell = tempNeighbours.get(nDir);
//                        System.out.println("neighTempCell" + neighTempCell);

                        if (!neighTempCell.isObstacle()) {
                            tempObsSurface = new ObsSurface(neighTempCell.getCoord(), nDir);
                            if (isSurfaceReachable(tempObsSurface, exploredArenaMap)) {
//                                System.out.println("tempObsSusrface" + tempObsSurface);
                                allPossibleObsSurfaces.put(tempObsSurface.toString(), tempObsSurface);
                            } else {
                                LOGGER.info("[getAllPossibleObsSurfaces] Unreachable surface removed" + tempObsSurface.toString());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("allPossibleObsSurfaces | " + allPossibleObsSurfaces.size() + " :\n" + allPossibleObsSurfaces);
        return allPossibleObsSurfaces;
    }

    public boolean isSurfaceReachable(ObsSurface obsSurface, ArenaMap exploredArenaMap) {
        int row = 0; int col = 0;
        int rowInc = 0; int colInc = 0;
        int obsX = obsSurface.getCol(); int obsY = obsSurface.getRow();

        switch (obsSurface.getSurface()) {
            case NORTH:
                rowInc = -1; colInc = 0;
                break;
            case SOUTH:
                rowInc = 1; colInc = 0;
                break;
            case WEST:
                rowInc = 0; colInc = 1;
                break;
            case EAST:
                rowInc = 0; colInc = -1;
                break;
        }

        for (int offset = AgentSettings.CAMERA_MIN; offset <= AgentSettings.CAMERA_MAX + 1; offset++) {
            row = obsY + rowInc * offset;
            col = obsX + colInc * offset;
            // left/right reachable
            if (rowInc==0) row++; if (colInc==0) col++;
            if (exploredArenaMap.checkRobotFitsCell(row, col)) return true;

            // Mid reachable
            if (rowInc==0) row--; if (colInc==0) col--;
            if (exploredArenaMap.checkRobotFitsCell(row, col)) return true;

            // left/right reachable
            if (rowInc==0) row--; if (colInc==0) col--;
            if (exploredArenaMap.checkRobotFitsCell(row, col)) return true;
        }

        return false;
    }

    public ObsSurface nearestObsSurface(Point loc, LinkedHashMap<String, ObsSurface> notYetTaken) {
        Point tempObsPoint = null;
        int tempX; int tempY;
        ObsSurface tempSurface;
        AgentSettings.Direction tempDir;
        ArrayList<ObsSurface> tempSurfaces;
        ArrayList<ObsSurface> tempObsSurfaceList = new ArrayList<ObsSurface>();
        ObsSurface currentCameraMiddleSurface = bot.getCameraMiddleSurface();
        LOGGER.info("currentCameraMiddleSurface " + currentCameraMiddleSurface.toString());

        // check if current surfaces facing camera have different directions (but not opposite).
        tempSurface = currentCameraMiddleSurface.getSideSurfaces(1, bot.getAgtDir()).get(1);
        tempDir = tempSurface.getSurface();
        tempSurface.setSurface(antiClockwise90(tempDir));
        LOGGER.info("tempSurface | " + 1 + " " + tempSurface.toString());
        if (notYetTaken.containsKey(tempSurface.toString())) {
            LOGGER.info("=============== [side surfaces | " + "offset = " + 1 + "] For " + loc + "\n" + notYetTaken + "\nos return " + tempSurface + "\n===============");
            return tempSurface;
        }
        tempSurface.setSurface(reverse(tempDir));
        LOGGER.info("tempSurface | " + 1 + " " + tempSurface.toString());
        if (notYetTaken.containsKey(tempSurface.toString())) {
            LOGGER.info("=============== [side surfaces | " + "offset = " + 1 + "] For " + loc + "\n" + notYetTaken + "\nos return " + tempSurface + "\n===============");
            return tempSurface;
        }

        // check for surfaces at the corner (if any)
        tempSurface = bot.getAgentDiagonalRightSurface();
        LOGGER.info("tempSurface | " + 1 + " " + tempSurface.toString());
        if (notYetTaken.containsKey(tempSurface.toString())) {
            LOGGER.info("=============== [diagonal surfaces] For " + loc + "\n" + notYetTaken + "\nos return " + tempSurface + "\n===============");
            return tempSurface;
        }


        // check 3, then 2 blocks away from currentCameraMiddleSurface in same dir as agent
        for (int offset = 3; offset >= 2; offset--) {
            tempSurface = currentCameraMiddleSurface.getSideSurfaces(offset, bot.getAgtDir()).get(0);
            LOGGER.info("tempSurfaces | " + offset + " " + tempSurface.toString());
            if (notYetTaken.containsKey(tempSurface.toString())) {
                LOGGER.info("=============== [side surfaces | " + "offset = " + offset + "] For " + loc + "\n" + notYetTaken + "\nos return " + tempSurface + "\n===============");
                return tempSurface;
            }
        }



        return absoluteNearestSurface(loc, notYetTaken);
    }

    public ObsSurface absoluteNearestSurface(Point loc, LinkedHashMap<String, ObsSurface> notYetTaken) {
        double dist = 1000, tempDist;
        Point tempPos;
        ObsSurface nearest = null;

        for (ObsSurface obstacle: notYetTaken.values()) {
//            tempPos = obstacle.getPos();
            // neighbour cell of that surface
            tempPos = getNeighbour(obstacle.getPos(), obstacle.getSurface());
            tempDist = loc.distance(tempPos);
            if (tempDist < dist) {
                dist = tempDist;
                nearest = obstacle;
            }
        }
        LOGGER.info("-------------- [absoluteNearestSurface] For " + loc + "\n" + notYetTaken + "\n--------------");
        return nearest;
    }

    public Point getNeighbour(Point pos, AgentSettings.Direction surfaceDir) {
        switch (surfaceDir) {
            case NORTH:
                return new Point(pos.x , pos.y + 1);
            case SOUTH:
                return new Point(pos.x, pos.y - 1);
            case WEST:
                return new Point(pos.x - 1, pos.y);
            case EAST:
                return new Point(pos.x + 1, pos.y);
        }
        return null;
    }

    public void takePicture(Point leftObs, Point middleObs, Point rightObs) {
        if ((leftObs==null) && (middleObs==null) && (rightObs==null)) return;

        String msg = parsePictureMsg(leftObs, middleObs, rightObs);

        if (!bot.isSim()) {
            NetworkMgr comm = NetworkMgr.getInstance();
            comm.sendMsg(msg, NetworkMgr.INSTRUCTIONS);
        }
        System.out.println("Taking image: " + msg);
    }

    /**
     * Determines the next move for the robot and executes it accordingly and take picture
     */
    @Override
    protected void nextMove() {
        System.out.println("Bot Direction: " + bot.getAgtDir());
        LinkedHashMap<String, Point> obsList;
        if (lookRight()) {
//            System.out.println("[DEBUG] Right Clear");
            moveBot(Actions.FACE_RIGHT);
            obsList = imageRecognitionRight(exploredArenaMap, false);
            if (lookForward()) {
//                System.out.println("  ->[DEBUG]Forward Clear");
                moveBot(Actions.FORWARD);
                obsList = imageRecognitionRight(exploredArenaMap, false);
            }
        } else if (lookForward()) {
//            System.out.println("[DEBUG]Forward Clear");
            moveBot(Actions.FORWARD);
            obsList = imageRecognitionRight(exploredArenaMap, false);
        } else if (lookLeft()) {
//            System.out.println("[DEBUG]Left Clear");
            moveBot(Actions.FACE_LEFT);
            obsList = imageRecognitionRight(exploredArenaMap, false);
            if (lookForward()) {
//                System.out.println("  ->[DEBUG]Forward Clear");
                moveBot(Actions.FORWARD);
                obsList = imageRecognitionRight(exploredArenaMap, false);
            }
        } else {
//            System.out.println("[DEBUG]Reverse Direction");
            moveBot(Actions.FACE_LEFT);
            obsList = imageRecognitionRight(exploredArenaMap, false);
            moveBot(Actions.FACE_LEFT);
            obsList = imageRecognitionRight(exploredArenaMap, false);
        }
        System.out.println("New Bot Direction: " + bot.getAgtDir());

        if (!bot.isSim()) {
            String[] MDFString = MapDescriptorFormat.generateMapDescriptorFormat(exploredArenaMap);
            String msg = MDFString[0] + ":" + MDFString[1] + "|";
            NetworkMgr.getInstance().sendMsg(msg, NetworkMgr.MAP_STRINGS);
        }
    }

    /**
     * find the closest reachable position that is not blocked by obstacle near the target surface
     * Assumption : surface is known to be reachable.
     * @return
     */
    protected Cell findSurfaceSurroundingReachable(int surfaceRow, int surfaceCol, AgentSettings.Direction dir) {
//        super.findSurroundingReachable(surfaceRow, surfaceCol);
        boolean leftClear = true, rightClear = true, topClear = true, botClear = true;
        int offset = 1;
        int rowInc = 0, colInc = 0;
        Cell tempCell;
        int tempFreeX = 0; int tempFreeY = 0;

        switch(dir) {
            case NORTH :
                rowInc = -1; colInc = 0;
                break;
            case SOUTH:
                rowInc = 1; colInc = 0;
                break;
            case EAST:
                rowInc = 0; colInc = -1;
                break;
            case WEST:
                rowInc = 0; colInc = 1;
                break;
        }

        while (true) {
            tempFreeX = surfaceCol + offset * colInc;
            tempFreeY = surfaceRow + offset * rowInc;

            // Left/Right displacement
            if (rowInc==0) tempFreeY--; if (colInc==0) tempFreeX--;
            tempCell = exploredArenaMap.getCell(tempFreeY, tempFreeX);
            if (exploredArenaMap.checkRobotFitsCell(tempFreeY, tempFreeX) && tempCell.isExplored()) {
                return tempCell;
            }

            // No displacement (middle)
            if (rowInc==0) tempFreeY++; if (colInc==0) tempFreeX++;
            tempCell = exploredArenaMap.getCell(tempFreeY, tempFreeX);
            if (exploredArenaMap.checkRobotFitsCell(tempFreeY, tempFreeX) && tempCell.isExplored()) {
                return tempCell;
            }

            // Left/Right displacement
            if (rowInc==0) tempFreeY++; if (colInc==0) tempFreeX++;
            tempCell = exploredArenaMap.getCell(tempFreeY, tempFreeX);
            if (exploredArenaMap.checkRobotFitsCell(tempFreeY, tempFreeX) && tempCell.isExplored()) {
                return tempCell;
            }

            if (offset>=5) {
                LOGGER.warning("Surface is unreachable!!");
                return new Cell(new Point(MapSettings.START_COL, MapSettings.START_ROW));
            }
            offset++;
        }
    }

    public static <K extends Comparable, V> Map<K,V> sortByKeys(Map<K,V> map) {
        return new TreeMap<>(map);
    }
}
