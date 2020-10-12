package utils;

public class SimulatorSettings {
    public static final boolean SIM = true;

    public static final int SIM_ACCELERATION = 40;      // The number of times simulation is accelerated by.

    public static final int GOHOMESLOW_SLEEP = 1500;     // Seconds simulation is asleep before executing goHomeSlow() method.

    public static final String NETWORK_IP_ADDRESS = "192.168.33.1";
//    public static final String NETWORK_IP_ADDRESS = "localhost";
    public static final int NETWORK_PORT = 5040;

//    public static final String EXPLORATION_ALGO_MODE = "E";
    public static final String EXPLORATION_ALGO_MODE = "P";
}
