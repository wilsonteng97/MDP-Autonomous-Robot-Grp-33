package gridmap;

import logic.Agent;

import javax.swing.*;

public class Grid extends JPanel {
    private final Cell[][] grid = new Cell[MapSettings.MAP_ROWS][MapSettings.MAP_COLS];
    private static Agent agent;

    public Grid(Agent agent) {
        this.agent = agent;
    }
}
