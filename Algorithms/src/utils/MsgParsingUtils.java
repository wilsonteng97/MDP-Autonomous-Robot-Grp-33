package utils;

import java.awt.*;

public class MsgParsingUtils {
    public static String parseFastestPathString(String msg) {
        String new_msg = "";
        int count = 1;
        msg = msg.replace("1|", "");
        char current_dir = msg.charAt(0);
        System.out.println(msg);
        for(int i=1; i<=msg.length(); i++){
            if(i == msg.length()){
                new_msg = new_msg + current_dir +  String.valueOf(count);
                break;
            }

            if(count == 9){
                new_msg = new_msg + current_dir + String.valueOf(count);
                count = 0;
            }

            if(msg.charAt(i) == current_dir){
                count = count + 1;
            }
            else{
                if (count == 0){
                    count = count + 1;
                    new_msg = new_msg + msg.charAt(i) + String.valueOf(count);
                } else {
                    new_msg = new_msg + current_dir + String.valueOf(count);
                    current_dir = msg.charAt(i);
                    count = 1;
                }
            }

        }

        return new_msg;
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
