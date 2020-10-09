package logic.exploration;

import hardware.Agent;
import hardware.AgentSettings;
import map.BlkSurface;
import map.Cell;
import map.Map;
import network.NetworkMgr;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ImageRegExp extends ExplorationAlgo {

    protected final Map exploredMap = null;
    protected final Map realMap = null;
    protected final Agent bot = null;
    protected int coverageLimit = 300;
    protected int timeLimit = 3600;    // in second
    protected int areaExplored;
    protected long startTime; // in millisecond
    protected long currentTime;
    private boolean calibrationMode;
    private int lastCalibrate;

    ArrayList<AgentSettings.Actions> actionsTaken = new ArrayList<>();

    // for image
    HashMap<String, BlkSurface> notYetTaken;


    Scanner scanner = new Scanner(System.in);

    public ImageRegExp(Map exploredMap, Map realMap, Agent bot, int coverageLimit, int timeLimit, Map exploredMap1) {
        super(exploredMap, realMap, bot, coverageLimit, timeLimit);
    }

    @Override
    public void runExploration() throws InterruptedException {
        this.imageExploration();
    }

    public void imageExploration() throws InterruptedException {
        HashMap<String, BlkSurface> allPossibleSurfaces;

        if (!bot.isSim()) {
            System.out.println("Starting calibration...");

            // TODO initial calibration

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

        super.explorationLoop(bot.getAgtY(), bot.getAgtX());
        exploredMap.repaint();

        notYetTaken = getUntakenSurfaces();
        if (notYetTaken.size() == 0) {
            return;
        }

        // get all untaken surfaces
        while (notYetTaken.size() > 0) {
            imageLoop();
        }
        
        // go back to start position
    }

    private HashMap<String, BlkSurface> getUntakenSurfaces() {
        HashMap<String, BlkSurface> untakenSurfaces = new HashMap<String, BlkSurface>();
        untakenSurfaces.put("Test String", new BlkSurface(new Point(1,1), AgentSettings.Direction.NORTH));
        return untakenSurfaces;
    }

    private void imageLoop() {
        // TODO
    }

    /**
     * Determines the next move for the bot and executes it accordingly.
     */
    @Override
    protected void nextMove() {

    }
}
