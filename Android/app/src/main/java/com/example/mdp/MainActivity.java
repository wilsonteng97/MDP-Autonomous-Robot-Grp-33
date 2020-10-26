package com.example.mdp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import com.example.mdp.Chat.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.example.mdp.Chat.ChatAdapter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //Chat Log
    List<Message> chat = new ArrayList<Message>();
    RecyclerView chatboxLv;
    ChatAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    static final int SENT_BY_ROBOT = 2;
    static final int SENT_BY_REMOTE = 1;

    //Constant Variables
    static final int LEFT = 5;
    static final int UP = 3;
    static final int RIGHT = 1;
    static final int DOWN = 7;
    static final int COL = 15;
    static final int ROW = 20;

    //State of our program
    boolean bluetoothIsOn = false;
    boolean tiltSensorOn = false;
    boolean automaticeUpdate = true;

    //Data from storage
    PersistentFiles files;

    //View Components
    ImageView bt;
    GameView map;
    JoyStick joystick;
    InputMethodManager imm;
    EditText chatboxinput;
    Button BtnsearchDevices;
    Button manualUpdateMap;
//    Switch touchSwitch;
    Switch autoUpdateMapSwitch;
    Switch updateValueSwitch;
    Switch updateValueSwitch2;
    Switch tiltSensorSwitch;
    Switch automaticUpdateSwitch;
    TiltSensor t;
    SensorManager sm;
    Sensor s;
    ChatHandler chatHandler;
    TextView statusView;

    // Bluetooth Connection
    BluetoothAdapter bluetoothAdapter;

    //private static final UUID mdpUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter.isEnabled()){
            bt.setImageResource(R.mipmap.bluetoothon);
        }else{
            bt.setImageResource(R.mipmap.bluetoothoff);
        }
    }

    /*
    * INIT SECTION =================================================================================
    */

    //initialize components for chat log
    private void initChat () {
        chat.add(new Message("Hello! MDP Group 33 here!", 2));
        adapter = new ChatAdapter(this, chat);
        chatboxLv.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);
        chatboxLv.setLayoutManager(linearLayoutManager);
        chatboxLv.scrollToPosition(chat.size() - 1);
        chatHandler = new ChatHandler();
    }

    //initialize all the View variables
    private void initComponents () {
        statusView = findViewById(R.id.statusView);
        map = findViewById(R.id.map);
        chatboxinput = findViewById(R.id.chatboxinput);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        joystick = findViewById(R.id.joystick);
        bt = findViewById(R.id.bluetooth);
        BtnsearchDevices = findViewById(R.id.searchDevicesBtn);
        manualUpdateMap = findViewById(R.id.updateMap);
        files = new PersistentFiles(this);
        joystick.setOnJoystickMoveListener(new JoyStick.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, final int direction) {
                int transformedDir;
                switch (direction) {
                    case UP:
                        transformedDir = map.UP;
                        break;
                    case DOWN:
                        transformedDir = map.DOWN;
                        break;
                    case LEFT:
                        transformedDir = map.LEFT;
                        break;
                    case RIGHT:
                        transformedDir = map.RIGHT;
                        break;
                    default:
                        transformedDir = 90;
                        break;
                }
                if (power>=75) {
                    map.moveRobot(transformedDir, automaticeUpdate);
                }
            }
        }, 1000);
        activateSwitches();
        chatboxLv = findViewById(R.id.chatboxlv);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        s = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        t = new TiltSensor(sm, s, map);

        if (tiltSensorOn) {
            t.registerListener();
        } else {
            t.unregisterListener();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("IncomingMsg"));
    }

    //called during start up, initialize anything necessary
    private void init () {
        initComponents();
        initChat();
    }

    /*
     * INIT SECTION ================================================================================
     */

    /*
     * BLUETOOTH SECTION ===========================================================================
     */

    //turn on/off bluetooth
    public void toggleBluetooth (View view) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            String noSupportText = "Your device does not support Bluetooth :(";
            sendChat(noSupportText, SENT_BY_ROBOT);
        }else {
            if (bluetoothIsOn) {
                //turn off bluetooth
                bt.setImageResource(R.mipmap.bluetoothoff);
                bluetoothIsOn = false;
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                }
            } else {
                //turn on bluetooth
                if (bluetoothAdapter == null) {
                    String noSupportText = "Your device does not support Bluetooth :(";
                    sendChat(noSupportText, SENT_BY_ROBOT);
                }
                //DEVICE'S BLUETOOTH NOT ENABLED
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);
                }
                bt.setImageResource(R.mipmap.bluetoothon);
                bluetoothIsOn = true;
            }
        }
    }

    //go to search devices activity
    public void searchDevices(View view){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            try {
                Intent intent = new Intent(MainActivity.this, ConnectBT.class);
                startActivity(intent);
            }catch(Exception e){

            }
        } else {
            String BTnotONtext = "Please switch on bluetooth first";
            sendChat(BTnotONtext, SENT_BY_ROBOT);
        }
    }

    /*
     * BLUETOOTH SECTION
     */

    //send chat to chat log
    public void sendChat (String msg, int sender) {
        chat.add(new Message(msg, sender));
        adapter.notifyDataSetChanged();
        chatboxLv.smoothScrollToPosition(chat.size() - 1);
    }

    //for persistent data value 1
    public void loadData (View view) {
        String txt = files.loadDataString("v1");
        sendChat(txt, SENT_BY_ROBOT);
    }

    //for persistent data value 2
    public void loadData2 (View view) {
        String txt = files.loadDataString("v2");
        sendChat(txt, SENT_BY_ROBOT);
    }

    //save data to persistent storage
    public void saveData (String name, String text) {
        files.saveData(name, text);
    }

    boolean canUpdateValue1 = false;
    boolean canUpdateValue2 = false;

    //to send text to robot
    public void sendText (View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        //Do something from the text input
        String str = chatboxinput.getText().toString();
        chatboxinput.setCursorVisible(false);
        if (!canUpdateValue1 && !canUpdateValue2) {
            sendChat(str, SENT_BY_REMOTE);
            BluetoothCommunication.writeMsg(str.getBytes(Charset.defaultCharset()));
        }
        if (canUpdateValue1) {
            sendChat(str, SENT_BY_REMOTE);
            saveData("v1", str);
            sendChat("Value 1 changed to \""+str+"\"", SENT_BY_ROBOT);
        }
        if (canUpdateValue2) {
            sendChat(str, SENT_BY_REMOTE);
            saveData("v2", str);
            sendChat("Value 2 changed to \""+str+"\"", SENT_BY_ROBOT);
        }
        chatboxinput.setText("");
    }

    //to enable cursor once the text input is clicked
    public void enableCursor (View view) {
        chatboxinput.setCursorVisible(true);
    }

    public void activateSwitches () {
//        touchSwitch = findViewById(R.id.touchSwitch);
//        touchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                map.toggleTouchScreen();
//            }
//        });

        updateValueSwitch = findViewById(R.id.updateValue);
        updateValueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (canUpdateValue1) {
                    sendChat("Changing value stops.", SENT_BY_ROBOT);
                } else {
                    sendChat("Your subsequent messages will modify the predefined value 1.", SENT_BY_ROBOT);
                }
                canUpdateValue1 = !canUpdateValue1;
            }
        });

        updateValueSwitch2 = findViewById(R.id.updateValue2);
        updateValueSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (canUpdateValue2) {
                    sendChat("Changing value stops.", SENT_BY_ROBOT);
                } else {
                    sendChat("Your subsequent messages will modify the predefined value 2.", SENT_BY_ROBOT);
                }
                canUpdateValue2 = !canUpdateValue2;
            }
        });

        tiltSensorSwitch = findViewById(R.id.tiltSensorSwitch);
        tiltSensorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (tiltSensorOn) {
                    t.unregisterListener();
                    sendChat("Tilt sensor deactivated.", SENT_BY_ROBOT);
                } else {
                    t.registerListener();
                    sendChat("Tilt sensor activated.", SENT_BY_ROBOT);
                }
                tiltSensorOn = !tiltSensorOn;
            }
        });

        automaticUpdateSwitch = findViewById(R.id.updateSwitch);
        automaticUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                automaticeUpdate = b;
            }
        });
    }

    // Receiving important command from RPI
    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("receivingMsg");
        if (chatHandler.chatIsCommand(msg)) {
            String[] arrayOfCommand = chatHandler.splitCommand(msg);
            for (String cmd : arrayOfCommand) {
                selectAction(cmd);
            }
        } else {
            sendChat(msg, SENT_BY_ROBOT);
        }
        }
    };

    public void selectAction (String command) {
        try {
            String[] arr = command.split(":", 2);
            String commandWhat = arr[0];
            String commandContent = arr[1];
            if (commandWhat.equals("status")) {
                receiveMessageStatus(commandContent);
            } else if (commandWhat.equals("f")) {
                receiveMessageFastestPath(commandContent);
            } else if (commandWhat.equals("s")) {
                receiveMessageObstacle(commandContent, true);
            } else if(commandWhat.equals("M")) { //it is map descriptor
                receiveMessageDescriptor(commandContent);
            } else if(commandWhat.equals("m")){ //it is robot movement
                receiveMessageMovement(commandContent);
            }
        } catch (Exception e) {
            return;
        }
    }

    public void receiveMessageFastestPath (String str) {
        String movement;
        int n;
        for (int i = 0; i<str.length(); i+=2) {
            movement = str.substring(i, i+1);
            n = Integer.parseInt(str.substring(i+1, i+2));
            for (int j = 0; j<n; j++) {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                receiveMessageMovement(movement);
            }
        }
    }

    public void receiveMessageStatus (String str) {
        statusView.setText(str);
    }

    public void receiveMessageObstacle (String str, Boolean isSpecialObstacle) {
        try {
            String[] array = str.split("'");
            String[] specialObstacle;
            int id, x, y;
            for (String _ : array) {
                specialObstacle = _.split(",", 3);
                if (specialObstacle.length == 3) {
                    id = Math.round(Float.parseFloat(specialObstacle[0]));
                    x = Math.round(Float.parseFloat(specialObstacle[1]));
                    y = Math.round(Float.parseFloat(specialObstacle[2]));
                    if (isSpecialObstacle) {
                        map.addObstacle(x, 19-y, id);
                        //saveImageCoordinate();
                    } else {
                        map.addObstacle(x, 19-y, 16);
                    }
                }
            }
        } catch (Exception e) {
            Log.i("Error bro", e.toString());
            return;
        }
    }

    public void saveImageCoordinate () {
        saveData("imageCoordinate", generateImageCoordinateString());
    }

    public String generateImageCoordinateString () {
        ArrayList<ArrayList<Integer>> specialObstacle = map.getSpecialObstacle();
        String str = "{";
        for (ArrayList<Integer> obs : specialObstacle) {
            str += "("+Integer.toString(obs.get(0))+","+Integer.toString(obs.get(1))+","+Integer.toString(19-obs.get(2))+"),";
        }
        str = str.substring(0, str.length()-1);
        str += "}";
        return str;
    }

    public void receiveMessageDescriptor (String str) {
        String[] arr = str.split(":", 2);
        String map1;
        String map2;
        try {
            map1 = arr[0]; //map part 1
            map2 = arr[1]; //map part 2
        } catch (Exception e) {
            return;
        }
        try {
            map1 = MapDescriptorFormat.hexToBin(map1);
            map2 = MapDescriptorFormat.hexToBin(map2);
        } catch (Exception e) {
            return;
        }
        map.setMapDescriptor(map1);
        map.setMapDescriptor2(map2);

        int x = 0;
        int y = 0;
        for (int i = 2; i<map1.length()-2; i++) {
            map.addExplored(x, 19-y, map1.charAt(i));
            x++;
            if (x == 15) {
                x = 0;
                y++;
            }
        }
        receiveMessageDescriptor2(map2);
    }

    public void receiveMessageDescriptor2 (String str) {
        String mapDescriptor = map.getMapDescriptor();
        int x = 0;
        int y = 0;
        int iReal = 0;
        for (int i = 2; i<mapDescriptor.length()-2; i++) {
            if (mapDescriptor.charAt(i) == '1') { //if it is explored
                try {
                    if (str.charAt(iReal) == '1') {
                        map.addObstacle(x, 19-y, GameView.OBSTACLE);
                    }
                    iReal++;
                } catch (StringIndexOutOfBoundsException e) {
                    return;
                }
            }
            x++;
            if (x == 15) {
                x = 0;
                y++;
            }
        }
    }

    public void receiveMessageMovement (String str) {
        switch (str) {
            case "A": //left
                statusView.setText("Rotate Left");
                map.moveRobot(map.ROTATELEFT, automaticeUpdate);
                break;
            case "D": //right
                statusView.setText("Rotate Right");
                map.moveRobot(map.ROTATERIGHT, automaticeUpdate);
                break;
            case "W": //forward
                statusView.setText("Move Forward");
                map.moveForward(automaticeUpdate);
                break;
            case "S": //backward
                statusView.setText("Move Backward");
                map.moveBackward(automaticeUpdate);
                break;
            default:
                break;
        }
    }

    public void updateMapManual (View view) {
        map.updateMapManual();
    }

    public void startDiscovery (View view) {
        String str = "ES|";
        sendChat("Start Discovery!", SENT_BY_REMOTE);
        BluetoothCommunication.writeMsg(str.getBytes(Charset.defaultCharset()));
    }

    public void sendWaypoint (View view) {
        String str = chatboxinput.getText().toString();
        try {
            String[] arr = str.split(",");
            int x = Integer.parseInt(arr[0]);
            int y = Integer.parseInt(arr[1]);
//            map.addObstacle(x, 19-y, map.WAYPOINT);
            map.setWaypoint(x, 19-y, automaticeUpdate);

            //Send to RPi
            str = "WP|"+arr[0]+"|"+arr[1];
            BluetoothCommunication.writeMsg(str.getBytes(Charset.defaultCharset()));

            //Send to chat
            String str2 = "Waypoint: "+arr[0]+", "+arr[1];
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            chatboxinput.setCursorVisible(false);
            sendChat(str2, SENT_BY_REMOTE);
            chatboxinput.setText("");
        } catch (Exception e) {
            return;
        }
    }

    public void resetMap (View view) {
        map.setMapDescriptor("");
        map.setMapDescriptor2("");
        map.resetMap();
    }

    public void getMapDescriptor (View view) {
        String map1 = map.getMapDescriptor();
        String map2 = map.getMapDescriptor2();
        String map1ToRpi = "map1:"+map1;
        String map2ToRpi = "map2:"+map2;
        sendChat("Map Descriptor Part 1: "+map1, SENT_BY_ROBOT);
        sendChat("Map Descriptor Part 2: "+map2, SENT_BY_ROBOT);
        BluetoothCommunication.writeMsg(map1ToRpi.getBytes(Charset.defaultCharset()));
        BluetoothCommunication.writeMsg(map2ToRpi.getBytes(Charset.defaultCharset()));
    }

    public void sendFastestPath (View view) {
        String str = "FS|";
        sendChat("Do fastest path!", SENT_BY_REMOTE);
        BluetoothCommunication.writeMsg(str.getBytes(Charset.defaultCharset()));
    }

    public void sendImagesCoordinate (View view) {
        sendChat(generateImageCoordinateString(), SENT_BY_ROBOT);
    }

//    public void emergencyImg (View view) {
//        String img = files.loadDataString("imageCoordinate");
//        img = "img:"+img;
//        sendChat(img, SENT_BY_ROBOT);
//        BluetoothCommunication.writeMsg("IMAGE".getBytes(Charset.defaultCharset()));
//    }

//    public void emergencyMap (View view) {
//        String map1 = files.loadDataString("map1");
//        String map2 = files.loadDataString("map2");
//        map1 = "map1:"+map1;
//        map2 = "map2:"+map2;
//        sendChat(map1, SENT_BY_ROBOT);
//        sendChat(map2, SENT_BY_ROBOT);
//        BluetoothCommunication.writeMsg("MDF".getBytes(Charset.defaultCharset()));
//    }

}