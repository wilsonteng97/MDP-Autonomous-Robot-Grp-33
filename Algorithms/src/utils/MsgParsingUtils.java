package utils;

import java.awt.*;

public class MsgParsingUtils {
    public static String parseFastestPathString(String msg) {
        char temp_char;
        char prev;
        int count = 0;
        String fpMsg = new String();

        prev = msg.charAt(0);
        for(int i = 0, n = msg.length() ; i < n ; i = i + 3) {
            temp_char = msg.charAt(i);
            if (temp_char == prev && count <= 8) {
                count += 1;
            } else {
                fpMsg += prev;
                fpMsg += String.valueOf(count);
                count = 1;
            }
            prev = temp_char;
        }
        fpMsg += prev;
        fpMsg += String.valueOf(count);

        return fpMsg;
    }

    public static String parsePictureMsg(Point leftObs, Point middleObs, Point rightObs) {
        if (leftObs == null) {
            leftObs = new Point(-1,-1);
        }
        if (middleObs == null) {
            middleObs = new Point(-1,-1);
        }
        if (rightObs == null) {
            rightObs = new Point(-1,-1);
        }

        String s = String.join(":", Integer.toString(leftObs.x), Integer.toString(leftObs.y),
                Integer.toString(middleObs.x), Integer.toString(middleObs.y),
                Integer.toString(rightObs.x), Integer.toString(rightObs.y));

        return "P" + s.toString() + "|";
    }
}
