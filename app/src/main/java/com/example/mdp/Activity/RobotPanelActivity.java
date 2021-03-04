package com.example.mdp.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Scroller;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mdp.Model.hexToBinaryConverter;
import com.example.mdp.Controller.BluetoothController;
import com.example.mdp.R;

import java.util.ArrayList;
import java.util.Locale;

public class RobotPanelActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private Button sendF1btn,sendF2btn,setF1btn,setF2btn, fastestPathBtn,explorationBtn, imageRecogBtn,setWaypointBtn,setOriginBtn,startBtn,resetMapBtn,mdfBtn, imageStrBtn;
    private ImageButton upBtn,leftBtn,rightBtn, micBtn,refreshBtn, msgHistoryBtn;
    private TextView F1txtbox, F2txtbox,bluetoothConnectionTxtbox, robotStatusTxtbox;
    private Switch autoUpdateSwitch;
    private String F1text, F2text;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private static final String MyPREFERENCES = "MyPrefs" ;
    private IntentFilter filter3, filter4, filter5;
    private ArenaView myMaze;
    private boolean updateFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_panel);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.scaledbluetooth);

        setF1btn =(Button)findViewById(R.id.setF1btn);
        setF2btn=(Button)findViewById(R.id.setF2btn);
        sendF1btn =(Button)findViewById(R.id.F1btn);
        sendF2btn =(Button)findViewById(R.id.F2btn);
        fastestPathBtn = (Button)findViewById(R.id.fastestPathBtn);
        explorationBtn = (Button)findViewById(R.id.explorationBtn);
        imageRecogBtn = (Button)findViewById(R.id.imageRecogBtn);
        setOriginBtn = (Button)findViewById(R.id.setOriginBtn);
        setWaypointBtn = (Button)findViewById(R.id.setWaypointBtn);
        startBtn = (Button)findViewById(R.id.startBtn);
        resetMapBtn = (Button)findViewById(R.id.resetMapBtn);
        mdfBtn = (Button)findViewById(R.id.mdfBtn);
        imageStrBtn = (Button)findViewById(R.id.imageBtn);
        F1txtbox = (TextView)findViewById(R.id.F1textBox);
        F2txtbox = (TextView)findViewById(R.id.F2textBox);
        bluetoothConnectionTxtbox = (TextView)findViewById(R.id.bluetoothConnectionTxtbox);
        robotStatusTxtbox = (TextView)findViewById(R.id.robotStatusTxtbox);
        upBtn = (ImageButton)findViewById(R.id.upBtn);
        leftBtn = (ImageButton)findViewById(R.id.leftBtn);
        rightBtn = (ImageButton)findViewById(R.id.rightBtn);
        micBtn = (ImageButton)findViewById(R.id.micBtn);
        refreshBtn = (ImageButton)findViewById(R.id.refreshBtn);
        msgHistoryBtn = (ImageButton)findViewById(R.id.msgHistoryBtn);
        autoUpdateSwitch = (Switch)findViewById(R.id.updateSwitch);

        if (autoUpdateSwitch.isChecked())
            updateFlag = true;

        SharedPreferences sh = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        bluetoothConnectionTxtbox.setText("Connected to:\n"+ BluetoothController.getConnectedDevice());

        // The value will be default as empty string because for
        // the very first time when the app is opened, there is nothing to show
        String F1text = sh.getString("F1String", "");
        String F2text = sh.getString("F2String", "");

        // We can then use the data
        F1txtbox.setText(F1text);
        F2txtbox.setText(F2text);

        BluetoothController.init(this, BluetoothAdapter.getDefaultAdapter(),BluetoothController.getAdapter());

        BluetoothController.sendCmd("SS");

        setF1btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onClickLogicF1F2(v,true); }
        });

        setF2btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogicF1F2(v,false);
            }
        });

        sendF1btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd(F1txtbox.getText().toString());
            }
        });

        sendF2btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd(F2txtbox.getText().toString());
            }
        });

        mdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMDFlogic(v);
            }
        });

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("F01|");
                if (updateFlag)
                    myMaze.updateMaze3("F01",true);
                else
                    myMaze.updateMaze3("F01",false);
                robotStatusTxtbox.setText("Moving forward");
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("L0|");
                if (updateFlag)
                    myMaze.updateMaze3("L0",true);
                else
                    myMaze.updateMaze3("L0",false);
                robotStatusTxtbox.setText("Rotating left");
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("R0|");
                if (updateFlag)
                    myMaze.updateMaze3("R0",true);
                else
                    myMaze.updateMaze3("R0",false);
                robotStatusTxtbox.setText("Rotating right");
            }
        });

        fastestPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("FP|START");
            }
        });

        explorationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("EX|START");
            }
        });

        imageRecogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("IMG|START");
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                speak();
            }
        });

        setOriginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                myMaze.setStartPoint(true);
            }
        });

        setWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                myMaze.setWayPoint(true);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("ARD|AND|START|");
            }
        });

        autoUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()) {
                    updateFlag = true;
                    myMaze.refreshMap();
                }  else
                    updateFlag = false;
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                myMaze.refreshMap();
            }
        });

        resetMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                myMaze.resetMap();
            }
        });

        imageStrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                onClickImageStrLogic(r);
            }
        });

        msgHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                onClickMsgHistoryLogic(r);
            }
        });
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(incomingMsgReceiver);
            unregisterReceiver(btConnectionStatusReceiver);
            unregisterReceiver(disconnectedReceiver);
            filter3 = null;
            filter4 = null;
            filter5 = null;
            super.onPause();
        } catch (Exception e) {
            Log.e(TAG,"Error in unregistering receiver", e);
        }
    }

    @Override
    protected void onResume() {
        //Log.d(TAG,"resume called");
        if (myMaze == null)
            myMaze = findViewById(R.id.mapView);
        registerReceivers();
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothController.getBluetoothThread() != null)
            BluetoothController.getBluetoothThread().cancel();
        // Don't forget to unregister the ACTION_FOUND receiver.
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(incomingMsgReceiver);
            unregisterReceiver(btConnectionStatusReceiver);
            unregisterReceiver(disconnectedReceiver);
        } catch(IllegalArgumentException e) {
            Log.d(TAG,"Receiver not registered");
        }
    }

    public void registerReceivers(){
        if ((filter3 == null) || (filter4 == null) || (filter5 == null)) {
            //Log.d(TAG,"is this called");
            filter3 = new IntentFilter("IncomingMsg");
            registerReceiver(incomingMsgReceiver, filter3);
            filter4 = new IntentFilter("btConnectionStatus");
            registerReceiver(btConnectionStatusReceiver, filter4);
            filter5 = new IntentFilter("disconnectedMsg");
            registerReceiver(disconnectedReceiver, filter5);
        }
    }

    public void onClickLogicF1F2(View v,boolean F1){
        AlertDialog dialogBuilder = new AlertDialog.Builder(v.getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fn_dialog, null);

        EditText editText = (EditText) dialogView.findViewById(R.id.edt_comment);
        Button okBtn = (Button) dialogView.findViewById(R.id.okBtn);
        Button cancelBtn = (Button) dialogView.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                editor = sharedpreferences.edit();
                if (!F1) {
                    F2text = editText.getText().toString();
                    F2txtbox.setText(F2text);
                    editor.putString("F2String", F2text);
                    editor.commit();
                } else {
                    F1text = editText.getText().toString();
                    F1txtbox.setText(F1text);
                    editor.putString("F1String", F1text);
                    editor.commit();
                }
                dialogBuilder.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHINGS
                dialogBuilder.cancel();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void onClickMDFlogic(View v){
        AlertDialog dialogBuilder = new AlertDialog.Builder(v.getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.nice_mdf_dialog, null);

        TextView mdfTxtview = (TextView) dialogView.findViewById(R.id.mdfTV);
        TextView mdfTxtview2 = (TextView) dialogView.findViewById(R.id.mdfTV2);
        Button closeBtn = (Button) dialogView.findViewById(R.id.okDialogBtn);
        mdfTxtview.setText(BluetoothController.getMdfString());
        mdfTxtview2.setText(BluetoothController.getMdfString2());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHINGS
                dialogBuilder.cancel();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void onClickImageStrLogic(View v){
        AlertDialog dialogBuilder = new AlertDialog.Builder(v.getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.nice_image_string_dialog, null);

        TextView imgStrTxtview = (TextView) dialogView.findViewById(R.id.imgStringsTV);
        Button closeBtn = (Button) dialogView.findViewById(R.id.okDialogBtn2);
        imgStrTxtview.setText(BluetoothController.getImgStrings());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHINGS
                dialogBuilder.cancel();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void onClickMsgHistoryLogic(View v){
        AlertDialog dialogBuilder = new AlertDialog.Builder(v.getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.message_chat_dialog, null);

        TextView msgLogTV = (TextView) dialogView.findViewById(R.id.msgLogTV);
        Button closeBtn = (Button) dialogView.findViewById(R.id.okDialogBtn3);
        msgLogTV.setText(BluetoothController.getMsgLog());
        msgLogTV.setScroller(new Scroller(this));
        msgLogTV.setVerticalScrollBarEnabled(true);
        msgLogTV.setMovementMethod(new ScrollingMovementMethod());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHINGS
                dialogBuilder.cancel();
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //Do stuff
            //Log.d(TAG, "Clicked main page");
            Intent i = new Intent(RobotPanelActivity.this, BluetoothSettingsActivity.class);
            startActivity(i);
            return true;
        }
        return false;
    }

    // Create a BroadcastReceiver for Receive message.
    public BroadcastReceiver incomingMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String log = BluetoothController.getMsgLog();
            String msg = intent.getStringExtra("receivingMsg");
            //Log.d("RobotPanelActivity",msg);
            if (msg.equalsIgnoreCase("R0")) {
                robotStatusTxtbox.setText("Rotating Right");
            } else if (msg.equalsIgnoreCase("L0")) {
                robotStatusTxtbox.setText("Rotating Left");
            } else if (msg.equalsIgnoreCase("F01")) {
                robotStatusTxtbox.setText("Moving Forward");
            } else if (msg.equalsIgnoreCase("N")) {
                robotStatusTxtbox.setText("Exploration completed");
            } else if (msg.equalsIgnoreCase("C")) {
                robotStatusTxtbox.setText("Taking Picture");
            } else if (msg.toLowerCase().contains("img")) {
                try {
                    String[] arrOfStr = msg.split("\\|");
//                Log.d(TAG,arrOfStr[2]);
                    BluetoothController.addImgString(arrOfStr[2]);
                    arrOfStr[2] = arrOfStr[2].replace("(", "");
                    arrOfStr[2] = arrOfStr[2].replace(")", "");
                    String[] strippedMsg = arrOfStr[2].split(",");
//                Log.d(TAG,strippedMsg[0]);
//                Log.d(TAG,strippedMsg[1]);
//                Log.d(TAG,strippedMsg[2]);
                    myMaze.setDiscoveredImgOnCell(Integer.parseInt(strippedMsg[0]), Integer.parseInt(strippedMsg[1]), Integer.parseInt(strippedMsg[2]));
                    if (updateFlag)
                        myMaze.refreshMap();
                } catch (Exception e) {
                    Log.e(TAG,"Error: ", e);
                }
            }
            if (msg.toLowerCase().contains("mdf")) {
                try {
                    BluetoothController.setMdfString(msg.split("\\|")[1]);
                    BluetoothController.setMdfString2(msg.split("\\|")[2]);
                    Log.d(TAG, msg.split("\\|")[3]);
                    String convertedMDF1 = hexToBinaryConverter.hexToBinary(msg.split("\\|")[1], true);
                    String convertedMDF2 = hexToBinaryConverter.hexToBinary(msg.split("\\|")[2], false);
                    //Log.d(TAG,convertedMDF1);
                    //Log.d(TAG,convertedMDF2);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            if (updateFlag) {
                                myMaze.updateMaze(convertedMDF1, convertedMDF2, true);
                            } else {
                                myMaze.updateMaze(convertedMDF1, convertedMDF2, false);
                            }
                        }
                    };
                    thread.start();
                } catch (Exception e) {
                    Log.e(TAG,"Error", e);
                }
            } else if (msg.toLowerCase().contains("fp")) {
                try {
                    String[] instructions = msg.split("\\|");
                    if (msg.toLowerCase().contains("f02") || msg.toLowerCase().contains("f03") || msg.toLowerCase().contains("f04") || msg.toLowerCase().contains("f05")
                            || msg.toLowerCase().contains("f06") || msg.toLowerCase().contains("f07") || msg.toLowerCase().contains("f08") || msg.toLowerCase().contains("f09")
                            || msg.toLowerCase().contains("f10") | msg.toLowerCase().contains("f11") | msg.toLowerCase().contains("f12") || msg.toLowerCase().contains("f13")
                            || msg.toLowerCase().contains("f14") || msg.toLowerCase().contains("f15") || msg.toLowerCase().contains("f16") || msg.toLowerCase().contains("f17")) {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (updateFlag)
                                        myMaze.updateMaze4(instructions, true, robotStatusTxtbox);
                                    else
                                        myMaze.updateMaze4(instructions, false, robotStatusTxtbox);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error in multithread: ", e);
                                }
                            }
                        };
                        thread.start();
                    } else {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (updateFlag)
                                        myMaze.updateMaze2(instructions, true, robotStatusTxtbox);
                                    else
                                        myMaze.updateMaze2(instructions, false, robotStatusTxtbox);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error in multithread: ", e);
                                }
                            }
                        };
                        thread.start();
                    }
                } catch (Exception e) {
                    Log.e(TAG,"Error ", e);
                }
            } else if (msg.toLowerCase().contains("loc")) {
                try {
                    String robotCoordsDirection = msg.split("\\|")[1];
                    robotCoordsDirection = robotCoordsDirection.replace("(", "");
                    robotCoordsDirection = robotCoordsDirection.replace(")", "");
                    String[] strippedRobotCoordsDirection = robotCoordsDirection.split(",");
                    int XCoord = Integer.parseInt(strippedRobotCoordsDirection[0]);
                    int YCoord = Integer.parseInt(strippedRobotCoordsDirection[1]);
                    String facingDirection = strippedRobotCoordsDirection[2];
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (updateFlag)
                                    myMaze.setRobotLocationAndDirection(XCoord, YCoord, facingDirection, true);
                                else
                                    myMaze.setRobotLocationAndDirection(XCoord, YCoord, facingDirection, false);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in multithread: ", e);
                            }
                        }
                    };
                    thread.start();
                } catch (Exception e){
                    Log.e(TAG,"Error: ",e);
                }
            }
            BluetoothController.saveMsgLog(log + "\n" + msg);
        }
    };

    public BroadcastReceiver btConnectionStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            enableAllBtn();
            String msg = intent.getStringExtra("Device");
            BluetoothController.setConnectedDevice(msg);
            BluetoothController.setActiveFlag(true);
            bluetoothConnectionTxtbox.setText("Connected to:\n"+ BluetoothController.getConnectedDevice());

        }
    };

    public BroadcastReceiver disconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothController.getAcceptedThread().cancel();
            BluetoothController.startBluetoothServer();
            BluetoothController.setConnectThreadToNull();
            BluetoothController.setConnectedDevice("");
            BluetoothController.setActiveFlag(false);
            bluetoothConnectionTxtbox.setText("Connected to:\n"+ BluetoothController.getConnectedDevice());
            Toast.makeText(context, "Disconnected! Attempting to reconnect." + BluetoothController.getConnectedDevice(),Toast.LENGTH_SHORT).show();
            BluetoothController.attemptConnection(BluetoothController.getConnectedBluetoothDevice(), true);
            disableAllBtn();
        }
    };

    public void disableAllBtn(){
        upBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);
        sendF1btn.setEnabled(false);
        sendF2btn.setEnabled(false);
    }

    public void enableAllBtn(){
        upBtn.setEnabled(true);
        leftBtn.setEnabled(true);
        rightBtn.setEnabled(true);
        sendF1btn.setEnabled(true);
        sendF2btn.setEnabled(true);
    }

    public void speak(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say something!");

        try{
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT:{
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).toLowerCase().contains("up") || result.get(0).toLowerCase().contains("forward"))
                        BluetoothController.sendCmd("F01");
                    else if (result.get(0).toLowerCase().contains("left"))
                        BluetoothController.sendCmd("L0");
                    else if (result.get(0).toLowerCase().contains("right"))
                        BluetoothController.sendCmd("R0");
                    else
                        Toast.makeText(this, "Cant understand your speech", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}