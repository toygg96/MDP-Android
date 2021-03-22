package com.example.mdp.Controller;

import android.util.Log;
import android.widget.TextView;

import com.example.mdp.Activity.ArenaView;
import com.example.mdp.Model.hexToBinaryConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class
QueueController extends Thread {
    private static final String TAG = "QueueController" ;
    private List <String> list;
    private TextView robotStatusTxtbox;
    private ArenaView myMaze;
    private boolean updateFlag = true;

    public QueueController(TextView robotStatusTxtbox){
        list = Collections.synchronizedList(new ArrayList<String>());
        this.robotStatusTxtbox = robotStatusTxtbox;
    }

    public void run() {
        while (true) {
            try {
                if (list.size() != 0) {
                    //Log.d(TAG, "Synchronized list size after adding: " + String.valueOf(list.size()));
                    String cmd = list.get(0);
                    Log.d(TAG, "Executing " + cmd);
                    if (cmd.equalsIgnoreCase("R|\n")) {
                        robotStatusTxtbox.setText("Rotating Right");
                        //myMaze.robotManualRotateRight(true);
                       // rotateSleep();
                    } else if (cmd.equalsIgnoreCase("A|\n")) {
                        robotStatusTxtbox.setText("Rotating Left");
                        //myMaze.robotManualRotateLeft(true);
                        //rotateSleep();
                    } else if (cmd.charAt(0) == 'F') {
                        //try {
                            robotStatusTxtbox.setText("Moving Forward");
                            //myMaze.robotMoveForward2(robotStatusTxtbox, cmd, true);
                        //} catch (Exception e) {
                        //    Log.e(TAG, "Move forward error.", e);
                        //}
                    } else if (cmd.equalsIgnoreCase("N|\n")) {
                        robotStatusTxtbox.setText("Exploration completed");
                    } else if (cmd.equalsIgnoreCase("C|\n")) {
                        robotStatusTxtbox.setText("Taking Picture");
                    } else if (cmd.toLowerCase().contains("img")) {
                        try {
                            String[] arrOfStr = cmd.split("\\|");
                            //                Log.d(TAG,arrOfStr[2]);
                            arrOfStr[1] = arrOfStr[1].replace("(", "");
                            arrOfStr[1] = arrOfStr[1].replace(")", "");
                            arrOfStr[1] = arrOfStr[1].replace("\n", "");
                            String[] strippedMsg = arrOfStr[1].split(",");
                            //                Log.d(TAG,strippedMsg[0]);
                            //                Log.d(TAG,strippedMsg[1]);
                            //                Log.d(TAG,strippedMsg[2]);
                            myMaze.setDiscoveredImgOnCell(Integer.parseInt(strippedMsg[0]), Integer.parseInt(strippedMsg[2]), Integer.parseInt(strippedMsg[1]));
                            String flippedImgString = "(" + strippedMsg[0] + "," + strippedMsg[2] + "," + strippedMsg[1] + ")";
                            BluetoothController.addImgString(flippedImgString);
                            if (updateFlag)
                                myMaze.refreshMap();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in receiving image status: ", e);
                        }
                    }
                    if (cmd.toLowerCase().contains("mdf")) {
                        BluetoothController.setMdfString(cmd.split("\\|")[1]);
                        BluetoothController.setMdfString2(cmd.split("\\|")[2]);
                        String convertedMDF1 = hexToBinaryConverter.hexToBinary(cmd.split("\\|")[1], true);
                        String convertedMDF2 = hexToBinaryConverter.hexToBinary(cmd.split("\\|")[2], false);
                        //Log.d(TAG,convertedMDF1);
                        //Log.d(TAG,convertedMDF2);
                        if (updateFlag) {
                            myMaze.updateMaze(convertedMDF1, convertedMDF2, true);
                        } else {
                            myMaze.updateMaze(convertedMDF1, convertedMDF2, false);
                        }

                    } else if (cmd.toLowerCase().contains("loc")) {
                        String robotCoordsDirection = cmd.split("\\|")[1];
                        robotCoordsDirection = robotCoordsDirection.replace("(", "");
                        robotCoordsDirection = robotCoordsDirection.replace(")", "");
                        String[] strippedRobotCoordsDirection = robotCoordsDirection.split(",");
                        int YCoord = Integer.parseInt(strippedRobotCoordsDirection[0]);
                        int XCoord = Integer.parseInt(strippedRobotCoordsDirection[1]);
                        String facingDirection = strippedRobotCoordsDirection[2];
                        //Log.d(TAG,"Direction: " + facingDirection);
                        if (updateFlag)
                            myMaze.setRobotLocationAndDirection(XCoord, YCoord, facingDirection, true);
                        else
                            myMaze.setRobotLocationAndDirection(XCoord, YCoord, facingDirection, false);

                    }
                    list.remove(0);
                    //Log.d(TAG, "Synchronized list size after adding: " + String.valueOf(list.size()));
                }
            } catch (Exception e) {
                Log.e(TAG,"Error in queue system",e);
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
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setUpdateFlag(boolean updateFlag) { this.updateFlag = updateFlag; }
}
