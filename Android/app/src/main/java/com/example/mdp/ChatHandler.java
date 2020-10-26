package com.example.mdp;

public class ChatHandler {

    public boolean chatIsCommand (String msg) {
        if (msg.substring(0, 1).equals("{")) return true;
        return false;
    }

    public String[] splitCommand (String msg) {
        msg = cleanCommand(new String[] {"\"", "{", "}", "[", "]", " ", "(", ")"} ,msg);
        String[] arr = msg.split("\\|");
        return arr;
    }

    public String[] getCommand (String msg) {
        String[] arr = msg.split(":", 2);
        return arr;
    }

    public String cleanCommand (String[] delete, String msg) {
        for(String key: delete) {
            msg = msg.replace(key, "");
        }
        return msg;
    }
}
