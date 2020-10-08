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
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import com.example.mdp.Chat.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp.Chat.ChatAdapter;

import java.nio.charset.Charset;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static androidx.core.content.ContextCompat.getSystemService;

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
    Switch touchSwitch;
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

    private static final UUID mdpUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private void initChat () {
        chat.add(new Message("Hello! MDP Group 33 here!", 2));
        adapter = new ChatAdapter(this, chat);
        chatboxLv.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);
        chatboxLv.setLayoutManager(linearLayoutManager);
        chatboxLv.scrollToPosition(chat.size() - 1);
        chatHandler = new ChatHandler();
    }

    public void sendChat (String msg, int sender) {
        chat.add(new Message(msg, sender));
        adapter.notifyDataSetChanged();
        chatboxLv.smoothScrollToPosition(chat.size() - 1);
    }

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
                        transformedDir = 90;
                        break;
                    case DOWN:
                        transformedDir = 270;
                        break;
                    case LEFT:
                        transformedDir = 180;
                        break;
                    case RIGHT:
                        transformedDir = 0;
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

    //for persistent data storage testing
    public void loadData (View view) {
        String txt = files.loadDataString("v1");
        sendChat(txt, SENT_BY_ROBOT);
    }

    public void loadData2 (View view) {
        String txt = files.loadDataString("v2");
        sendChat(txt, SENT_BY_ROBOT);
    }

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
        touchSwitch = findViewById(R.id.touchSwitch);
        touchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                map.toggleTouchScreen();
            }
        });

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
            String[] arr = chatHandler.splitCommand(msg);
            if (arr[0].equals("status")) {
                receiveMessageStatus(arr[1]);
            } else if (arr[0].equals("obstacle")) {
                receiveMessageObstacle(arr[1], false);
            } else if (arr[0].equals("sobstacle")) {
                receiveMessageObstacle(arr[1], true);
            } else if(arr[0].equals("M")) { //it is map descriptor
                receiveMessageDescriptor(arr[1]);
            } else if(arr[0].equals("m")){ //it is robot movement
                receiveMessageMovement(arr[1]);
            }
        } else {
            sendChat(msg, SENT_BY_ROBOT);
        }
        }
    };

    public void receiveMessageStatus (String str) {
        statusView.setText(str);
    }

    public void receiveMessageObstacle (String str, Boolean isSpecialObstacle) {
        String[] listOfPos = str.split(";");
        int x, y;
        String[] temp;
        for(String pos:listOfPos) {
            temp = pos.split(",");
            x = Integer.parseInt(temp[0]);
            y = Integer.parseInt(temp[1]);
            if (isSpecialObstacle) {
                map.addObstacle(x, y, Integer.parseInt(temp[2]));
            } else {
                map.addObstacle(x, y, 16);
            }
        }
    }

    public void receiveMessageDescriptor (String str) {
        String[] arr = str.split("\\|", 2);
        String map1 = arr[0]; //map part 1
        String map2 = arr[1]; //map part 2
        map1 = MapDescriptorFormat.hexToBin(map1);
        map2 = MapDescriptorFormat.hexToBin(map2);
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
                map.moveRobot(map.ROTATELEFT, automaticeUpdate);
                break;
            case "D": //right
                map.moveRobot(map.ROTATERIGHT, automaticeUpdate);
                break;
            case "W": //forward
                map.moveForward(automaticeUpdate);
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
            map.addObstacle(x, y, map.WAYPOINT);
            str = "WP|"+arr[0]+"!"+arr[1];
            BluetoothCommunication.writeMsg(str.getBytes(Charset.defaultCharset()));
            sendText(view);
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
        sendChat("Map Descriptor Part 1: "+map1, SENT_BY_ROBOT);
        sendChat("Map Descriptor Part 2: "+map2, SENT_BY_ROBOT);
    }

    public void sendFastestPath (View view) {
        String str = "FS|";
        sendChat("Do fastest path!", SENT_BY_ROBOT);
        BluetoothCommunication.writeMsg(str.getBytes(Charset.defaultCharset()));
    }

    public void sendWaypoint (int x, int y) {
        map.addObstacle(x, y, 20);
    }
}