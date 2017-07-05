package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;

/**
 * Created by Ollie on 20/04/2017.
 */

public class HeartObject extends GameObject {

    public HeartObject(float x, float y, Bitmap mBitmap){
        this.x = x;
        this.y = y;
        this.image = mBitmap;
    }
}
