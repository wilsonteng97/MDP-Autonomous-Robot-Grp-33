package logic.exploration;

import hardware.Agent;
import hardware.AgentSettings;
import hardware.AgentSettings.Actions;
import map.Cell;
import map.Map;
import map.MapSettings;
import map.ObsSurface;
import network.NetworkMgr;
import utils.MapDescriptorFormat;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class ImageRegExp extends ExplorationAlgo {
    private static final Logger LOGGER = Logger.getLogger(ImageRegExp.class.getName());

    // for image reg
    private static HashMap<String, ObsSurface> notYetTaken;

    public ImageRegExp(Map exploredMap, Map realMap, Agent bot, int coverageLimit, int timeLimit) {
        super(exploredMap, realMap, bot, coverageLimit, timeLimit);
    }

    @Override
    public void runExploration() {
        System.out.println("[DEBUG: runExploration] executed");
        if (!bot.isSim()) {
            System.out.println("Starting calibration...");

            if (!bot.isSim()) {
                // TODO initial calibration
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
        System.out.println("Test image end");
        exploredMap.repaint();
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

        goToPoint(new Point (MapSettings.START_COL, MapSettings.START_ROW));
    }

    private void imageLoop() {
        boolean success;
        ArrayList<ObsSurface> surfTaken;

        ObsSurface nearestObstacle = nearestObsSurface(bot.getAgtPos(), notYetTaken);
        Cell nearestCell = findSurroundingReachable(nearestObstacle.getRow(), nearestObstacle.getCol());

        if (nearestCell != null) {
            // go to nearest cell
            success = goToPointTakePicture(nearestCell.getCoord(), nearestObstacle);
            System.out.println("success: " + success);
        }
        removeFromNotYetTaken(nearestObstacle);
    }

    private boolean goToPointTakePicture(Point loc, ObsSurface obsSurface) {
        Point before = bot.getAgtPos();
        goToPoint(loc);
        Point after = bot.getAgtPos();
        if (before == after) return false;
        HashMap<String, Point> obsList;

        System.out.println("imageRecognitionRight before");
        obsList = imageRecognitionRight(exploredMap);
        System.out.println("imageRecognitionRight after");

        AgentSettings.Direction desiredDir = AgentSettings.Direction.antiClockwise90(obsSurface.getSurface());
        if (desiredDir == bot.getAgtDir()) {
            LOGGER.info("desiredDir" + bot.getAgtDir());
            return true;
        }
        else if (desiredDir == AgentSettings.Direction.clockwise90(bot.getAgtDir())) {
            LOGGER.info("desiredDir clockwise " + bot.getAgtDir());
            moveBot(AgentSettings.Actions.FACE_RIGHT);
            obsList = imageRecognitionRight(exploredMap);
        }
        else if (desiredDir == AgentSettings.Direction.antiClockwise90(bot.getAgtDir())) {
            LOGGER.info("desiredDir anticlockwise" + bot.getAgtDir());
            moveBot(AgentSettings.Actions.FACE_LEFT);
            obsList = imageRecognitionRight(exploredMap);
        }
        // opposite
        else {
            System.out.println("desiredDir opposite" + bot.getAgtDir());
            moveBot(AgentSettings.Actions.FACE_LEFT);
            obsList = imageRecognitionRight(exploredMap);

            moveBot(AgentSettings.Actions.FACE_LEFT);
            obsList = imageRecognitionRight(exploredMap);
        }
        return true;
    }

    private HashMap<String, Point> imageRecognitionRight(Map exploredMap) {
        ArrayList<ObsSurface> surfTaken = bot.returnSurfacesTakenRight(exploredMap);
        updateNotYetTaken(surfTaken);
        HashMap<String, Point> obsList = bot.returnObsRight(exploredMap);
        senseAndRepaint();
        takePicture(obsList.getOrDefault("L", null),
                    obsList.getOrDefault("M", null),
                    obsList.getOrDefault("R", null));
        return obsList;
    }

    private HashMap<String, ObsSurface> getUnTakenSurfaces() {
        HashMap<String, ObsSurface> notYetTaken;

        // get all possible obstacle surfaces
        notYetTaken = getAllPossibleObsSurfaces();
        for (String tempObsSurfaceStr : bot.getSurfaceTaken().keySet()) {
            if (!notYetTaken.containsKey(tempObsSurfaceStr)) {
                LOGGER.warning("Surface taken not in all possible surfaces. Please check. \n\n\n");
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

    private HashMap<String, ObsSurface> getAllPossibleObsSurfaces() {
        Cell tempCell; Cell neighTempCell;
        ObsSurface tempObsSurface;
        HashMap<AgentSettings.Direction, Cell> tempNeighbours;
        HashMap<String, ObsSurface> allPossibleObsSurfaces = new HashMap<String, ObsSurface>();

        for (int r = 0; r < MapSettings.MAP_ROWS; r++) {
            for (int c = 0; c < MapSettings.MAP_COLS; c++) {
                tempCell = exploredMap.getCell(r, c);
                if (tempCell.isObstacle()) {
                    System.out.println("tempCell" + tempCell);
                    tempNeighbours = exploredMap.getNeighboursHashMap(tempCell);
                    System.out.println("tempNeighbours" + tempNeighbours);
                    for (AgentSettings.Direction nDir : tempNeighbours.keySet()) {
                        neighTempCell = tempNeighbours.get(nDir);
//                        System.out.println("neighTempCell" + neighTempCell);

                        if (!neighTempCell.isObstacle()) {
                            tempObsSurface = new ObsSurface(neighTempCell.getCoord(), nDir);
//                            System.out.println("tempObsSusrface" + tempObsSurface);
                            allPossibleObsSurfaces.put(tempObsSurface.toString(), tempObsSurface);
                        }
                    }
                }
            }
        }
        System.out.println("allPossibleObsSurfaces | " + allPossibleObsSurfaces.size() + " :\n" + allPossibleObsSurfaces);
        return allPossibleObsSurfaces;
    }

    public ObsSurface nearestObsSurface(Point loc, HashMap<String, ObsSurface> notYetTaken) {
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

        String msg = AgentSettings.Actions.parsePictureMsg(leftObs, middleObs, rightObs);

        if (!bot.isSim()) {
            NetworkMgr comm = NetworkMgr.getInstance();
            comm.sendMsg(msg + "", NetworkMgr.INSTRUCTIONS);
        }
        System.out.println("Taking image: " + msg);
    }

//    /**
//     * take picture and send out coordinate is there are walls/obstacles in surrounding
//     */
//    protected void tryTakePicture() {
//        AgentSettings.Direction botDir = bot.getAgtDir();
//        for (int offset = AgentSettings.CAMERA_MIN; offset <= AgentSettings.CAMERA_MAX; offset++) {
//            if (canTakePicture(botDir, offset)) {
//                if (botDir == AgentSettings.Direction.NORTH) {
//                    bot.takePicture(bot.getAgtRow(), bot.getAgtCol() + (1 + offset));
//                }
//                else if (botDir == AgentSettings.Direction.EAST) {
//                    bot.takePicture(bot.getAgtRow() - (1 + offset), bot.getAgtCol());
//                }
//                else if (botDir == AgentSettings.Direction.WEST) {
//                    bot.takePicture(bot.getAgtRow() + (1 + offset), bot.getAgtCol());
//                }
//                else {
//                    bot.takePicture(bot.getAgtRow(), bot.getAgtCol() - (1 + offset));
//                }
//                break;
//            }
//        }
//    }

//    /**
//     * Check if the if the center cell on the RHS of the bot is wall/obstacle so can take picture
//     */
//    private boolean canTakePicture(AgentSettings.Direction botDir, int offset) {
//        int row = bot.getAgtRow();
//        int col = bot.getAgtCol();
//
//        switch (botDir) {
//            case NORTH:
//                return exploredMap.isWallOrObstacleCell(row, col + (1 + offset));
//            case EAST:
//                return exploredMap.isWallOrObstacleCell(row - (1 + offset), col);
//            case SOUTH:
//                return exploredMap.isWallOrObstacleCell(row, col - (1 + offset));
//            case WEST:
//                return exploredMap.isWallOrObstacleCell(row + (1 + offset), col);
//        }
//
//        return false;
//    }

    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    @Override
    protected void nextMove() {
        System.out.println("Bot Direction: " + bot.getAgtDir());
        if (lookRight()) {
//            System.out.println("[DEBUG] Right Clear");
            moveBot(Actions.FACE_RIGHT);
            if (lookForward()) {
//                System.out.println("  ->[DEBUG]Forward Clear");
                moveBot(Actions.FORWARD);
            }
        } else if (lookForward()) {
//            System.out.println("[DEBUG]Forward Clear");
            moveBot(Actions.FORWARD);
        } else if (lookLeft()) {
//            System.out.println("[DEBUG]Left Clear");
            moveBot(Actions.FACE_LEFT);
            if (lookForward()) {
//                System.out.println("  ->[DEBUG]Forward Clear");
                moveBot(Actions.FORWARD);
            }
        } else {
//            System.out.println("[DEBUG]Reverse Direction");
//            moveBot(Actions.FACE_LEFT);
//            tryTakePicture();
//            moveBot(Actions.FACE_LEFT);
        }

        if (!bot.isSim()) {
            String[] MDFString = MapDescriptorFormat.generateMapDescriptorFormat(exploredMap);
            String msg = MDFString[0] + "|" + MDFString[1];
            NetworkMgr.getInstance().sendMsg(msg, NetworkMgr.MAP_STRINGS);
        }
    }


}
