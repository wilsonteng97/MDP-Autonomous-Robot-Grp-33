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
    public RightWallHugging(Map exploredMap, Map realMap, Agent bot, int coverageLimit, int timeLimit) {
        super(exploredMap, realMap, bot, coverageLimit, timeLimit);
    }

    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    @Override
    protected void nextMove() {
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




}
