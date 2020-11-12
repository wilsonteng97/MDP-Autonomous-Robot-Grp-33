package com.example.mdp;

import java.security.spec.ECField;

public class MapDescriptorFormat {

    public static String binToHex(String bin) {
        int dec = Integer.parseInt(bin, 2);
        return Integer.toHexString(dec);
    }

    public static String hexToBin(String hexStr) {
        String bin = "", tempStr = "", tempBin;
        int temp;
        for (int i = 0; i < hexStr.length(); i++) {
            tempStr += hexStr.charAt(i);
            try {
                temp = Integer.parseInt(tempStr, 16);
            } catch (Exception e) {
                temp = 0;
            }
            try {
                tempBin = Integer.toBinaryString(temp);
            } catch (Exception e) {
                tempBin = "0";
            }
            if (tempBin.length() != 4)
                while (tempBin.length() != 4)
                    tempBin = "0" + tempBin;
            bin += tempBin;
            tempStr = "";
        }
        return bin;
    }

    public static String checkBar (String s) {
        int length = s.length();
        if (s.substring(length-1).equals("|")) {
            return s.substring(0, length-3);
        }
        return s;
    }
}