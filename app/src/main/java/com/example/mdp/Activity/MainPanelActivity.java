package com.example.mdp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mdp.Controller.BluetoothController;
import com.example.mdp.R;

import java.nio.charset.Charset;

public class MainPanelActivity extends AppCompatActivity {
    private Button sendF1btn,sendF2btn,setF1btn,setF2btn;
    private TextView F1textbox,F2textbox;
    private String F1text, F2text;

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

        setF1btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogicF1F2(v,true);
            }
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
                if (BluetoothController.getBluetoothThread() != null)
                    BluetoothController.getBluetoothThread().write(F1textbox.getText().toString().getBytes(Charset.defaultCharset()));
                else
                    BluetoothController.getAcceptedThread().write(F1textbox.getText().toString().getBytes(Charset.defaultCharset()));
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
                if (!F1) {
                    F2text = editText.getText().toString();
                    F2textbox.setText(F2text);
                } else {
                    F1text = editText.getText().toString();
                    F1textbox.setText(F1text);
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
            Intent i = new Intent(MainPanelActivity.this,MainActivity.class);
            startActivity(i);
            return true;
        }
        return false;
    }
}