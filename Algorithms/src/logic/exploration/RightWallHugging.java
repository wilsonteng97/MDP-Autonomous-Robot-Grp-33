package logic.exploration;

import hardware.Agent;
import map.Map;

public class RightWallHugging extends ExplorationAlgo {
    public RightWallHugging(Map explorationMap, Map dummyMap, Agent agt, int coverageLimit, int timeLimit) {
        super(explorationMap, dummyMap, agt, coverageLimit, timeLimit);
    }
}
