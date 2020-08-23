package utils;

import map.Map;
import map.MapSettings;

import java.io.*;

import static utils.IOsettings.FILE_DIR;
import static utils.IOsettings.FILE_EXT;

public class FileIO {
    public static Map loadMap(String filename) {
        Map map = new Map();
        try {
            InputStream inputStream = new FileInputStream(FILE_DIR + filename + FILE_EXT);
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
                    if (bin.charAt(binPtr) == '1') map.createVirtualWalls(row, col);
                    binPtr++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
