package com.example.mdp.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mdp.Controller.BluetoothController;
import com.example.mdp.Model.Cell;
import com.example.mdp.R;
import com.example.mdp.Controller.BluetoothController;

import org.w3c.dom.Text;

import java.nio.charset.Charset;

import static android.content.ContentValues.TAG;

public class ArenaView extends View{

    private static final String TAG = "ArenaView";
    private static Cell[][] cells;
    private static final int COLS = 15, ROWS = 20;
    private static final float WallThickness = 1;
    private static float cellSizeX,cellSizeY, hMargin, vMargin;
    private static Paint wallPaint, robotPaint, waypointPaint, directionPaint,  emptyPaint, virtualWallPaint, obstaclePaint, unexploredPaint, ftpPaint, endPointPaint, gridNumberPaint, exploredPaint;
    private static int robotRow = 18, robotCols = 1, wayPointRow =-1, wayPointCols=-1;
    private static String robotDirection = "north";
    private static boolean setRobotPostition = false, setWayPointPosition = false;
    private static boolean createCellStatus = false;
    private static int imgXCoord = -1,imgYCoord= -1;

    //CONSTRUCTOR
    public ArenaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        createAllPaint();
    }

    //DRAW SHAPES ON CANVAS
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG,"onDraw() called");

        //BACKGROUND COLOR OF CANVAS
        canvas.drawColor(Color.WHITE);

        //WIDTH OF THE CANVAS
        int width = getWidth();
        //HEIGHT OF THE CANVAS
        int height = getHeight();
        //Log.d(TAG,"Width: " + String.valueOf(width));
        //Log.d(TAG,"Height: " + String.valueOf(height));
        cellSizeX = (float) 493 / COLS;
        cellSizeY = (float) 599 / ROWS;

        //CALCULATE MARGIN SIZE FOR THE CANVAS
        hMargin = (width - COLS * cellSizeX) / 11;
        vMargin = (height - ROWS * cellSizeY) / 2;

        //Log.d(TAG,"Cell sizeX: " + String.valueOf(cellSizeX));
        //Log.d(TAG,"Cell sizeY: " + String.valueOf(cellSizeY));
        //CREATE CELL ONCE
        if(!createCellStatus) {
            //CREATE CELL COORDINATES
            Log.d(TAG,"CREATE CELL");
            createCell();
            createCellStatus = true;
        }

        //SET THE MARGIN IN PLACE
        canvas.translate(40, 0);
        drawEverything(canvas);
        setRobotPostition = false;

    }

    //ON TOUCH METHOD
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        int  coordinates[] = findCoordinatesOnMap(x, y);
        Log.d(TAG, "(" + coordinates[0] + "," + coordinates[1] + ')');

        if (setRobotPostition) {
            //ENSURE ONTOUCH IS WITHIN THE MAZE
            if (coordinates[0] != -1 && coordinates[1] != -1) {

                //ENSURE COORDINATES IS NOT THE FIRST OR LAST ROW/COLS AS THE ROBOT IS PLOT BASED ON THE CENTER COORDINATES
                if ((coordinates[0] != 0 && coordinates[0] != 14) && (coordinates[1] != 0 && coordinates[1] != 19)) {
                    robotCols = coordinates[0];
                    robotRow = getInverseYCoord(coordinates[1]);
                    refreshMap();
                    BluetoothController.sendCmd("SC|(" + String.valueOf(coordinates[0]) + "," + String.valueOf(coordinates[1]) + ")");
                    setRobotPostition = false;

                }
            }
        } else if (setWayPointPosition) {

            //ENSURE ONTOUCH IS WITHIN THE MAZE
            if (coordinates[0] != -1 && coordinates[1] != -1) {

                wayPointCols = coordinates[0];
                wayPointRow = getInverseYCoord(coordinates[1]);
                refreshMap();
                BluetoothController.sendCmd("WP|(" + String.valueOf(coordinates[0]) + "," + String.valueOf(coordinates[1]) + ")");
                setWayPointPosition = false;

            }
        }

        return super.onTouchEvent(event);
    }

    public void createAllPaint(){

        //PAINT FOR END POINT
        endPointPaint = new Paint();
        endPointPaint.setColor(Color.GREEN);

        //PAINT THE THICKNESS OF THE WALL
        wallPaint = new Paint();
        wallPaint.setColor(Color.parseColor("#58ACFF"));
        wallPaint.setStrokeWidth(WallThickness);

        //COLOR FOR ROBOT
        robotPaint = new Paint();
        robotPaint.setColor(Color.GREEN);

        //COLOR FOR ROBOT DIRECTION
        directionPaint = new Paint();
        directionPaint.setColor(Color.BLACK);

        //COLOR FOR WAY POINT
        waypointPaint = new Paint();
        waypointPaint.setColor(Color.YELLOW);

        //COLOR FOR EXPLORED BUT EMPTY
        emptyPaint = new Paint();
        emptyPaint.setColor(Color.WHITE);

        //COLOR FOR VIRTUAL WALL
        virtualWallPaint = new Paint();
        virtualWallPaint.setColor(Color.parseColor("#FFA500"));

        //COLOR FOR OBSTACLE
        obstaclePaint = new Paint();
        obstaclePaint.setColor(Color.BLACK);

        //COLOR FOR UNEXPLORED PATH
        unexploredPaint = new Paint();
        unexploredPaint.setColor(Color.parseColor("#0E79E5"));

        gridNumberPaint = new Paint();
        gridNumberPaint.setColor(Color.BLACK);
        gridNumberPaint.setTextSize(12);
        gridNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);

        //COLOR FOR FASTEST PATH
        ftpPaint = new Paint();
        ftpPaint.setColor(Color.parseColor("#FFC0CB"));

        // Explored paint
        exploredPaint = new Paint();
        exploredPaint.setColor(Color.WHITE);

    }

    private void createCell() {
        cells = new Cell[COLS][ROWS];

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                cells[x][y] = new Cell(x * cellSizeX, y * cellSizeY, (x + 1) * cellSizeX, (y + 1) * cellSizeY, unexploredPaint);
            }
        }

    }

    private void recreateCell() {
        cells = new Cell[COLS][ROWS];

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                cells[x][y] = new Cell(x * cellSizeX, y * cellSizeY, (x + 1) * cellSizeX, (y + 1) * cellSizeY, unexploredPaint);
                cells[x][y].setImgFlag(false);
                cells[x][y].setImgID(-1);

            }
        }

    }

    public void drawEverything(Canvas canvas){
        drawCell(canvas);
        drawBorder(canvas);
        drawGridNumber(canvas);
        drawEndPoint(canvas);
        drawRobot(canvas);
        drawWayPoint(canvas);
        drawDiscoveredImgs(canvas);
    }

    private void drawEndPoint(Canvas canvas) {
        for (int x = 12; x < COLS; x++) {
            for (int y = 0; y < 3; y++) {

                //DRAW EACH INDIVIDUAL CELL
                canvas.drawRect(cells[x][y].startX,cells[x][y].startY,cells[x][y].endX,cells[x][y].endY,endPointPaint);

            }
        }
    }

    private void drawCell(Canvas canvas){
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                //DRAW EACH INDIVIDUAL CELL
                canvas.drawRect(cells[x][y].startX,cells[x][y].startY,cells[x][y].endX,cells[x][y].endY,cells[x][y].paint);
            }
        }
    }

    private void drawBorder(Canvas canvas){
        //DRAW BORDER FOR EACH CELL
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                //Log.d(TAG, String.valueOf("X: " + x));
                //Log.d(TAG, String.valueOf("Y: " + y));
                //DRAW VERTICAL LINES
                canvas.drawLine(cells[x][y].startX,
                        cells[x][y].startY,
                        cells[x][y].startX,
                        cells[x][y].endY, wallPaint);

                //DRAW HORIZONTAL LINES
                canvas.drawLine(
                        cells[x][y].startX,
                        cells[x][y].startY,
                        cells[x][y].endX,
                        cells[x][y].startY, wallPaint);
            }
        }

        for (int x = 0; x < COLS; x++) {
                canvas.drawLine(
                        cells[x][19].startX,
                        cells[x][19].endY,
                        cells[x][19].endX,
                        cells[x][19].endY, wallPaint);
        }

        for (int y = 0; y < ROWS; y++) {
            canvas.drawLine(
                    cells[14][y].endX,
                    cells[14][y].startY,
                    cells[14][y].endX,
                    cells[14][y].endY, wallPaint);
        }
    }

    private void drawRobot(Canvas canvas) {
        //Rect rec =
        Bitmap robot = BitmapFactory.decodeResource(
                getContext().getResources(),
                R.drawable.robot2
        );

        int xCoord = (int) cells[robotCols-1][robotRow+1].startX;
        int yCoord = (int) cells[robotCols-1][robotRow+1].startY;
        int x2Coord = (int) cells[robotCols-1][robotRow+1].endX;
        int y2Coord = (int) cells[robotCols-1][robotRow+1].endY;
        Rect rec = new Rect(xCoord, (int)(yCoord-(2*cellSizeY)),(int)(x2Coord+(2*cellSizeX)),y2Coord);
        canvas.drawBitmap(robot, null, rec, null);
        drawDirectionArrow(canvas);
    }

    private void drawDirectionArrow(Canvas canvas) {

        float halfWidth = (cells[robotCols][robotRow - 1].endX - cells[robotCols][robotRow - 1].startX) / 2;

        //TRIANGLE FOR ROBOT DIRECTION
        Path path = new Path();

        switch (robotDirection){
            case "north":
                path.moveTo(cells[robotCols][robotRow - 1].startX + halfWidth, cells[robotCols][robotRow - 1].startY); // Top
                path.lineTo(cells[robotCols][robotRow - 1].startX, cells[robotCols][robotRow - 1].endY); // Bottom left
                path.lineTo(cells[robotCols][robotRow - 1].endX, cells[robotCols][robotRow - 1].endY); // Bottom right
                path.lineTo(cells[robotCols][robotRow - 1].startX + halfWidth, cells[robotCols][robotRow - 1].startY); // Back to Top
                break;

            case "south":
                path.moveTo(cells[robotCols][robotRow + 1].endX - halfWidth, cells[robotCols][robotRow + 1].endY); // Top
                path.lineTo(cells[robotCols][robotRow + 1].startX, cells[robotCols][robotRow + 1].startY); // Bottom left
                path.lineTo(cells[robotCols + 1][robotRow + 1].startX, cells[robotCols +1][robotRow + 1].startY); // Bottom right
                path.lineTo(cells[robotCols][robotRow + 1].endX - halfWidth, cells[robotCols][robotRow + 1].endY); // Back to Top
                break;

            case "east":
                path.moveTo(cells[robotCols+1][robotRow].startX + (2*halfWidth), cells[robotCols][robotRow].startY + halfWidth); // Top
                path.lineTo(cells[robotCols+1][robotRow].startX, cells[robotCols+1][robotRow].startY); // Bottom left
                path.lineTo(cells[robotCols+1][robotRow+1].startX, cells[robotCols+1][robotRow+1].startY); // Bottom right
                path.lineTo(cells[robotCols+1][robotRow].startX + (2*halfWidth) , cells[robotCols][robotRow].startY + halfWidth); // Back to Top
                break;

            case "west":
                path.moveTo(cells[robotCols-1][robotRow].startX, cells[robotCols][robotRow].startY + halfWidth); // Top
                path.lineTo(cells[robotCols][robotRow].startX, cells[robotCols][robotRow].startY); // Bottom left
                path.lineTo(cells[robotCols][robotRow + 1].startX, cells[robotCols][robotRow  +1].startY); // Bottom right
                path.lineTo(cells[robotCols-1][robotRow].startX, cells[robotCols][robotRow].startY + halfWidth); // Back to Top
                break;
        }
        path.close();
        canvas.drawPath(path, directionPaint);
    }

    private void drawWayPoint(Canvas canvas) {
        if(wayPointRow != -1 && wayPointCols != -1) {
            canvas.drawRect(cells[wayPointCols][wayPointRow].startX, cells[wayPointCols][wayPointRow].startY, cells[wayPointCols][wayPointRow].endX, cells[wayPointCols][wayPointRow].endY, waypointPaint);
        }
    }

    private void drawGridNumber(Canvas canvas) {
        //GRID NUMBER FOR COLUMN
        for (int x = 0; x < 15; x++) {
            if(x > 9){
                canvas.drawText(Integer.toString(x), cells[x][19].startX + (cellSizeX / 6), cells[x][19].endY + (cellSizeY), gridNumberPaint);
            } else {
                //GRID NUMBER FOR ROW
                canvas.drawText(Integer.toString(x), cells[x][19].startX + (cellSizeX / 4), cells[x][19].endY + (cellSizeY), gridNumberPaint);
            }
        }

        //GRID NUMBER FOR ROW
        for (int y = 0;y <20;y++) {
            if(y >9){
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX - (cellSizeX / 1.5f), cells[0][y].endY - (cellSizeY / 3.5f), gridNumberPaint);
            }
            else {
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX - (cellSizeX / 1.2f), cells[0][y].endY - (cellSizeY / 3.5f), gridNumberPaint);
            }
        }
    }

    public void drawDiscoveredImgs(Canvas canvas){

            for (int x = 0; x < COLS; x++) {
                for (int y = 0; y < ROWS; y++) {
                    if (cells[x][y].imgFlag) {
                        Bitmap discoveredImg = null;
                        switch (cells[x][y].imgID) {

                            case -1:
                                break;

                            case 1:
                                 discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num1);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 2:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num2);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 3:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num3);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 4:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num4);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 5:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num5);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 6:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num6);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 7:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num7);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 8:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num8);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 9:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num9);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 10:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num10);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 11:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num11);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 12:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num12);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 13:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num13);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 14:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num14);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;

                            case 15:
                                discoveredImg = BitmapFactory.decodeResource(
                                        getContext().getResources(),
                                        R.drawable.num15);
                                drawDiscoveredImg(canvas,discoveredImg,x,y);
                                break;
                        }
                    }
                }
            }

    }

    public void drawDiscoveredImg(Canvas canvas, Bitmap discoveredImg, int x, int y){
        Rect rec = new Rect((int) cells[x][y].startX, (int) cells[x][y].startY, (int) cells[x][y].endX, (int) cells[x][y].endY);
        canvas.drawBitmap(discoveredImg, null, rec, null);
    }

    public void setDiscoveredImgOnCell(int imgID, int XCoord, int YCoord) {
        if (imgID < 1 || imgID > 15) {
            Log.d(TAG,"ImgID invalid!");
            return;
        }
        cells[XCoord][getInverseYCoord(YCoord)].setImgFlag(true);
        cells[XCoord][getInverseYCoord(YCoord)].setImgID(imgID);
    }

    private int[] findCoordinatesOnMap(float x, float y) {

        int row = -1, cols = -1;

        //FIND COLS OF THE MAZE BASED ON ONTOUCH
        for (int i = 0; i < COLS; i++) {
            if ((x - 40) <= cells[i][0].endX && (x - 40) >= cells[i][0].startX) {
                cols = i;
                break;
            }
        }
        //FIND ROW OF THE MAZE BASED ON ONTOUCH
        for (int j = 0; j < ROWS; j++) {
            if (y <= cells[0][j].endY && y >= cells[0][j].startY) {
                row = (j-19)*-1;
                break;
            }

        }

        return new int[]{cols, row};
    }

    public int getInverseYCoord(int YCoord){
        return ((YCoord-19)*-1);
    }

    public void setWayPoint(boolean status){
        setWayPointPosition = status;
    }

    public void setStartPoint(boolean status){
        setRobotPostition = status;
    }

    public int[] getRobotStartPoint(){
        return new int[] {robotCols, robotRow};
    }

    public int[] getWaypoint(){
        return new int[] {wayPointCols, wayPointRow};
    }

    public void updateMaze(String mdfString1,String mdfString2, boolean autoUpdate){

        int counter = 0;

        try {
            for (int y = 0; y < ROWS; y++) {
                for (int x = 0; x < COLS; x++) {
                    char tmp = mdfString1.charAt(counter);
                    if (tmp == '1') {
                        cells[x][getInverseYCoord(y)].setPaint(exploredPaint);
                        //Log.d(TAG,"(" + String.valueOf(x) + "," + String.valueOf(y) + ")");
                    }
                    counter++;
                }

            }

        counter = 0;
            for (int y = 0; y < ROWS; y++) {
                for (int x = 0; x < COLS; x++) {
                    Paint tmpPaint = cells[x][getInverseYCoord(y)].getPaint();
                    if (tmpPaint.getColor() == exploredPaint.getColor()) {
                        char tmp = mdfString2.charAt(counter);
                        if (tmp == '1') {
                            cells[x][getInverseYCoord(y)].setPaint(obstaclePaint);
                            //Log.d(TAG,"(" + String.valueOf(x) + "," + String.valueOf(y) + ")");
                        } else if (tmp == '0') {
                            cells[x][getInverseYCoord(y)].setPaint(emptyPaint);
                            //Log.d(TAG,"(" + String.valueOf(x) + "," + String.valueOf(y) + ")");
                        }
                        counter++;
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG,"Error: " ,e);
        }

        //ENSURE AUTO UPDATE TOGGLE BUTTON IS ON
        if(autoUpdate) {
            refreshMap();
        }
        //Log.d(TAG, "Stage 4: ");

    }

    // For fastest path string command (E.g P|F01|F01|L|F01) theres no $ to signify end of string
    public void updateMaze2(String[] instructions, boolean autoUpdate, TextView robotStatusTxtbox){

        //robotCols = XCoord;
        //robotRow = getInverseYCoord(YCoord);
        //robotDirection = facingDirection;
        for (String instruction: instructions) {
            //Log.d(TAG,instruction);
            switch (instruction) {
                case "F01":
                    robotMoveForward(robotStatusTxtbox,autoUpdate);
                    break;
                case "L":
                    robotRotateLeft(robotStatusTxtbox,autoUpdate);
                    break;
                case "L1":
                    robotRotateLeft(robotStatusTxtbox,autoUpdate);
                    robotMoveForward(robotStatusTxtbox,autoUpdate);
                    break;
                case "R":
                    robotRotateRight(robotStatusTxtbox,autoUpdate);
                    break;
                case "R1":
                    robotRotateRight(robotStatusTxtbox,autoUpdate);
                    robotMoveForward(robotStatusTxtbox,autoUpdate);
                    break;
            }
        }
    }

    public void updateMaze3(String instruction, boolean autoUpdate){

        switch (instruction) {
            case "F01":
                if (robotDirection.equalsIgnoreCase("north"))
                    if((robotRow - 1)  < 1)
                        Log.d(TAG,"INVALID MOVEMENT");
                    else
                        robotRow -= 1;
                else if (robotDirection.equalsIgnoreCase("south"))
                    if ((robotRow + 1) > 18)
                        Log.d(TAG,"INVALID MOVEMENT");
                    else
                        robotRow += 1;
                else if (robotDirection.equalsIgnoreCase("east"))
                    if ((robotCols + 1)  > 13)
                        Log.d(TAG,"INVALID MOVEMENT");
                    else
                        robotCols += 1;
                else if (robotDirection.equalsIgnoreCase("west"))
                    if((robotCols - 1) < 1)
                        Log.d(TAG,"INVALID MOVEMENT");
                    else
                        robotCols -= 1;
                if (autoUpdate) {
                    refreshMap();
                    break;
                }
            case "L":
                robotManualRotateLeft(autoUpdate);
                break;
            case "R":
                robotManualRotateRight(autoUpdate);
                break;
        }

    }

    // For fastest path string command (E.g P|R|F12|L|F17$) theres a $ to signify end of string
    public void updateMaze4(String[] instructions, boolean autoUpdate, TextView robotStatusTxtbox){
            for (String instruction: instructions) {
                instruction = instruction.replace("$","");
                if (instruction.charAt(0) == 'F') {
                    int numOfSteps = Integer.parseInt(instruction.substring(1, instruction.length()));
                    while (numOfSteps != 0) {
                        robotStatusTxtbox.setText("Moving forward");
                        if (robotDirection.equalsIgnoreCase("north")) {
                            robotRow -= 1;
                            numOfSteps -= 1;
                        } else if (robotDirection.equalsIgnoreCase("south")) {
                            robotRow += 1;
                            numOfSteps -= 1;
                        } else if (robotDirection.equalsIgnoreCase("east")) {
                            robotCols += 1;
                            numOfSteps -= 1;
                        } else if (robotDirection.equalsIgnoreCase("west")) {
                            robotCols -= 1;
                            numOfSteps -= 1;
                        }

                        if (autoUpdate) {
                            refreshMap();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e){
                            Log.e(TAG,"Error in waiting",e);
                        }
                    }
                }
                //Log.d(TAG,"Message: " + instruction);
                if (instruction.equalsIgnoreCase("L")) {
                    robotRotateLeft(robotStatusTxtbox, autoUpdate);
                } else if (instruction.equalsIgnoreCase("R")){
                    robotRotateRight(robotStatusTxtbox,autoUpdate);
                } else if (instruction.equalsIgnoreCase("L1")){
                    robotRotateLeft(robotStatusTxtbox, autoUpdate);
                    robotMoveForward(robotStatusTxtbox,autoUpdate);
                } else if (instruction.equalsIgnoreCase("R1")){
                    robotRotateRight(robotStatusTxtbox, autoUpdate);
                    robotMoveForward(robotStatusTxtbox,autoUpdate);
                }
            }

    }

    public void setRobotLocationAndDirection(int XCoord, int YCoord, String facingDirection,boolean autoUpdate){
        robotCols = XCoord;
        robotRow = getInverseYCoord(YCoord);
        robotDirection = facingDirection.toLowerCase();
        if (autoUpdate)
            refreshMap();
    }

    public void refreshMap(){
        invalidate();
    }

    public void resetMap() {
        recreateCell();
        robotCols = 1;
        robotRow = 18;
        wayPointCols = -1;
        wayPointRow = -1;
        refreshMap();
    }

    public void robotMoveForward(TextView robotStatusTxtbox, boolean autoUpdate){
        robotStatusTxtbox.setText("Moving forward");
        if (robotDirection.equalsIgnoreCase("north")) {
            robotRow -= 1;
        } else if (robotDirection.equalsIgnoreCase("south")) {
            robotRow += 1;
        } else if (robotDirection.equalsIgnoreCase("east")) {
            robotCols += 1;
        } else if (robotDirection.equalsIgnoreCase("west")) {
            robotCols -= 1;
        }

        if (autoUpdate) {
            refreshMap();
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            Log.e(TAG,"Error in waiting",e);
        }
    }

    public void robotRotateLeft(TextView robotStatusTxtbox, boolean autoUpdate){
        robotStatusTxtbox.setText("Rotating left");
        if (robotDirection.equalsIgnoreCase("north"))
            robotDirection = "west";
        else if (robotDirection.equalsIgnoreCase("south"))
            robotDirection = "east";
        else if (robotDirection.equalsIgnoreCase("east"))
            robotDirection = "north";
        else if (robotDirection.equalsIgnoreCase("west"))
            robotDirection = "south";
        if (autoUpdate) {
            refreshMap();
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                Log.e(TAG,"Error in waiting",e);
            }
        }
    }

    public void robotRotateRight(TextView robotStatusTxtbox, boolean autoUpdate){
        robotStatusTxtbox.setText("Rotating right");
        if (robotDirection.equalsIgnoreCase("north"))
            robotDirection = "east";
        else if (robotDirection.equalsIgnoreCase("south"))
            robotDirection = "west";
        else if (robotDirection.equalsIgnoreCase("east"))
            robotDirection = "south";
        else if (robotDirection.equalsIgnoreCase("west"))
            robotDirection = "north";
        if (autoUpdate) {
            refreshMap();
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                Log.e(TAG,"Error in waiting",e);
            }
        }
    }

    public void robotManualRotateLeft(boolean autoUpdate){
        if (robotDirection.equalsIgnoreCase("north"))
            robotDirection = "west";
        else if (robotDirection.equalsIgnoreCase("south"))
            robotDirection = "east";
        else if (robotDirection.equalsIgnoreCase("east"))
            robotDirection = "north";
        else if (robotDirection.equalsIgnoreCase("west"))
            robotDirection = "south";
        if (autoUpdate)
            refreshMap();

    }

    public void robotManualRotateRight(boolean autoUpdate){
        if (robotDirection.equalsIgnoreCase("north"))
            robotDirection = "east";
        else if (robotDirection.equalsIgnoreCase("south"))
            robotDirection = "west";
        else if (robotDirection.equalsIgnoreCase("east"))
            robotDirection = "south";
        else if (robotDirection.equalsIgnoreCase("west"))
            robotDirection = "north";
        if (autoUpdate)
            refreshMap();
    }

}