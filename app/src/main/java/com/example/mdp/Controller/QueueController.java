package com.example.mdp.Controller;

import android.util.Log;
import android.widget.TextView;

import com.example.mdp.Activity.ArenaView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueController extends Thread {
    private static final String TAG = "QueueController" ;
    private List <String> list;
    private TextView robotStatusTxtbox;
    private ArenaView myMaze;

    public QueueController(TextView robotStatusTxtbox){
        list = Collections.synchronizedList(new ArrayList<String>());
        this.robotStatusTxtbox = robotStatusTxtbox;
    }

    public void run() {
        while (true) {
            if (list.size() != 0) {
                //Log.d(TAG, "Synchronized list size after adding: " + String.valueOf(list.size()));
                String cmd = list.get(0);
                Log.d(TAG,"Executing " + cmd);
                if (cmd.equalsIgnoreCase("R|\n") ) {
                    robotStatusTxtbox.setText("Rotating Right");
                    myMaze.robotManualRotateRight(true);
                    rotateSleep();
                } else if (cmd.equalsIgnoreCase("A|\n")) {
                    robotStatusTxtbox.setText("Rotating Left");
                    myMaze.robotManualRotateLeft(true);
                    rotateSleep();
                } else if (cmd.charAt(0) == 'F') {
                    try {
                        robotStatusTxtbox.setText("Moving Forward");
                        myMaze.robotMoveForward2(robotStatusTxtbox, cmd, true);
                    } catch (Exception e) {
                        Log.e(TAG,"Move forward error." , e);
                    }
                }
                list.remove(0);
                //Log.d(TAG, "Synchronized list size after adding: " + String.valueOf(list.size()));
            }
        }
    }

    public void setMyMaze(ArenaView myMaze){
        this.myMaze = myMaze;
    }

    public void addMessageToQueue(String message){
        list.add(message);
    }

    public void rotateSleep(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
