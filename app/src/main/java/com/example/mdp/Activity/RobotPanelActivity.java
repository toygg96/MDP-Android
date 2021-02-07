package com.example.mdp.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private Button sendF1btn,sendF2btn,setF1btn,setF2btn, fastestPathBtn,explorationBtn, imageRecogBtn,setWaypointBtn,setOriginBtn,startBtn;
    private ImageButton upBtn,downBtn,leftBtn,rightBtn, micBtn;
    private TextView F1txtbox, F2txtbox,bluetoothConnectionTxtbox, robotStatusTxtbox;
    private String F1text, F2text;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private static final String MyPREFERENCES = "MyPrefs" ;
    private IntentFilter filter3, filter4, filter5;
    private ArenaView myMaze;

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
        F1txtbox = (TextView)findViewById(R.id.F1textBox);
        F2txtbox = (TextView)findViewById(R.id.F2textBox);
        bluetoothConnectionTxtbox = (TextView)findViewById(R.id.bluetoothConnectionTxtbox);
        robotStatusTxtbox = (TextView)findViewById(R.id.robotStatusTxtbox);
        upBtn = (ImageButton)findViewById(R.id.upBtn);
        downBtn = (ImageButton)findViewById(R.id.downBtn);
        leftBtn = (ImageButton)findViewById(R.id.leftBtn);
        rightBtn = (ImageButton)findViewById(R.id.rightBtn);
        micBtn = (ImageButton)findViewById(R.id.micBtn);

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

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:MoveForward");
            }
        });

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:MoveBackward");
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:RotateLeft");
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:RotateRight");
            }
        });

        fastestPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:Fastest");
            }
        });

        explorationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:Exploration");
            }
        });

        imageRecogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                BluetoothController.sendCmd("cmd:Recognition");
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
                BluetoothController.sendCmd("cmd:Recognition");
            }
        });
    }

    @Override
    protected void onPause() {
        unregisterReceiver(incomingMsgReceiver);
        unregisterReceiver(btConnectionStatusReceiver);
        unregisterReceiver(disconnectedReceiver);
        filter3 = null;
        filter4 = null;
        filter5 = null;
        super.onPause();

    }

    @Override
    protected void onResume() {
        Log.d(TAG,"resume called");
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
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

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
            Log.d("RobotPanelActivity",msg);
            if (msg.equalsIgnoreCase("MOVING:ROTATINGRIGHT")) {
                robotStatusTxtbox.setText("Rotating Right");
            } else if (msg.equalsIgnoreCase("MOVING:ROTATINGLEFT")) {
                robotStatusTxtbox.setText("Rotating Left");
            } else if (msg.equalsIgnoreCase("MOVING:FORWARD")) {
                robotStatusTxtbox.setText("Moving Forward");
            } else if (msg.equalsIgnoreCase("MOVING:REVERSE")) {
                robotStatusTxtbox.setText("Reversing");
            } else if (msg.equalsIgnoreCase("IDLE")) {
                robotStatusTxtbox.setText("Idle");
            } else if (msg.toLowerCase().contains("img:")) {
                String [] arrOfStr = msg.split(":");
                arrOfStr[1] = arrOfStr[1].replace("(", "");
                arrOfStr[1] = arrOfStr[1].replace(")", "");
                String [] strippedMsg = arrOfStr[1].split(",");
                myMaze.setDiscoveredImgOnCell(Integer.parseInt(strippedMsg[0]),Integer.parseInt(strippedMsg[1]),Integer.parseInt(strippedMsg[2]));
                myMaze.refreshMap();
            }
            if (msg.toLowerCase().contains("update:")) {
                String convertedMDF1 = hexToBinaryConverter.hexToBinary(msg.split(":")[1],true);
                String convertedMDF2 = hexToBinaryConverter.hexToBinary(msg.split(":")[2],false);
                Log.d(TAG,convertedMDF1);
                Log.d(TAG,convertedMDF2);
                myMaze.updateMaze(convertedMDF1,convertedMDF2,true);
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
            disableAllBtn();
        }
    };

    public void disableAllBtn(){
        upBtn.setEnabled(false);
        downBtn.setEnabled(false);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);
        sendF1btn.setEnabled(false);
        sendF2btn.setEnabled(false);
    }

    public void enableAllBtn(){
        upBtn.setEnabled(true);
        downBtn.setEnabled(true);
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
                        BluetoothController.sendCmd("cmd:MoveForward");
                    else if (result.get(0).toLowerCase().contains("backward") || result.get(0).toLowerCase().contains("reverse"))
                        BluetoothController.sendCmd("cmd:MoveBackward");
                    else if (result.get(0).toLowerCase().contains("left"))
                        BluetoothController.sendCmd("cmd:RotateLeft");
                    else if (result.get(0).toLowerCase().contains("right"))
                        BluetoothController.sendCmd("cmd:RotateRight");
                    else
                        Toast.makeText(this, "Cant understand your speech", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}