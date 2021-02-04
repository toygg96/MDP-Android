package com.example.mdp.Model;

import android.graphics.Paint;

//Cell Class
public class Cell {

    public float startX, startY, endX, endY;
    public Paint paint;

    public Cell(float startX, float startY, float endX, float endY, Paint paint) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.paint = paint;
    }

    public void setPaint(Paint paint){
        this.paint = paint;
    }
}