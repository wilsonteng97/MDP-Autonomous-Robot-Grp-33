package utils;

import map.Map;
import map.MapSettings;

import java.io.*;

import static utils.IOsettings.FILE_DIR;
import static utils.IOsettings.FILE_EXT;

public class FileIO {
    public static Map loadMap(Map map, String filename) {
        try {
            InputStream inputStream = new FileInputStream("/Users/guomukun/mdp/MDP-Autonomous-Robot-Grp-33/Algorithms/arena/" + filename + FILE_EXT);
            BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = buf.readLine();
            }

            String bin = sb.toString();
            int binPtr = 0;
            for (int row = MapSettings.MAP_ROWS - 1; row >= 0; row--) {
                for (int col = 0; col < MapSettings.MAP_COLS; col++) {
                    if (bin.charAt(binPtr) == '1') map.getCell(row, col).setObstacle(true);
                    binPtr++;
                }
            }
            map.setAllExplored();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
