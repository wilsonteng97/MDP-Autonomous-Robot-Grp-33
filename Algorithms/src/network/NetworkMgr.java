package network;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class NetworkMgr {

    // Arduino Integration (Arduino --> PC)
    public static final String SENSOR_VALS = "S1|S2|S3|S4|S5|S6";   // "S1|S2|S3|S4|S5|S6", SX being the sensor value for X sensor
    // Rpi Integration (Rpi --> PC)
    public static final String IMG_DONE = "D";                      // Inform algo that photo has been taken and algo can resume exploration
    public static final String ALL_IMGS_FOUND = "I";                // Inform algo that all images have been taken
    // Android Integration (Android --> PC)
    public static final String EXP_START = "ES|";                   // Start exploration (RPi send E| to Arduino)
    public static final String FP_START = "FS|";                    // Fastest path (RPi send F| to Arduino)
    public static final String SEND_MDF_STR = "SendArena";          // Give MDF string
    public static final String START_POS = "starting (x,y,s)";      // Determine starting point (x and y coordinates, s is a integer of direction: 0-up, 1-right, 2-down, 3-left)
    public static final String SET_WAYPOINT = "waypoint (x,y)";     // Set waypoint (x,y)

    // PC --> other components
    public static final String MAP_STRINGS = "MAP";         // PC --> Android
    public static final String BOT_POS = "BOT_POS";         // PC --> Android
    public static final String BOT_START = "BOT_START";     // PC --> Arduino
    public static final String INSTRUCTIONS = "INSTR";      // PC --> Arduino

    //Variables for Java Socket
    private String ipAddr;
    private int port = 8080;
    private static Socket socket = null;

    // Buffers for Reading and Writing
    private BufferedWriter out;
    private BufferedReader in;
    private int msgCounter = 0;

    // Static Representation of NetMgr Instance
    private static NetworkMgr nwMgr = null;

    public NetworkMgr() {
        this.ipAddr = "192.168.33.1";
        this.port = 5040;
    }

    public static NetworkMgr getInstance() {
        if (nwMgr == null) {
            System.out.println("[getInstance()] Creating new NetworkMgr instance.");
            nwMgr = new NetworkMgr();
        }
        return nwMgr;
    }

    // Check if socket is connected.
    public boolean isConnected() {
        if (socket == null) {
            System.out.println("[isConnected()] Socket not initialised.");
            return false;
        }
        return socket.isConnected();
    }

    // Start connection with Rpi
    public boolean startConn() {
        try {
            System.out.println("Initiating Connection with RPI...");
            socket = new Socket(ipAddr, port);
            // Init in and out
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream())));
            System.out.println("[startConn()] Connection established successfully!");
            return true;
        } catch (UnknownHostException e) {
            System.out.println("[startConn()] Connection Failed (UnknownHostException)!"); e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[startConn()] Connection Failed (IOException)!"); e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[startConn()] Connection Failed (IOException)!"); e.printStackTrace();
        }
        System.out.println("[startConn()] Connection Failed!");
        return false;
    }

    // End connection with Rpi
    public void endConn() {
        try {
            System.out.println("[endConn()] Closing Connection...");
            if (socket != null) {
                socket.close();
                socket = null;
            }
            out.close();
            in.close();
            System.out.println("[endConn()] Connection Closed!");
        } catch (IOException e) {
            System.out.println("[endConn()] Unable to Close Connection (IOException)!"); e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("[endConn()] Unable to Close Connection (NullPointerException)!"); e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[endConn()] Unable to Close Connection (Exception)!"); e.printStackTrace();
        }
    }

    // Send Message
    public boolean sendMsg(String msg, String msgType) {
        try {
            System.out.println("[sendMsg()] Sending Message...");
            String outputMsg;
            // FIXME finalize message format here
            if (msg == null) {
                outputMsg = msgType + "\n";
            } else if (msgType.equals(MAP_STRINGS) || msgType.equals(BOT_POS)) {
                outputMsg = msgType + " " + msg + "\n";
            } else {
                outputMsg = msgType + " " + msg + "\n";
            }
            out.write(outputMsg);
            out.flush(); msgCounter++;
            System.out.println("[sendMsg() | " + msgCounter + "] Message: " + outputMsg);
        }
        catch (IOException e) {
            System.out.println("[sendMsg()] Sending Message Failed (IOException)!");
            if(socket.isConnected())
                System.out.println("[sendMsg()] Connection still Established!");
            else {
                while(true)
                {
                    System.out.println("[sendMsg()] Connection disrupted! Trying to Reconnect!");
                    if(nwMgr.startConn())
                        break;
                }
            }
            return nwMgr.sendMsg(msg, msgType);
        }
        catch (Exception e) {
            System.out.println("[sendMsg()] Sending Message Failed (IOException)!"); e.printStackTrace();
            return false;
        }
        return true;

    }

    // A naive receiveMsg()
//     public String receiveMsg() {
//         try {
//             System.out.println("[receiveMsg()] Receiving Message...");
//             String receivedMsg, parsedMsg;
//             StringBuilder msgParser = new StringBuilder();
//             InputStream din=socket.getInputStream();
//             while (true) {
//                 if(din.available()!=0){
//                     byte[] data321 = new byte[512];
//                     din.read(data321);
//                     receivedMsg=new String(data321, StandardCharsets.UTF_8);
//                     for (int i = 0, n = receivedMsg.length(); i < n; i++) {
//                         char c = receivedMsg.charAt(i);
//                         msgParser.append(c);
//                         if (c == '|') {
//                             break;
//                         }
//                     }
//                     parsedMsg = msgParser.toString();
// //                    System.out.println("Message Length: " + parsedMsg.length());

//                     if (parsedMsg != null && parsedMsg.length() > 0) {
//                         System.out.println("[receiveMsg()] Received Message: "+parsedMsg);
//                         return parsedMsg;
//                     }
//                 }
//             }


//         } catch (IOException e) {
//             System.out.println("[receiveMsg()] Receiving Message Failed (IOException)!");
//             return receiveMsg();
//         } catch (Exception e) {
//             System.out.println("[receiveMsg()] Receiving Message Failed!"); e.printStackTrace();
//         }
//         return null;
//     }

    // Wait and receive Message
    public String receiveMsg() {
        try {
            System.out.println("[receiveMsg()] Receiving Message...");
            String receivedMsg;
            while (true) {
                    receivedMsg = in.readLine();
                    if (receivedMsg != null && receivedMsg.length() > 0) {
                        System.out.println("[receiveMsg()] Received Message: "+receivedMsg);
                        return receivedMsg;
                }
            }
        } catch (IOException e) {
            System.out.println("[receiveMsg()] Receiving Message Failed (IOException)!");
            return receiveMsg();
        } catch (Exception e) {
            System.out.println("[receiveMsg()] Receiving Message Failed!"); e.printStackTrace();
        }
        return null;
    }
}
