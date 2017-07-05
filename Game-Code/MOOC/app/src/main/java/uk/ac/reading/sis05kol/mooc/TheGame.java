package uk.ac.reading.sis05kol.mooc;

//Other parts of the android libraries that we use
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class TheGame extends GameThread {

    //Player statistics
    public int iLives = 5;

    //Will store the images needed
    private Bitmap mBall;
    private Bitmap mEnemy;
    private Bitmap mPoints;
    private Bitmap mHearts;
    private Bitmap mHearts2;
    private Bitmap mPowerUpSpeed;
    private Bitmap mPowerUpKill;
    private Bitmap mBalloon;

    //The X and Y position of the ball on the screen (middle of ball)
    private float mBallX = 0;
    private float mBallY = 0;

    //The speed (pixel/second) of the ball in direction X and Y
    private float mBallSpeedX = 0;
    private float mBallSpeedY = 0;
    private float mGravity = -9;

    public Random Random;
    public int timer;

    //setup of array lists
    public ArrayList<GameObject> ObjectList = new ArrayList<GameObject>();
    public ArrayList<GameObject> BalloonList = new ArrayList<GameObject>();

    public void setiLives(){
        iLives = 5;
    }

    //This is run before anything else, so we can prepare things here
    public TheGame(GameView gameView) {
        //House keeping
        super(gameView);

        //Prepare the images so we can draw it on the screen (using a canvas)
        mBall = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.player);
        mEnemy = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.ufo);
        mPoints = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.points);
        mHearts = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.heart);
        mHearts2 = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.heart2);
        mPowerUpKill = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.powerupkill);
        mPowerUpSpeed = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.powerupspeed);
        mBalloon = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.balloon);

    }
    //This is run before a new game (also after an old game)
    @Override
    public void setupBeginning() {
        mMode = STATE_READY; //set state to ready
        iLives = 5; //initialise lives
        ObjectList.clear(); //clear arrays

        //Initialise speeds
        mBallSpeedX = 0;
        mBallSpeedY = 0;

        //Place the ball in the middle of the screen.
        //mBall.Width() and mBall.getHeigh() gives us the height and width of the image of the ball
        mBallX = mCanvasWidth / 2;
        mBallY = mCanvasHeight / 2;

        Random random = new Random();//initialise random number gen
        for (int k = 0; k < 5; k++) { //for 5 balloons
            int a = random.nextInt(mCanvasWidth); //get random x co ord
            int b = random.nextInt(mCanvasHeight); //get random y coord
            PointObject Balloon = new PointObject(a, b, mBalloon);//create new balloon object
            ObjectList.add(Balloon); //add balloon object to array
        }
    }

    @Override
    protected void doDraw(Canvas canvas) {
        if (canvas == null) return;
        super.doDraw(canvas);

        //draw objects in object array
        for (int i = 0; i < ObjectList.size(); i++) { //for all object in array
            GameObject mObject = ObjectList.get(i);
            if (mObject.y < mCanvasHeight - 160){ //if anything above the "ground"
            canvas.drawBitmap(mObject.image, mObject.x, mObject.y, null); //draw image
            }
            else{
                ObjectList.remove(mObject); //else remove object from array
                mObject = null; //set to null
            }
        }

        //draw remaining lives
        int topLeft = 0;
        for (int i = 0; i < iLives; i++){ //for each life
            canvas.drawBitmap(mHearts2, topLeft, 0, null);//draw golden heart next to each other in top left
            topLeft = topLeft + mHearts2.getWidth();
        }

        //draw player
        canvas.drawBitmap(mBall, mBallX - mBall.getWidth() / 2, mBallY - mBall.getHeight() / 2, null);
    }

    //This is run whenever the phone is touched by the user
    @Override
    protected void actionOnTouch(float x, float y) {
        //Increase/decrease the speed of the ball making the ball move towards the touch
        mBallSpeedX = x - mBallX;
        mBallSpeedY = y - mBallY;
    }

    //This is run just before the game "scenario" is printed on the screen
    @Override
    protected void updateGame(float secondsElapsed) {
        //create the list of random balloons obstacles

        //player can only have 5 lives, if more auto set to 5
        if(iLives >= 5)
            iLives = 5;

        //if 0 lives game over
        if (iLives <= 0){
            //Game over


            //upload score to Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            float f = getScore(); //get the score
            String f2 = Float.toString(f); //change float to string
            DatabaseReference myRef = database.getReference("Score");

            myRef.setValue(f2); //upload string


            //invoke lose state
            setState(STATE_LOSE);
        }
        //if 1500ms in
        if( secondsElapsed < 1500){
            randomObjectCreation(1); //multiplier is set to 1
        }
        else if(secondsElapsed < 4500){
            randomObjectCreation(2);//multiplaier set to 2
        }
        else{
            randomObjectCreation(8);//multiplier set to 8
        }

        enemyMovement(secondsElapsed);
        playerCollisionDetection();
        playerUpdate(secondsElapsed);
    }

    public void enemyMovement(float secondsElapsed){
        //////////ENEMY MOVEMENT//////////////////
        for ( int i = 0; i < ObjectList.size(); i++ ){
            //if moving enemy
            GameObject ArrayObject = ObjectList.get(i);
            if (ArrayObject.image == mEnemy){
                if (ArrayObject.x > mCanvasWidth) {
                    if (ArrayObject.xSpeed > 0)
                        ArrayObject.xSpeed = -ArrayObject.xSpeed;
                }
                else if (ArrayObject.x < -0.1) {
                    if (ArrayObject.x < 0) {
                        ArrayObject.x = -ArrayObject.x;
                        ArrayObject.xSpeed = -ArrayObject.xSpeed;
                    }
                }
                else if (ArrayObject.y > mCanvasHeight - 160) {
                    ObjectList.remove(ArrayObject);
                    ArrayObject = null;
                }
                else if (ArrayObject.y < -0.1) {
                    ArrayObject.ySpeed = -ArrayObject.ySpeed;
                }

                ArrayObject.x = ArrayObject.x + secondsElapsed * ArrayObject.xSpeed;
                ArrayObject.y = ArrayObject.y + secondsElapsed * ArrayObject.ySpeed;
            }
        }
    }

    public void randomObjectCreation(int multiplier){
        /////////////////random creation of friendly/enemy entity/////////////////////
        Random Random = new Random();
        int r = Random.nextInt(5000);
        //if life counter is below 3 then have slim chance of spawning in a 1up
        if(iLives <= 3) {
            if (r < 10 && r > 0) {
                //create a friendly HEART
                int a = Random.nextInt(mCanvasWidth);
                int b = Random.nextInt(mCanvasHeight);
                PointObject Hearts = new PointObject(a, b, mHearts);
                ObjectList.add(Hearts);
            }
        }
        if (r < 40 && r > 30){
            //create a speed powerup
            int a = Random.nextInt(mCanvasWidth);
            int b = Random.nextInt(mCanvasHeight);
            PointObject PowerUpSpeed = new PointObject(a, b, mPowerUpSpeed);
            ObjectList.add(PowerUpSpeed);
        }
        else if (r < 50 && r > 40){
            //create a kill powerup
            for (int i = 0; i < multiplier; i++) {
                int a = Random.nextInt(mCanvasWidth);
                int b = Random.nextInt(mCanvasHeight);
                PointObject PowerUpKill = new PointObject(a, b, mPowerUpKill);
                ObjectList.add(PowerUpKill);
            }
        }
        else if (r < 5000 && r > 4900){
            //create a friendly POINTS
                int a = Random.nextInt(mCanvasWidth);
                int b = Random.nextInt(mCanvasHeight);
                PointObject Points = new PointObject(a, b, mPoints);
                ObjectList.add(Points);

        }

        else if (r < 3000 && r > 2900) {
            //create a bad ENEMY
            for (int i = 0; i < multiplier; i++) {
                int a = Random.nextInt(mCanvasWidth);
                int b = Random.nextInt(mCanvasHeight / 2);
                Random Random2 = new Random();
                int s = 10 * (Random2.nextInt(20) - 10);
                EnemyObject Enemy = new EnemyObject(a, b, mEnemy, s, 100);
                ObjectList.add(Enemy);
            }
        }
        /////////////////////////////////////////////////////////////////////////////
    }

    public void playerCollisionDetection(){
        ////PLAYER collision detection////
        Rect PlayerHitBox = new Rect((int) mBallX, (int) mBallY, (int) mBallX + mBall.getWidth(), (int) mBallY + mBall.getHeight());
        for (int i = 0; i < ObjectList.size(); i++) {
            if (PlayerHitBox.intersect((int) ObjectList.get(i).x, (int) ObjectList.get(i).y, (int) ObjectList.get(i).x + ObjectList.get(i).image.getWidth(), (int) ObjectList.get(i).y + ObjectList.get(i).image.getHeight())) {

                GameObject temp = ObjectList.get(i);
                if (temp.image == mEnemy) {
                    iLives--;
                    ObjectList.remove(temp);
                    temp = null;
                    mBallSpeedX = mBallSpeedX / 2;
                    mBallSpeedY = mBallSpeedY / 2;
                    if (score > 0) {
                        updateScore(-5);
                    }
                }
                else if (temp.image == mPoints) {
                    ObjectList.remove(temp);
                    temp = null;
                    updateScore(10);
                }
                else if (temp.image == mHearts) {
                    iLives++;
                    ObjectList.remove(temp);
                    temp = null;
                }
                else if (temp.image == mPowerUpSpeed) {
                    ObjectList.remove(temp);
                    temp = null;
                    mBallSpeedX = mBallSpeedX * 5;
                    mBallSpeedY = mBallSpeedY * 5;
                }
                else if (temp.image == mPowerUpKill){
                    for (int j = 0; j < ObjectList.size(); j++){
                        GameObject temp2 = ObjectList.get(j);
                        if (temp2.image == mEnemy){
                            ObjectList.remove(temp2);
                            temp2 = null;
                            updateScore(5);
                        }
                    }
                    ObjectList.remove(temp);
                    temp = null;
                }
                //for balloon crashes reverse the direction of the bird
                else if (temp.image == mBalloon){
                    if (temp.x > mBallX) { //if ballonn to left of bird
                        mBallSpeedX = -mBallSpeedX; //reverse speed
                        mBallX = mBallX - 0.1f;//adds 0.1 pix to the bird so doesnt get stuck in balloon
                    }
                    if (temp.x < mBallX) { //if balloon to right of bird
                        mBallSpeedX = -mBallSpeedX;//reverse speed
                        mBallX = mBallX + 0.1f;//adds 0.1 pix to the bird so doesnt get stuck in balloon
                    }
                    if (temp.y > mBallY){
                        mBallSpeedY = -mBallSpeedY; //reverse speed
                        mBallY = mBallY - 0.1f;//adds 0.1 pix to the bird so doesnt get stuck in balloon
                    }
                    if (temp.y < mBallY) {
                        mBallSpeedY = -mBallSpeedY;//reverse speed
                        mBallY = mBallY + 0.1f; //adds 0.1 pix to the bird so doesnt get stuck in balloon
                    }
                }
            }
        }
    }

    public void playerUpdate(float secondsElapsed){
        //////Player updates//////
        //Move the ball's X and Y using the speed (pixel/sec)
        mBallSpeedY = mBallSpeedY - mGravity;
        mBallX = mBallX + secondsElapsed * mBallSpeedX;
        mBallY = mBallY + secondsElapsed * mBallSpeedY;
        ////////////////////////////////////////////

        ////////keeps the player confined within the canvas/////////////////////////
        if (mBallX > mCanvasWidth) { //if ball outside the right side on canvas
            mBallX = mCanvasWidth; //set to edge
            if (mBallSpeedX > 0)
                mBallSpeedX = -mBallSpeedX; //reverse speed
        }
        if (mBallX < -0.1) { //if ball off left side canvas
            mBallX = 0; //set to left side canvas
            if (mBallSpeedX < 0)
                mBallSpeedX = -mBallSpeedX;//reverse speed
        }
        if (mBallY > mCanvasHeight - 160) {
            mBallY = mCanvasHeight - 160; //stops at invis line which is background floor
        }
        if (mBallY < -0.1) { //if off the top
            mBallY = 0; //set to top
            mBallSpeedY = -mBallSpeedY;//reverse speed
        }
    }
}
