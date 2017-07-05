package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;

/**
 * Created by Ollie on 19/04/2017.
 */

public class PointObject extends GameObject {

    public PointObject(float x, float y, Bitmap mBitmap){
        this.x = x;
        this.y = y;
        this.image = mBitmap;
    }
}
