package logic;

import hardware.Agent;
import hardware.AgentSettings;
import logic.exploration.RightWallHugging;

public class TestAlgo {
    public static void main(String[] args) throws Exception {
        System.out.println("testing...");

        Agent bot = new Agent(1, 1, AgentSettings.Direction.NORTH, true);
        TestMap testMap = new TestMap(bot);

        System.out.println(testMap);

        RightWallHugging algo = new RightWallHugging(testMap, testMap, bot, 300, 3600);
        algo.runExploration();
    }
}
