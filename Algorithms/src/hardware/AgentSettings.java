package hardware;

public class AgentSettings {
    // G values used for A* algorithm
    public static final int MOVE_COST = 1;
    public static final int TURN_COST = 5;
    public static final double INFINITE_COST = 10000000;
    //	public static final int CALIBRATE_AFTER = 3; //Calibrate After number of moves

    public static final int MOVE_STEPS = 1;
    public static final int MOVE_SPEED = 5000;	//Delays before movement (Lower = faster) in milliseconds
    public static final long WAIT_TIME = 5000;	//Time waiting before retransmitting in milliseconds
    public static final short CAMERA_RANGE = 4;

    // Sensors default range (In grids)
    public static final int SHORT_MIN = 1;
    public static final int SHORT_MAX = 3;

    public static final int LONG_MIN = 1;
    public static final int LONG_MAX = 5;

    public static final double RIGHT_THRES = 0.5; //Threshold value or right sensor will calibrate once exceeded
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
        FORWARD, FACE_LEFT, FACE_RIGHT, MOVE_LEFT, MOVE_RIGHT, BACKWARD, ALIGN_FRONT, ALIGN_RIGHT, SEND_SENSORS, ERROR, ENDEXP, ENDFAST, ROBOT_POS, START_EXP, START_FAST;
    }
}
