import hardware.Agent;
import hardware.AgentSettings;
import map.GraphicsSettings;
import map.Map;
import map.MapSettings;
import network.NetworkMngr;

import javax.swing.*;
import java.awt.*;

public class Simulator {
    private static JFrame _appFrame = null;                                     // application JFrame

    private static JPanel _mapCards = null;                                     // JPanel for map views
    private static JPanel _buttons = null;                                      // JPanel for buttons

    private static Agent agt;
    private static AgentSettings.Direction startDir = AgentSettings.START_DIR;  // Agent Start Direction

    private static Map dummyMap = null;                                          // real map
    private static Map exploredMap = null;                                      // exploration map

    private static int timeLimit = 3600;                                        // time limit
    private static int coverageLimit = 300;                                     // coverage limit

//    private static final NetworkMngr comm = NetworkMngr.getMngr();
    private static final boolean sim = true;

    public static void main(String[] args) {
//        if (!sim) comm.openConnection();

        agt = new Agent(MapSettings.START_ROW, MapSettings.START_COL, startDir, sim);
        if (sim) {
            dummyMap = new Map(agt); dummyMap.setAllUnexplored();
        }
        exploredMap = new Map(agt); exploredMap.setAllUnexplored();

        displayAll();
    }

    private static void displayAll() {
        // Main frame init for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Group 33");
        _appFrame.setSize(new Dimension(690, 700));
        _appFrame.setResizable(false);

        // Position main frame at the center of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        _appFrame.setLocation(dim.width / 2 - _appFrame.getSize().width / 2, dim.height / 2 - _appFrame.getSize().height / 2);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());

        // Create the JPanel for the buttons
        _buttons = new JPanel();

        // Add _mapCards & _buttons to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_buttons, BorderLayout.PAGE_END);

        // Initialize the main map view
        initMainLayout();

        // Initialize the buttons
        initButtonsLayout();

        // Display the application
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void initMainLayout() {
        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        _mapCards.add(exploredMap, "EXPLORATION");
        if (sim) {
            _mapCards.add(dummyMap, "DUMMY_MAP");
            cl.show(_mapCards, "DUMMY_MAP");
        } else {
            cl.show(_mapCards, "EXPLORATION");
        }
    }

    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout());
        addButtons();
    }

    private static void addButtons() {

    }
}
