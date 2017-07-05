package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;

/**
 * Created by Ollie on 19/04/2017.
 */

public class EnemyObject extends GameObject{

    public EnemyObject(float x, float y, Bitmap mBitmap, float xSpeed, float ySpeed){
        this.x = x;
        this.y = y;
        this.image = mBitmap;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;

    }
}

