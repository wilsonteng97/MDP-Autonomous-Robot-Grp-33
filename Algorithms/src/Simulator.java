import hardware.Agent;
import hardware.AgentSettings;
import logic.exploration.ExplorationAlgo;
import logic.exploration.ImageRegExp;
import logic.exploration.RightWallHugging;
import logic.fastestpath.AStarHeuristicSearch;
import logic.fastestpath.FastestPathAlgo;
import map.ArenaMap;
import map.MapSettings;
import network.NetworkMgr;
import utils.SimulatorSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Scanner;

import static utils.FileIO.loadMap;
import static utils.MapDescriptorFormat.generateMapDescriptorFormat;
import static utils.MsgParsingUtils.parseFastestPathString;

public class Simulator {
    private static final boolean sim = SimulatorSettings.SIM;

    private static JFrame _appFrame = null;                                     // application JFrame

    private static JPanel _mapCards = null;                                     // JPanel for map views
    private static JPanel _buttons = null;                                      // JPanel for buttons

    private static Agent agt;
    private static AgentSettings.Direction startDir = AgentSettings.START_DIR;  // Agent Start Direction

    private static ArenaMap dummyArenaMap = null;                                         // real map
    private static ArenaMap explorationArenaMap = null;                                   // exploration map

    private static int timeLimit = 3600;                                        // in seconds
    private static int coverageLimit = 300;                                     // coverage limit

    private static final NetworkMgr comm = NetworkMgr.getInstance();

    private static String[] stringMDF;

    private static int waypointX;
    private static int waypointY;

    private static final Scanner sc = new Scanner(System.in);

    private static String startToWaypoint;
    private static String waypointToGoal;

    public static void main(String[] args) {
        if (!sim) comm.startConn();

        agt = new Agent(MapSettings.START_ROW, MapSettings.START_COL, sim);
        if (sim) {
            dummyArenaMap = new ArenaMap(agt); dummyArenaMap.setAllUnexplored();
        }
        explorationArenaMap = new ArenaMap(agt); explorationArenaMap.setAllUnexplored();

        displayAll();

        if (!sim) {
            readWaypointFromRpi();
        }
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
        _mapCards.add(explorationArenaMap, "EXPLORATION");
        if (sim) {
            _mapCards.add(dummyArenaMap, "DUMMY_MAP");
            cl.show(_mapCards, "DUMMY_MAP");
        } else {
            cl.show(_mapCards, "EXPLORATION");
        }
    }

    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout());
        addButtons();
    }

    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }

    private static void addButtons() {
        if (sim) {
            JButton btn_LoadMap = new JButton("Load Map");
            // Load ArenaMap Button
            formatButton(btn_LoadMap);
            btn_LoadMap.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JDialog loadMapDialog = new JDialog(_appFrame, "Load Map", true);
                    loadMapDialog.setSize(400, 60);
                    loadMapDialog.setLayout(new FlowLayout());

                    final JTextField loadTF = new JTextField(15);
                    loadTF.addKeyListener(new KeyListener() {
                        @Override
                        public void keyTyped(KeyEvent e) {}

                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (e.getKeyCode()==KeyEvent.VK_ENTER) {
                                explorationArenaMap.resetMap();
                                loadMapDialog.setVisible(false);
                                loadMap(dummyArenaMap, loadTF.getText());
                                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                                cl.show(_mapCards, "DUMMY_MAP");
                                dummyArenaMap.repaint();
                            }
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {}


                    });

                    JButton loadMapButton = new JButton("Load");

                    loadMapButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            explorationArenaMap.resetMap();
                            loadMapDialog.setVisible(false);
                            loadMap(dummyArenaMap, loadTF.getText());
                            CardLayout cl = ((CardLayout) _mapCards.getLayout());
                            cl.show(_mapCards, "DUMMY_MAP");
                            dummyArenaMap.repaint();
                        }


                    });

                    loadMapDialog.add(new JLabel("File Name: "));
                    loadMapDialog.add(loadTF);
                    loadMapDialog.add(loadMapButton);
                    loadMapDialog.setVisible(true);
                }
            });
            _buttons.add(btn_LoadMap);

            // Show DummyMap Button
            JButton btn_ShowDummyMap = new JButton("Toggle Fog");
            formatButton(btn_ShowDummyMap);
            btn_ShowDummyMap.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (dummyArenaMap.isMapExplored()) {
                        dummyArenaMap.setAllUnexplored();
                    } else {
                        dummyArenaMap.setAllExplored();
                    }
                    CardLayout cl = ((CardLayout) _mapCards.getLayout());
                    cl.show(_mapCards, "DUMMY_MAP");
                    dummyArenaMap.repaint();
                }
            });
            _buttons.add(btn_ShowDummyMap);
        }

        // FastestPath Class for Multithreading
        class FastestPath extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                System.out.println("Executing Fastest path");
                agt.setAgtCtrCoord(agt.getAgtRow(), agt.getAgtCol());
//                if (!sim) agt.setSim(true);
                Agent fakeBot = new Agent(agt.getAgtRow(), agt.getAgtCol(), true);
                explorationArenaMap.repaint();

                // Compute fastest path beforehand
                FastestPathAlgo toGoal, toWaypoint;
                if (sim) readWaypointFromStdin();
                toWaypoint = new AStarHeuristicSearch(explorationArenaMap, fakeBot);
                startToWaypoint = parseFastestPathString(toWaypoint.runFastestPath(waypointY, waypointX));

                toGoal = new AStarHeuristicSearch(explorationArenaMap, fakeBot);
                waypointToGoal = parseFastestPathString(toGoal.runFastestPath(MapSettings.GOAL_ROW, MapSettings.GOAL_COL));

                if (!sim) {
                    // Transmit signal to get Agent to start. Initiate handshake signals.
                    while (true) {
                        System.out.println("Waiting for FS|...");
                        String msg = comm.receiveMsg();
                        if (msg.equals(NetworkMgr.FP_START)) break;
                    }
                }
                
                if (!sim) NetworkMgr.getInstance().sendMsg("K" + startToWaypoint + waypointToGoal, NetworkMgr.INSTRUCTIONS);
                else System.out.println("K" + startToWaypoint + waypointToGoal);

//                if (!sim) agt.setSim(false);

                return 222;
            }
        }

        // Exploration Class for Multithreading
        class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                System.out.println("In [Exploration] class");
                int row, col;

                row = MapSettings.START_ROW;
                col = MapSettings.START_COL;

                explorationArenaMap.setAllUnexplored();
                if (dummyArenaMap != null && sim) dummyArenaMap.setAllUnexplored();
                agt.setAgtCtrCoord(row, col);
                explorationArenaMap.repaint();

                ExplorationAlgo exploration;
                if (SimulatorSettings.EXPLORATION_ALGO_MODE == "P") {
                    exploration = new ImageRegExp(explorationArenaMap, dummyArenaMap, agt, coverageLimit, timeLimit);
                } else {
                    exploration = new RightWallHugging(explorationArenaMap, dummyArenaMap, agt, coverageLimit, timeLimit);
                }

//                if (!sim) {
                    // Transmit signal to start Agent
//                    NetworkMgr.getInstance().sendMsg(null, NetworkMgr.BOT_START);
//                }

                exploration.runExploration();

                if (!sim) {
                    System.out.println("Automatically fastest path");
                    new FastestPath().execute();
                }

                stringMDF = generateMapDescriptorFormat(explorationArenaMap);
                return 111;
            }
        }

        // Exploration Button
        JButton btn_Exploration = new JButton("Exploration");
        formatButton(btn_Exploration);
        btn_Exploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                new Exploration().execute();
            }
        });
        _buttons.add(btn_Exploration);

        // Fastest Path Button
        JButton btn_FastestPath = new JButton("Fastest Path");
        formatButton(btn_FastestPath);
        btn_FastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                new FastestPath().execute();
            }
        });
        _buttons.add(btn_FastestPath);


        // TimeExploration Class for Multithreading
        class TimeExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                agt.setAgtCtrCoord(MapSettings.START_ROW, MapSettings.START_COL);
                explorationArenaMap.repaint();

                ExplorationAlgo timeExplo = new RightWallHugging(explorationArenaMap, dummyArenaMap, agt, coverageLimit, timeLimit);
                timeExplo.runExploration();

                return 333;
            }
        }

        // Time-limited Exploration Button
        JButton btn_TimeExploration = new JButton("Time-Limited");
        formatButton(btn_TimeExploration);
        btn_TimeExploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog timeExploDialog = new JDialog(_appFrame, "Time-Limited Exploration", true);
                timeExploDialog.setSize(400, 60);
                timeExploDialog.setLayout(new FlowLayout());
                JTextField timeTF = new JTextField(5);
                JButton timeSaveButton = new JButton("Run");

                timeTF.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {}

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
                            timeExploDialog.setVisible(false);
                            String time = timeTF.getText();
                            String[] timeArr = time.split(":");
                            timeLimit = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]);
                            System.out.println("[btn_TimeExploration()] " + timeLimit);
                            CardLayout cl = ((CardLayout) _mapCards.getLayout());
                            cl.show(_mapCards, "EXPLORATION");
                            new TimeExploration().execute();
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {}
                });

                timeSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        timeExploDialog.setVisible(false);
                        String time = timeTF.getText();
                        String[] timeArr = time.split(":");
                        timeLimit = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]);
                        System.out.println("[btn_TimeExploration()] " + timeLimit);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "EXPLORATION");
                        new TimeExploration().execute();
                    }
                });

                timeExploDialog.add(new JLabel("Time Limit (in MM:SS): "));
                timeExploDialog.add(timeTF);
                timeExploDialog.add(timeSaveButton);
                timeExploDialog.setVisible(true);
            }
        });
        _buttons.add(btn_TimeExploration);


        // CoverageExploration Class for Multithreading
        class CoverageExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                agt.setAgtCtrCoord(MapSettings.START_ROW, MapSettings.START_COL);
                explorationArenaMap.repaint();

                ExplorationAlgo coverageExplo = new RightWallHugging(explorationArenaMap, dummyArenaMap, agt, coverageLimit, timeLimit);
                coverageExplo.runExploration();

                return 444;
            }
        }

        // Coverage-limited Exploration Button
        JButton btn_CoverageExploration = new JButton("Coverage-Limited");
        formatButton(btn_CoverageExploration);
        btn_CoverageExploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog coverageExploDialog = new JDialog(_appFrame, "Coverage-Limited Exploration", true);
                coverageExploDialog.setSize(400, 60);
                coverageExploDialog.setLayout(new FlowLayout());
                JTextField coverageTF = new JTextField(5);
                JButton coverageSaveButton = new JButton("Run");

                coverageTF.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {}

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
                            coverageExploDialog.setVisible(false);
                            coverageLimit = (int) ((Integer.parseInt(coverageTF.getText())) * MapSettings.MAP_SIZE / 100.0);
                            System.out.println("[btn_CoverageExploration()] " + coverageLimit);
                            new CoverageExploration().execute();
                            CardLayout cl = ((CardLayout) _mapCards.getLayout());
                            cl.show(_mapCards, "EXPLORATION");
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {}
                });

                coverageSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        coverageExploDialog.setVisible(false);
                        coverageLimit = (int) ((Integer.parseInt(coverageTF.getText())) * MapSettings.MAP_SIZE / 100.0);
                        System.out.println("[btn_CoverageExploration()] " + coverageLimit);
                        new CoverageExploration().execute();
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "EXPLORATION");
                    }
                });

                coverageExploDialog.add(new JLabel("Coverage Limit (% of maze): "));
                coverageExploDialog.add(coverageTF);
                coverageExploDialog.add(coverageSaveButton);
                coverageExploDialog.setVisible(true);
            }
        });
        _buttons.add(btn_CoverageExploration);
    }

    private static void readWaypointFromStdin() {
        System.out.println("Waiting for waypoint input...");
        String[] waypointCoords = sc.nextLine().split("\\|");
        waypointX = Integer.parseInt(waypointCoords[1]);
        waypointY = Integer.parseInt(waypointCoords[2]);
        System.out.printf("Successfully reading waypoint [%d, %d]\n", waypointX, waypointY);
    }

    private static void readWaypointFromRpi() {
        String[] waypointCoords;
        while (true) {
            System.out.println("Waiting for waypoint input...");
            waypointCoords = comm.receiveMsg().split("\\|");
            if (waypointCoords[0].equals("WP")) break;
        }
        waypointX = Integer.parseInt(waypointCoords[1]);
        waypointY = Integer.parseInt(waypointCoords[2]);

        System.out.printf("Successfully reading waypoint [%d, %d]\n", waypointX, waypointY);
    }
}
