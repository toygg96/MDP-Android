package com.example.mdp.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mdp.Controller.BluetoothController;
import com.example.mdp.R;

import java.nio.charset.Charset;

public class RobotPanelActivity extends AppCompatActivity {
    private Button sendF1btn,sendF2btn,setF1btn,setF2btn;
    private TextView F1textbox,F2textbox;
    private String F1text, F2text;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private static final String MyPREFERENCES = "MyPrefs" ;

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
        F1textbox = (TextView)findViewById(R.id.F1textBox);
        F2textbox = (TextView)findViewById(R.id.F2textBox);
        SharedPreferences sh = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // The value will be default as empty string because for
        // the very first time when the app is opened, there is nothing to show
        String F1text = sh.getString("F1String", "");
        String F2text = sh.getString("F2String", "");

        // We can then use the data
        F1textbox.setText(F1text);
        F2textbox.setText(F2text);

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
                Log.d(TAG,String.valueOf(BluetoothController.getAcceptedThread() == null));
                if (BluetoothController.getBluetoothThread() != null)
                    BluetoothController.getBluetoothThread().write(F1textbox.getText().toString().getBytes(Charset.defaultCharset()));
                else {
                    try {
                        BluetoothController.getAcceptedThread().write(F1textbox.getText().toString().getBytes(Charset.defaultCharset()));
                    } catch (Exception e) {
                        Log.e(TAG, "Crashed here", e);
                    }
                }
            }
        });

        sendF2btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                if (BluetoothController.getBluetoothThread() != null)
                    BluetoothController.getBluetoothThread().write(F2textbox.getText().toString().getBytes(Charset.defaultCharset()));
                else
                    BluetoothController.getAcceptedThread().write(F2textbox.getText().toString().getBytes(Charset.defaultCharset()));
            }
        });
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
                    F2textbox.setText(F2text);
                    editor.putString("F2String", F2text);
                    editor.commit();

                } else {
                    F1text = editText.getText().toString();
                    F1textbox.setText(F1text);
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
}