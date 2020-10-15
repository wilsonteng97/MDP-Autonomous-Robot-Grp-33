package logic.exploration;

import hardware.Agent;
import hardware.AgentSettings.Actions;
import map.ArenaMap;
import network.NetworkMgr;
import utils.MapDescriptorFormat;

public class RightWallHugging extends ExplorationAlgo {
    public RightWallHugging(ArenaMap exploredArenaMap, ArenaMap realArenaMap, Agent bot, int coverageLimit, int timeLimit) {
        super(exploredArenaMap, realArenaMap, bot, coverageLimit, timeLimit);
    }

    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    @Override
    protected void nextMove() {
        System.out.println("Bot Direction: " + bot.getAgtDir());
        if (lookRight()) {
//            System.out.println("[DEBUG] Right Clear");
            moveBot(Actions.FACE_RIGHT);
            NetworkMgr.getInstance().sendMsg("Z", NetworkMgr.INSTRUCTIONS);
            senseAndRepaint();
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
            NetworkMgr.getInstance().sendMsg("Z", NetworkMgr.INSTRUCTIONS);
            senseAndRepaint();
            if (lookForward()) {
//                System.out.println("  ->[DEBUG]Forward Clear");
                moveBot(Actions.FORWARD);
            }
        } else {
//            System.out.println("[DEBUG]Reverse Direction");
            moveBot(Actions.FACE_LEFT);
            NetworkMgr.getInstance().sendMsg("Z", NetworkMgr.INSTRUCTIONS);
            senseAndRepaint();
//            tryTakePicture();
            moveBot(Actions.FACE_LEFT);
        }
        System.out.println("New Bot Direction: " + bot.getAgtDir());
        if (!bot.isSim()) {
            String[] MDFString = MapDescriptorFormat.generateMapDescriptorFormat(exploredArenaMap);
            String msg = MDFString[0] + ":" + MDFString[1] + "|";
            NetworkMgr.getInstance().sendMsg(msg, NetworkMgr.MAP_STRINGS);
        }
    }




}
