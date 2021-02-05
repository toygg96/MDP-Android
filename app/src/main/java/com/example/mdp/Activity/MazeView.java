package com.example.mdp.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.mdp.Model.Cell;

public class MazeView extends View {

    private static final String TAG = "MazeView";
    private static Cell[][] cells;
    private static final int COLS = 15, ROWS = 20;
    private static final float WallThickness = 2;
    private static float cellSizeX,cellSizeY, hMargin, vMargin;
    private static Paint wallPaint, robotPaint, waypointPaint, directionPaint,  emptyPaint, virtualWallPaint, obstaclePaint, unexploredPaint, ftpPaint, endPointPaint, gridNumberPaint;
    private static int robotRow = 18, robotCols = 1, wayPointRow =-1, wayPointCols=-1;
    private static String robotDirection = "east";
    private static boolean setRobotPostition = false, setWayPointPosition = false;
    private static boolean createCellStatus = false;

    //CONSTRUCTOR
    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        Log.d(TAG,"Mazeview non empty constructor called");
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
        Log.d(TAG,"Width: " + String.valueOf(width));
        Log.d(TAG,"Height: " + String.valueOf(height));
        cellSizeX = (float) 453 / COLS;
        cellSizeY = (float) 465 / ROWS;

        //CALCULATE MARGIN SIZE FOR THE CANVAS
        hMargin = (width - COLS * cellSizeX) / 11;
        vMargin = (height - ROWS * cellSizeY) / 2;

        Log.d(TAG,"Cell sizeX: " + String.valueOf(cellSizeX));
        Log.d(TAG,"Cell sizeY: " + String.valueOf(cellSizeY));
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

        int coordinates[];
        float x = event.getX();
        float y = event.getY();

        coordinates = findCoordinatesOnMap(x, y);
        Log.d(TAG, "(" + coordinates[0] + " , " + coordinates[1] + ')');

        if (setRobotPostition) {
            //ENSURE ONTOUCH IS WITHIN THE MAZE
            if (coordinates[0] != -1 && coordinates[1] != -1) {

                //ENSURE COORDINATES IS NOT THE FIRST OR LAST ROW/COLS AS THE ROBOT IS PLOT BASED ON THE CENTER COORDINATES
                if ((coordinates[0] != 0 && coordinates[0] != 14) && (coordinates[1] != 0 && coordinates[1] != 19)) {
                    robotCols = coordinates[0];
                    robotRow = coordinates[1];
                    invalidate();

                    // send start point to RPI

                }
            }
        } else if (setWayPointPosition) {

            //ENSURE ONTOUCH IS WITHIN THE MAZE
            if (coordinates[0] != -1 && coordinates[1] != -1) {

                wayPointCols = coordinates[0];
                wayPointRow = coordinates[1];
                invalidate();

                // send waypoint to RPI

            }
        }

        return super.onTouchEvent(event);
    }

    public void createAllPaint(){

        //PAINT FOR END POINT
        endPointPaint = new Paint();
        endPointPaint.setColor(Color.RED);

        //PAINT THE THICKNESS OF THE WALL
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
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
        unexploredPaint.setColor(Color.GRAY);

        gridNumberPaint = new Paint();
        gridNumberPaint.setColor(Color.BLACK);
        gridNumberPaint.setTextSize(12);
        gridNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);

        //COLOR FOR FASTEST PATH
        ftpPaint = new Paint();
        ftpPaint.setColor(Color.parseColor("#FFC0CB"));
    }

    //CREATE Cell METHOD
    private void createCell() {
        cells = new Cell[COLS][ROWS];

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {

                cells[x][y] = new Cell(x * cellSizeX, y * cellSizeY, (x + 1) * cellSizeX, (y + 1) * cellSizeY, unexploredPaint);

            }
        }

    }

    public void drawEverything(Canvas canvas){
        //DRAW BORDER FOR EACH CELL
        drawBorder(canvas);

        //DRAW EACH INDIVIDUAL CELL
        //drawCell(canvas);

        //DRAW GRID NUMBER
        drawGridNumber(canvas);

        //DRAW ENDPOINT ON MAZE
        drawEndPoint(canvas);

        //DRAW ROBOT ON MAZE
        drawRobot(canvas);

        //DRAW WAY POINT ON MAZE
        drawWayPoint(canvas);
    }

    //DRAW ENDPOINT CELL
    private void drawEndPoint(Canvas canvas) {

        for (int x = 12; x < COLS; x++) {
            for (int y = 0; y < 3; y++) {

                //DRAW EACH INDIVIDUAL CELL
                canvas.drawRect(cells[x][y].startX,cells[x][y].startY,cells[x][y].endX,cells[x][y].endY,endPointPaint);

            }
        }
    }

    //DRAW INDIVIDUAL CELL
    private void drawCell(Canvas canvas){

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {

                //DRAW EACH INDIVIDUAL CELL
                canvas.drawRect(cells[x][y].startX,cells[x][y].startY,cells[x][y].endX,cells[x][y].endY,cells[x][y].paint);

            }
        }
    }

    //DRAW BORDER FOR EACH CELL
    private void drawBorder(Canvas canvas){

        //DRAW BORDER FOR EACH CELL
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Log.d(TAG, String.valueOf("X: " + x));
                Log.d(TAG, String.valueOf("Y: " + y));
                //DRAW LINE FOR TOPWALL OF CELL
//                canvas.drawLine(cells[x][y].startY,
//                        cells[x][y].startX,
//                        cells[x][y].endY,
//                        cells[x][y].startX, wallPaint);
                canvas.drawLine(
                        cells[x][y].startX,
                        cells[x][y].startY,
                        cells[x][y].endX,
                        cells[x][y].startY, wallPaint);
                if (y == 19)
                    canvas.drawLine(
                            cells[x][19].startX,
                            cells[x][19].endY,
                            cells[x][19].endX,
                            cells[x][19].endY, wallPaint);
            }
        }
    }

    //DRAW ROBOT ON THE CANVAS
    private void drawRobot(Canvas canvas) {

        float halfWidth = (cells[robotCols][robotRow - 1].endX - cells[robotCols][robotRow - 1].startX) / 2;

        // DRAW COLOR FOR MIDDLE OF ROBOT
        canvas.drawRect(cells[robotCols][robotRow].startX, cells[robotCols][robotRow].startY, cells[robotCols][robotRow].endX, cells[robotCols][robotRow].endY, robotPaint);

        // DRAW COLOR FOR THE RECT LEFT OF MID POINT
        canvas.drawRect(cells[robotCols - 1][robotRow].startX, cells[robotCols - 1][robotRow].startY, cells[robotCols - 1][robotRow].endX, cells[robotCols - 1][robotRow].endY, robotPaint);

        //  DRAW COLOR FOR THE RECT RIGHT OF MID POINT
        canvas.drawRect(cells[robotCols + 1][robotRow].startX, cells[robotCols + 1][robotRow].startY, cells[robotCols + 1][robotRow].endX, cells[robotCols + 1][robotRow].endY, robotPaint);

        //  DRAW COLOR FOR THE RECT BELOW MID POINT
        canvas.drawRect(cells[robotCols][robotRow - 1].startX, cells[robotCols][robotRow - 1].startY, cells[robotCols][robotRow - 1].endX, cells[robotCols][robotRow - 1].endY, robotPaint);

        //  DRAW COLOR FOR THE RECT ABOVE MID POINT
        canvas.drawRect(cells[robotCols][robotRow + 1].startX, cells[robotCols][robotRow + 1].startY, cells[robotCols][robotRow + 1].endX, cells[robotCols][robotRow + 1].endY, robotPaint);

        // DRAW COLOR FOR LOWER LEFT EDGE
        canvas.drawRect(cells[robotCols - 1][robotRow - 1].startX, cells[robotCols - 1][robotRow - 1].startY, cells[robotCols - 1][robotRow - 1].endX, cells[robotCols - 1][robotRow - 1].endY, robotPaint);

        // DRAW COLOR FOR UPPER LEFT EDGE
        canvas.drawRect(cells[robotCols - 1][robotRow + 1].startX, cells[robotCols - 1][robotRow + 1].startY, cells[robotCols - 1][robotRow + 1].endX, cells[robotCols - 1][robotRow + 1].endY, robotPaint);


        // DRAW COLOR FOR UPPER RIGHT EDGE
        canvas.drawRect(cells[robotCols + 1][robotRow + 1].startX, cells[robotCols + 1][robotRow + 1].startY, cells[robotCols + 1][robotRow + 1].endX, cells[robotCols + 1][robotRow + 1].endY, robotPaint);

        // DRAW COLOR FOR LOWER RIGHT EDGE
        canvas.drawRect(cells[robotCols + 1][robotRow - 1].startX, cells[robotCols + 1][robotRow - 1].startY, cells[robotCols + 1][robotRow - 1].endX, cells[robotCols + 1][robotRow - 1].endY, robotPaint);

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

    //DRAW ROBOT ON THE CANVAS
    private void drawWayPoint(Canvas canvas) {

        if(wayPointRow != -1 && wayPointCols != -1) {
            canvas.drawRect(cells[wayPointCols][wayPointRow].startX, cells[wayPointCols][wayPointRow].startY, cells[wayPointCols][wayPointRow].endX, cells[wayPointCols][wayPointRow].endY, waypointPaint);
        }
    }

    //DRAW NUMBERS ON MAP GRID
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


    //FIND COORDIANTES OF THE CELLMAZE BASED ON ONTOUCH
    private int[] findCoordinatesOnMap(float x, float y) {

        int row = -1, cols = -1;

        //FIND COLS OF THE MAZE BASED ON ONTOUCH
        for (int i = 0; i < COLS; i++) {

            if (x <= cells[i][0].endX && x >=  cells[i][0].startX) {
                cols = i;
                Log.d(TAG, "X = " + String.valueOf(x));

                break;
            }
        }
        //FIND ROW OF THE MAZE BASED ON ONTOUCH
        for (int j = 0; j < ROWS; j++) {

            if (y <= cells[0][j].endY && y >=  cells[0][j].startY) {
                row = (j-19)*-1;
                Log.d(TAG, "Y = " + String.valueOf(y));

                break;
            }

        }

        return new int[]{cols, row};
    }

    //ALLOW USER TO SET WAYPOINT POSITION
    public void setWayPoint(boolean status){
        setWayPointPosition = status;
    }

    //ALLOW USER TO SET ROBOT POSITION
    public void setStartPoint(boolean status){
        setRobotPostition = status;
    }

    //RETURN START POINT OF ROBOT FOR BUTTON CLICK
    public int[] getRobotStartPoint(){
        return new int[] {robotCols, robotRow};
    }

    //RETURN WAYPOINT FOR BUTTON CLICK
    public int[] getWaypoint(){
        return new int[] {wayPointCols, wayPointRow};
    }

    //UPDATE MAZE WHEN MAZE INFO ARRIVES
    public void updateMaze(String[] mazeInfo,boolean autoUpdate){

        robotDirection = mazeInfo[1];
        robotCols = Integer.parseInt(mazeInfo[2]);
        robotRow = 19 - Integer.parseInt(mazeInfo[3]);

        int counter =0;

        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLS; y++) {

            // HANDLE UPDATING OF MAP

            }

        }

        //ENSURE AUTO UPDATE TOGGLE BUTTON IS ON
        if(autoUpdate) {
            invalidate();
        }
        Log.d(TAG, "Stage 4: ");


    }

    //REFRESH THE MAZE
    public void refreshMap(){
        invalidate();
    }

}