package com.example.mdp.Model;

import android.graphics.Paint;

//Cell Class
public class Cell {

    public float startX, startY, endX, endY;
    public Paint paint;
    public boolean imgFlag;
    public int imgID = -1;

    public Cell(float startX, float startY, float endX, float endY, Paint paint) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.paint = paint;
        this.imgFlag = false;
    }

    public void setPaint(Paint paint){
        this.paint = paint;
    }

    public Paint getPaint() { return this.paint; }

    public void setImgFlag(boolean imgFlag){ this.imgFlag = imgFlag; }

    public void setImgID(int imgID) { this.imgID = imgID; }
}