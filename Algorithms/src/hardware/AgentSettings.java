package hardware;

public class AgentSettings {
    // Agent Start Direction
    public static final Direction START_DIR = Direction.NORTH;
    public static final int SPEED = 100;                // delay between movements (ms)

    // G values used for A* algorithm
    public static final int MOVE_COST = 1;
    public static final int TURN_COST = 5;
    public static final int INFINITE_COST = 10000000;
//    public static final int CALIBRATE_AFTER = 3;        //Calibrate After number of moves

    public static final int MOVE_STEPS = 1;
    public static final int MOVE_SPEED = 5000;	        // Delays before movement (Lower = faster) in milliseconds
    public static final long WAIT_TIME = 5000;	        // Time waiting before retransmitting in milliseconds
    public static final short CAMERA_RANGE = 4;

    // Sensors default range (In grids)
    public static final int SHORT_MIN = 1;
    public static final int SHORT_MAX = 2;

    public static final int LONG_MIN = 3;
    public static final int LONG_MAX = 4;

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
        ALIGN_FRONT, ALIGN_RIGHT,                   // Calibrate Robot, only used for real runs

        RESET_ROBOT,                                // Reset Agent, sensors to initial position/direction.
                                                    // If applicable, reset Waypoint too.

        ERROR;                                      // Error
          
          public static String print(Actions m) {
            switch (m) {
                case FORWARD:
                    return "F";
                case BACKWARD:
                    return "B";
                case FACE_RIGHT:
                    return "FR";
                case MOVE_RIGHT:
                    return "MR";
                case FACE_LEFT:
                    return "FL";
                case MOVE_LEFT:
                    return "ML";
                case START_EXP:
                    return "START_EXP";
                case ALIGN_FRONT:
                    return "ALIGN_FRONT";
                case ALIGN_RIGHT:
                    return "ALIGN_RIGHT";
                case START_FAST:
                    return "START_FAST";
                case ERROR:
                default:
                    return "E";
            }
        }
    }
}
