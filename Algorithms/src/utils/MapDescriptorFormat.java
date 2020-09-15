package utils;

import map.*;

public class MapDescriptorFormat {

    private static String binToHex(String bin) {
        int dec = Integer.parseInt(bin, 2);
        return Integer.toHexString(dec);
    }

    private static String hexToBin(String hexStr) {
        String bin = "", tempStr = "", tempBin;
        int temp;
        for (int i = 0; i < hexStr.length(); i++) {
            tempStr += hexStr.charAt(i);
            temp = Integer.parseInt(tempStr, 16);
            tempBin = Integer.toBinaryString(temp);
            if (tempBin.length() != 4)
                while (tempBin.length() != 4)
                    tempBin = "0" + tempBin;
            bin += tempBin;
            tempStr = "";
        }
        return bin;
    }

    public static String[] generateMapDescriptorFormat(Map map) {
        String[] ret = new String[2];

        String Part1 = generateMDFStringPart1(map);
        String Part2 = generateMDFStringPart2(map);

        ret[0] = Part1; ret[1] = Part2;
        System.out.println("P1: " + Part1 + "\nP2: " + Part2);
        System.out.println();
        return ret;
    }

    public static String generateMDFStringPart1(Map map) {
        StringBuilder part1 = new StringBuilder();
        StringBuilder part1_bin = new StringBuilder();
        part1_bin.append("11");
        for (int r = 0; r < MapSettings.MAP_ROWS; r++) {
            for (int c = 0; c < MapSettings.MAP_COLS; c++) {

                if (map.getCell(r, c).isExplored())
                    part1_bin.append("1");
                else
                    part1_bin.append("0");

                if (part1_bin.length() == 4) {
                    part1.append(binToHex(part1_bin.toString()));
                    part1_bin.setLength(0);
                }
            }
        }
        part1_bin.append("11");
        part1.append(binToHex(part1_bin.toString()));
        return part1.toString();
    }

    public static String generateMDFStringPart2(Map map) {
        StringBuilder part2 = new StringBuilder();
        StringBuilder part2_bin = new StringBuilder();
        for (int r = 0; r < MapSettings.MAP_ROWS; r++) {
            for (int c = 0; c < MapSettings.MAP_COLS; c++) {
                if (map.getCell(r, c).isExplored()) {
                    if (map.getCell(r, c).isObstacle())
                        part2_bin.append("1");
                    else
                        part2_bin.append("0");

                    if (part2_bin.length() == 4) {
                        part2.append(binToHex(part2_bin.toString()));
                        part2_bin.setLength(0);
                    }
                }
            }
        }
        if (part2_bin.length() > 0) part2.append(binToHex(part2_bin.toString()));
        return part2.toString();
    }
}
