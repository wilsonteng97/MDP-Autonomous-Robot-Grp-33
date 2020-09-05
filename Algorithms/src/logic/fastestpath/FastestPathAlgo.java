package logic.fastestpath;

import hardware.Agent;
import hardware.AgentSettings;
import map.Cell;
import map.Map;

import java.util.Stack;

abstract public class FastestPathAlgo {
    protected Map exploredMap;
    protected final Map realMap;
    protected Agent bot;
    protected AgentSettings.Direction curDir; // current direction of robot
    protected Cell current;                   // current Cell
    protected boolean explorationMode;


    public FastestPathAlgo(Map exploredMap, Agent bot) {
        this.realMap = null;
        initObject(exploredMap, bot);
    }

    public FastestPathAlgo(Map exploredMap, Agent bot, Map realMap) {
        this.realMap = realMap;
        this.explorationMode = true;
        initObject(exploredMap, bot);

    }

    protected void initObject(Map map, Agent bot) {
        this.bot = bot;
        this.exploredMap = map;
        this.current = map.getCell(bot.getAgtRow(), bot.getAgtCol());
        this.curDir = bot.getAgtDir();
    }

    /**
     * Returns true if the cell can be visited.
     */
    protected boolean canBeVisited(Cell c) {
        return c.isExplored() && !c.isObstacle() && !c.isVirtualWall();
    }

    /**
     * Returns the target direction of the bot from [botR, botC] to target Cell.
     * Assuming bot position and target position are neighbors
     */
    protected AgentSettings.Direction getTargetDir(int botR, int botC, AgentSettings.Direction botDir, Cell target) {
        if (botC - target.getCol() > 0) {
            return AgentSettings.Direction.WEST;
        } else if (target.getCol() - botC > 0) {
            return AgentSettings.Direction.EAST;
        } else {
            if (botR - target.getRow() > 0) {
                return AgentSettings.Direction.SOUTH;
            } else if (target.getRow() - botR > 0) {
                return AgentSettings.Direction.NORTH;
            } else {
                return botDir;
            }
        }
    }

    /**
     * Get the actual turning cost from one DIRECTION to another.
     * TODO need to test
     */
    protected double getTurnCost(AgentSettings.Direction a, AgentSettings.Direction b) {
        int numOfTurn = Math.abs(a.ordinal() - b.ordinal()) / 2;    // need to test
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * AgentSettings.TURN_COST);
    }

    /**
     * Returns true if the robot can move forward one cell with the current heading.
     */
    protected boolean canMoveForward() {
        int row = bot.getAgtRow();
        int col = bot.getAgtCol();

        switch (bot.getAgtDir()) {
            case NORTH:
                if (!exploredMap.isObstacleCell(row + 2, col - 1) && !exploredMap.isObstacleCell(row + 2, col) && !exploredMap.isObstacleCell(row + 2, col + 1)) {
                    return true;
                }
                break;
            case EAST:
                if (!exploredMap.isObstacleCell(row + 1, col + 2) && !exploredMap.isObstacleCell(row, col + 2) && !exploredMap.isObstacleCell(row - 1, col + 2)) {
                    return true;
                }
                break;
            case SOUTH:
                if (!exploredMap.isObstacleCell(row - 2, col - 1) && !exploredMap.isObstacleCell(row - 2, col) && !exploredMap.isObstacleCell(row - 2, col + 1)) {
                    return true;
                }
                break;
            case WEST:
                if (!exploredMap.isObstacleCell(row + 1, col - 2) && !exploredMap.isObstacleCell(row, col - 2) && !exploredMap.isObstacleCell(row - 1, col - 2)) {
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Returns the movement to execute to get from one direction to another.
     */
    protected AgentSettings.Actions getTargetMove(AgentSettings.Direction a, AgentSettings.Direction b) {
        switch (a) {
            case NORTH:
                switch (b) {
                    case NORTH:
                        return AgentSettings.Actions.ERROR;
                    case SOUTH:
                        return AgentSettings.Actions.FACE_LEFT;
                    case WEST:
                        return AgentSettings.Actions.FACE_LEFT;
                    case EAST:
                        return AgentSettings.Actions.FACE_RIGHT;
                }
                break;
            case SOUTH:
                switch (b) {
                    case NORTH:
                        return AgentSettings.Actions.FACE_LEFT;
                    case SOUTH:
                        return AgentSettings.Actions.ERROR;
                    case WEST:
                        return AgentSettings.Actions.FACE_RIGHT;
                    case EAST:
                        return AgentSettings.Actions.FACE_LEFT;
                }
                break;
            case WEST:
                switch (b) {
                    case NORTH:
                        return AgentSettings.Actions.FACE_RIGHT;
                    case SOUTH:
                        return AgentSettings.Actions.FACE_LEFT;
                    case WEST:
                        return AgentSettings.Actions.ERROR;
                    case EAST:
                        return AgentSettings.Actions.FACE_LEFT;
                }
                break;
            case EAST:
                switch (b) {
                    case NORTH:
                        return AgentSettings.Actions.FACE_LEFT;
                    case SOUTH:
                        return AgentSettings.Actions.FACE_RIGHT;
                    case WEST:
                        return AgentSettings.Actions.FACE_LEFT;
                    case EAST:
                        return AgentSettings.Actions.ERROR;
                }
        }
        return AgentSettings.Actions.ERROR;
    }

    /**
     * Prints the fastest path from the Stack object.
     */
    abstract protected void printFastestPath(Stack<Cell> path);

    /**
     * Executes the fastest path and returns a StringBuilder object with the path steps.
     */
    abstract protected String executePath(Stack<Cell> path, int goalRow, int goalCol);

    /**
     * Find the fastest path from the robot's current position to [goalRow, goalCol].
     */
    abstract public String runFastestPath(int goalRow, int goalCol);

    /**
     * Generates path in reverse using the parents HashMap.
     */
    abstract protected Stack<Cell> getPath(int goalRow, int goalCol);
}
