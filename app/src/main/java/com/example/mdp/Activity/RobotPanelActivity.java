package com.example.mdp.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mdp.Controller.BluetoothController;
import com.example.mdp.R;

import java.nio.charset.Charset;

public class RobotPanelActivity extends AppCompatActivity {
    private Button sendF1btn,sendF2btn,setF1btn,setF2btn, fastestPathBtn,explorationBtn, imageRecogBtn;
    private ImageButton upBtn,downBtn,leftBtn,rightBtn;
    private TextView F1txtbox, F2txtbox,bluetoothConnectionTxtbox;
    private String F1text, F2text;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private static final String MyPREFERENCES = "MyPrefs" ;
    private IntentFilter filter3, filter4, filter5;

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
        F1txtbox = (TextView)findViewById(R.id.F1textBox);
        F2txtbox = (TextView)findViewById(R.id.F2textBox);
        bluetoothConnectionTxtbox = (TextView)findViewById(R.id.bluetoothConnectionTxtbox);
        upBtn = (ImageButton)findViewById(R.id.upBtn);
        downBtn = (ImageButton)findViewById(R.id.downBtn);
        leftBtn = (ImageButton) findViewById(R.id.leftBtn);
        rightBtn = (ImageButton)findViewById(R.id.rightBtn);
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
                sendCmd(F1txtbox.getText().toString());
            }
        });

        sendF2btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd(F2txtbox.getText().toString());
            }
        });

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:MoveForward");
            }
        });

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:MoveBackward");
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:RotateLeft");
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:RotateRight");
            }
        });

        fastestPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:Fastest");
            }
        });

        explorationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:Exploration");
            }
        });

        imageRecogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                sendCmd("cmd:Recognition");
            }
        });
    }

    @Override
    protected void onPause() {
        unregisterReceiver(incomingMsgReceiver);
        unregisterReceiver(btConnectionStatusReceiver);
        unregisterReceiver(disconnectedReceiver);
        super.onPause();

    }

    @Override
    protected void onResume() {
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
            Log.d(TAG,"is this called");
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
            String msg = intent.getStringExtra("receivingMsg");
            Log.d("RobotPanelActivity",msg);
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

    public void sendCmd(String cmdString){
        //Log.d(TAG,String.valueOf(BluetoothController.getAcceptedThread() == null));
        if (BluetoothController.getBluetoothThread() != null)
            BluetoothController.getBluetoothThread().write(cmdString.getBytes(Charset.defaultCharset()));
        else {
            try {
                BluetoothController.getAcceptedThread().write(cmdString.getBytes(Charset.defaultCharset()));
            } catch (Exception e) {
                Log.e(TAG, "Crashed here", e);
            }
        }
    }
}