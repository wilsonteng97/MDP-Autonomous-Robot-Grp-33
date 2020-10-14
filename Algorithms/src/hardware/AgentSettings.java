package hardware;

import java.awt.*;

public class AgentSettings {
    // Agent Start Direction
    public static final Direction START_DIR = Direction.NORTH;
    public static final int SPEED = 1000;                           // delay between movements (ms)
    public static final int TURN_SPEED = 2000;                      // delay between movements (ms)
    public static final int CALIBERATE_SPEED = 3000;                      // delay between movements (ms)

    public static final int GOAL_ROW = 18;                          // row no. of goal cell
    public static final int GOAL_COL = 13;                          // col no. of goal cell
    public static final int START_ROW = 1;                          // row no. of start cell
    public static final int START_COL = 1;                          // col no. of start cell

    // G values used for A* algorithm
    public static final int MOVE_COST = 1;
    public static final int TURN_COST = 5;
    public static final int INFINITE_COST = 10000000;
//    public static final int CALIBRATE_AFTER = 3;        //Calibrate After number of moves

    public static final int MOVE_STEPS = 1;
    public static final int MOVE_SPEED = 2000;	        // Delays before movement (Lower = faster) in milliseconds
    public static final long WAIT_TIME = 5000;	        // Time waiting before retransmitting in milliseconds
    public static final short CAMERA_RANGE = 4;

    // Sensors default range (In grids)
    public static final int SHORT_MIN = 1;
    public static final int SHORT_MAX = 3;

    public static final int LONG_MIN = 1;
    public static final int LONG_MAX = 5;

    // Camera default range (In grids)
    public static final int CAMERA_MIN = 1;
    public static final int CAMERA_MAX = SHORT_MAX;     // Should be the same as SHORT_MAX

    // Camera Direction
    public static final Direction CAMERA_DIRECTION = Direction.EAST;

    public static final double RIGHT_THRES = 0.5;       // Threshold value or right sensor will calibrate once exceeded
    public static final double RIGHT_DIS_THRES_CLOSE = 1.0;
    public static final double RIGHT_DIS_THRES_FAR = 3.8;

    // Direction enum based on compass
    public static enum Direction {
        NORTH_WEST, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST;

        // Get new direction when robot turns clockwise
        public static Direction clockwise90(Direction currDirection) {
            return values()[(currDirection.ordinal() + 2) % values().length];
        }
        public static Direction clockwise(Direction currDirection, int step45) {
            return values()[(currDirection.ordinal() + step45) % values().length];
        }
        // Get new direction when robot turns anti-clockwise
        public static Direction antiClockwise90(Direction currDirection) {
            return values()[(currDirection.ordinal() + values().length - 2) % values().length];
        }
        public static Direction antiClockwise(Direction currDirection, int step45) {
            return values()[(currDirection.ordinal() + values().length - step45) % values().length];
        }

        public static Direction reverse(Direction currDirection) {
            return values()[(currDirection.ordinal() + 4) % values().length];
        }

        /**
         * Direction Representations :
         *
         *        \1|N|2/
         *         W| |E
         *        /4|S|3\
         */
        public static char print(Direction direction) {
            switch (direction) {
                case NORTH_WEST:
                    return '1';
                case NORTH:
                    return 'N';
                case NORTH_EAST:
                    return '2';
                case EAST:
                    return 'E';
                case SOUTH_EAST:
                    return '3';
                case SOUTH:
                    return 'S';
                case SOUTH_WEST:
                    return '4';
                case WEST:
                    return 'W';
                default:
                    return '-';
            }
        }
    }

    public static enum Actions {

        START_EXP, START_FAST, END_EXP, END_FAST,   // Start/End "Exploration"/"Fastest Path" tasks

        FORWARD, BACKWARD, MOVE_LEFT, MOVE_RIGHT,   // Move with reference to the direction Agent is facing.
        FACE_LEFT, FACE_RIGHT, FACE_REVERSE,        // Change Direction of Agent
        ALIGN_FRONT, ALIGN_RIGHT, CALIBRATE,                  // Calibrate Robot, only used for real runs

        RESET_ROBOT,                                // Reset Agent, sensors to initial position/direction.
                                                    // If applicable, reset Waypoint too.

        TAKE_PICTURE,

        ERROR;                                      // Error

          public static String print(Actions m) {
            switch (m) {
                case FORWARD:
                    return "W1|";
                case BACKWARD:
                    return "S|";
                case FACE_RIGHT:
                    return "D1|";
//                case MOVE_RIGHT:
//                    return "D1|";
                case FACE_LEFT:
                    return "A1|";
//                case MOVE_LEFT:
//                    return "A1|";
                case START_EXP:
                    return "ES";
                case ALIGN_FRONT:
                    return "V|";
                case ALIGN_RIGHT:
                    return "B|";
                case START_FAST:
                    return "FS";
                case TAKE_PICTURE:
                    return "P|";
                case ERROR:
                default:
                    return "E";
            }
        }
    }
}
