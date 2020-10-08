package com.example.mdp;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class GameView extends View {

    //Constant values -> Direction
    public static final int LEFT = 180;
    public static final int UP = 90;
    public static final int RIGHT = 0;
    public static final int DOWN = 270;
    public static final int ROTATELEFT = -1;
    public static final int ROTATERIGHT = 1;
    public static final int WAYPOINT = 20;

    //Constant values -> Drawing
    public static final int FREE = 0;
    public static final int EXPLORED = 17;
    public static final int OBSTACLE = 16;
    public static final int ROBOT = 19;
    public static final int FINISH = 18;

    //Map
    int col, row;
    int[][] mapState = new int[col][row];
    int width = 0;
    float cellSize = 0;
    String mapDescriptor = "";
    String mapDescriptor2 = "";
    Rect temp;

    //Robot
    Robot robot;
    Bitmap robotBitmap;
    Bitmap sign1Bitmap;
    Bitmap sign2Bitmap;
    Bitmap sign3Bitmap;
    Bitmap sign4Bitmap;
    Bitmap sign5Bitmap;
    Bitmap sign6Bitmap;
    Bitmap sign7Bitmap;
    Bitmap sign8Bitmap;
    Bitmap sign9Bitmap;
    Bitmap sign10Bitmap;
    Bitmap sign11Bitmap;
    Bitmap sign12Bitmap;
    Bitmap sign13Bitmap;
    Bitmap sign14Bitmap;
    Bitmap sign15Bitmap;
    Bitmap waypoint;

    //Destination
    Destination finish;

    //background
    Drawable bg = getResources().getDrawable(R.mipmap.background, null);

    //Paints
    Paint cellBorderPaint;
    Paint cellPaint;
    Paint obstaclePaint;
    Paint robotPaint;
    Paint textPaint;
    Paint finishPaint;
    Paint exploredPaint;

    private void initPaint () {
        cellBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellBorderPaint.setStyle(Paint.Style.STROKE);
        cellBorderPaint.setColor(ColorPallete.Magnolia);
        cellBorderPaint.setStrokeWidth(1);

        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setColor(ColorPallete.HanPurple);

        obstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        obstaclePaint.setStyle(Paint.Style.FILL);
        obstaclePaint.setColor(ColorPallete.Xiketic);

        exploredPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        exploredPaint.setStyle(Paint.Style.FILL);
        exploredPaint.setColor(ColorPallete.DarkOrchid);

        robotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        robotPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(0);
        textPaint.setColor(Color.rgb(255,255,255));

        finishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        finishPaint.setColor(ColorPallete.RussianViolet);
    }

    //map preparation
    //set robot position to map
    public void mapToMap (int x, int y, int what) {
        //123
        //456
        //789
        //1
        mapState[y-2][x] = what;
        //2
        mapState[y-2][x+1] = what;
        //3
        mapState[y-2][x+2] = what;
        //4
        mapState[y-1][x] = what;
        //5
        mapState[y-1][x+1] = what;
        //6
        mapState[y-1][x+2] = what;
        //7
        mapState[y][x] = what;
        //8
        mapState[y][x+1] = what;
        //9
        mapState[y][x+2] = what;
    }

    public void addObstacle (int x, int y, int id) { //16 for normal obstacle
        try {
            mapState[y][x] = id;
        } catch (Exception e) {

        }
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public void addExplored (int x, int y, char _) {
        try {
            if (_ == '0') {
                mapState[y][x] = FREE;
            } else if (mapState[y][x] == FINISH) {

            } else {
                mapState[y][x] = EXPLORED;
            }
        } catch (Exception e) {

        }
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    private void initMap () {
        for (int j = 0; j<row; j++) {
            for (int i = 0; i<col; i++) {
                if (mapState[j][i] < 1 || mapState[j][i] > 17) {
                    mapState[j][i] = FREE;
                }
            }
        }
        mapToMap(finish.x, finish.y, FINISH);
        mapToMap(robot.x, robot.y, ROBOT);
    }

    public void resetMap () {
        for (int j = 0; j<row; j++) {
            for (int i = 0; i<col; i++) {
                mapState[j][i] = FREE;
            }
        }
        mapToMap(finish.x, finish.y, FINISH);
        mapToMap(robot.x, robot.y, ROBOT);
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    private void initBitmap () {
        robotBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.robotonup);
        sign1Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.up_arrow_1);
        sign2Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.down_arrow_2);
        sign3Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.right_arrow_3);
        sign4Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.left_arrow_4);
        sign5Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.go_5);
        sign6Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.six_6);
        sign7Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.seven_7);
        sign8Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.eight_8);
        sign9Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.nine_9);
        sign10Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.zero_10);
        sign11Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.v_11);
        sign12Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.w_12);
        sign13Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.x_13);
        sign14Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.y_14);
        sign15Bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.z_15);
        waypoint = BitmapFactory.decodeResource(getResources(), R.mipmap.waypoint);
    }

    private void initRobot () {
        robot = new Robot(false, UP, false);
    }

    private void initDestination () {
        finish = new Destination();
    }

    private void init () {
        initBitmap();
        initRobot();
        initDestination();
        initPaint();
        initMap();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GameView, 0,0);
        try {
            row = attributes.getInteger(R.styleable.GameView_row_count, 0);
            col = attributes.getInteger(R.styleable.GameView_column_count, 0);
            mapState = new int[row][col];
        } finally {
            attributes.recycle();
        }
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        robot.isDrawn = false;

        //If View is not ready
        if (width == 0) return;

        //Draw the canvas
        drawCanvas(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w == oldw && h == oldh) return;
        ViewGroup.LayoutParams params = this.getLayoutParams();

        Float a = ( ((float) row/col) * w);

        params.height = (int)(((float) row/col) * w);
        params.width = w;
        this.setLayoutParams(params);
        width = w;
        cellSize = w/col;
        textPaint.setTextSize(cellSize == 0 ? 20 : cellSize);
        scaleBitmap(cellSize);
    }

    public void scaleBitmap (float cellSize) {
        robotBitmap = Bitmap.createScaledBitmap(robotBitmap, (int) cellSize*3, (int) cellSize*3, false);
        sign1Bitmap = Bitmap.createScaledBitmap(sign1Bitmap, (int) cellSize, (int) cellSize, false);
        sign2Bitmap = Bitmap.createScaledBitmap(sign2Bitmap, (int) cellSize, (int) cellSize, false);
        sign3Bitmap = Bitmap.createScaledBitmap(sign3Bitmap, (int) cellSize, (int) cellSize, false);
        sign4Bitmap = Bitmap.createScaledBitmap(sign4Bitmap, (int) cellSize, (int) cellSize, false);
        sign5Bitmap = Bitmap.createScaledBitmap(sign5Bitmap, (int) cellSize, (int) cellSize, false);
        sign6Bitmap = Bitmap.createScaledBitmap(sign6Bitmap, (int) cellSize, (int) cellSize, false);
        sign7Bitmap = Bitmap.createScaledBitmap(sign7Bitmap, (int) cellSize, (int) cellSize, false);
        sign8Bitmap = Bitmap.createScaledBitmap(sign8Bitmap, (int) cellSize, (int) cellSize, false);
        sign9Bitmap = Bitmap.createScaledBitmap(sign9Bitmap, (int) cellSize, (int) cellSize, false);
        sign10Bitmap = Bitmap.createScaledBitmap(sign10Bitmap, (int) cellSize, (int) cellSize, false);
        sign11Bitmap = Bitmap.createScaledBitmap(sign11Bitmap, (int) cellSize, (int) cellSize, false);
        sign12Bitmap = Bitmap.createScaledBitmap(sign12Bitmap, (int) cellSize, (int) cellSize, false);
        sign13Bitmap = Bitmap.createScaledBitmap(sign13Bitmap, (int) cellSize, (int) cellSize, false);
        sign14Bitmap = Bitmap.createScaledBitmap(sign14Bitmap, (int) cellSize, (int) cellSize, false);
        sign15Bitmap = Bitmap.createScaledBitmap(sign15Bitmap, (int) cellSize, (int) cellSize, false);
        waypoint = Bitmap.createScaledBitmap(waypoint, (int) cellSize, (int) cellSize, false);
    }

    private void drawCanvas (Canvas canvas) {
        Rect bgRect = new Rect(0, 0, (int) cellSize*15, (int) cellSize*20);
        bg.setBounds(bgRect);
        bg.draw(canvas);
        for (int j = 0; j<row; j++) {
            for (int i = 0; i<col; i++) {
                temp = new Rect((int) cellSize*i,(int)cellSize*j, (int)cellSize*(i+1), (int)cellSize*(j+1));
                switch (mapState[j][i]) {
                    case 1:
                        drawSigns(temp, canvas, sign1Bitmap);
                        break;
                    case 2:
                        drawSigns(temp, canvas, sign2Bitmap);
                        break;
                    case 3:
                        drawSigns(temp, canvas, sign3Bitmap);
                        break;
                    case 4:
                        drawSigns(temp, canvas, sign4Bitmap);
                        break;
                    case 5:
                        drawSigns(temp, canvas, sign5Bitmap);
                        break;
                    case 6:
                        drawSigns(temp, canvas, sign6Bitmap);
                        break;
                    case 7:
                        drawSigns(temp, canvas, sign7Bitmap);
                        break;
                    case 8:
                        drawSigns(temp, canvas, sign8Bitmap);
                        break;
                    case 9:
                        drawSigns(temp, canvas, sign9Bitmap);
                        break;
                    case 10:
                        drawSigns(temp, canvas, sign10Bitmap);
                        break;
                    case 11:
                        drawSigns(temp, canvas, sign11Bitmap);
                        break;
                    case 12:
                        drawSigns(temp, canvas, sign12Bitmap);
                        break;
                    case 13:
                        drawSigns(temp, canvas, sign13Bitmap);
                        break;
                    case 14:
                        drawSigns(temp, canvas, sign14Bitmap);
                        break;
                    case 15:
                        drawSigns(temp, canvas, sign15Bitmap);
                        break;
                    case FREE:
                        canvas.drawRect(temp, cellBorderPaint);
                        break;
                    case EXPLORED:
                        canvas.drawRect(temp, cellBorderPaint);
                        canvas.drawRect(temp, exploredPaint);
                        break;
                    case OBSTACLE:
                        canvas.drawRect(temp, cellBorderPaint);
                        canvas.drawRect(temp, obstaclePaint);
                        break;
                    case ROBOT:
                        if (!robot.isDrawn) {
                            robot.pos = new Rect((int) cellSize*i,(int) cellSize*j, (int) (3*cellSize+cellSize*i), (int) (3*cellSize+cellSize*j));
                            robot.isDrawn = true;
                        }
                        canvas.drawRect(temp, cellBorderPaint);
                        break;
                    case FINISH:
                        canvas.drawRect(temp, finishPaint);
                        break;
                    case WAYPOINT:
                        drawSigns(temp, canvas, waypoint);
                        break;
                    default:
                        break;
                }
            }
        }
        canvas.drawText("Finish", (int) finish.x*cellSize+10, (int) finish.y*cellSize - 10, textPaint);
        drawRobot(robot.pos, canvas);
    }

    int rotationDegree;

    private void drawSigns (Rect pos, Canvas canvas, Bitmap b) {
        Matrix matrix = new Matrix();
        matrix.setTranslate(pos.left, pos.top);
        canvas.drawBitmap(b, matrix, null);
    }

    private void drawRobot (Rect pos, Canvas canvas) {
        Matrix matrix = new Matrix();
        switch (robot.dir) {
            case UP:
                rotationDegree = 0;
                break;
            case LEFT:
                rotationDegree = -90;
                break;
            case RIGHT:
                rotationDegree = 90;
                break;
            case DOWN:
                rotationDegree = 180;
                break;
        }
        matrix.setRotate(rotationDegree, (float) (pos.bottom-pos.top)/2, (float) (pos.right-pos.left)/2);
        matrix.postTranslate(pos.left, pos.top);
        canvas.drawBitmap(robotBitmap, matrix, null);
        robot.isDrawn = true;
    }

    public void moveForward (boolean updateMap) {
        moveRobot(robot.dir, updateMap);
    }

    //to move robot
    public void moveRobot (int direction, boolean updateMap) {

//        public static final int LEFT = 180;
//        public static final int UP = 90;
//        public static final int RIGHT = 0;
//        public static final int DOWN = 270;

        int newdir;
        if (direction != 1 && direction != -1) {
            int rotation = robot.dir - direction;
            if (rotation > 90 || rotation < -90) return;
            String ins = "";
            if (rotation == 90) {
                direction = ROTATERIGHT;
//                ins = "{move:[{direction:right}]}";
            }
            else if (rotation == -90 || rotation == 270) {
                direction = ROTATELEFT;
//                ins = "{move:[{direction:left}]}";
            }
            else if (rotation == 0) {
//                ins = "{move:[{direction:forward}]}";
            }
        }
//        BluetoothCommunication.writeMsg(ins.getBytes(Charset.defaultCharset()));
        switch (direction){
            case UP:
                canMove(robot.x, robot.y-1, ROBOT);
                robot.dir = UP;
                break;
            case RIGHT:
                canMove(robot.x + 1, robot.y, ROBOT);
                robot.dir = RIGHT;
                break;
            case DOWN:
                canMove(robot.x, robot.y + 1, ROBOT);
                robot.dir = DOWN;
                break;
            case LEFT:
                canMove(robot.x - 1, robot.y, ROBOT);
                robot.dir = LEFT;
                break;
            case ROTATELEFT:
                newdir = (robot.dir + 90) >= 360 ? (robot.dir + 90 - 360) : (robot.dir + 90);
                robot.dir = newdir;
                break;
            case ROTATERIGHT:
                newdir = (robot.dir - 90) < 0 ? (robot.dir - 90 + 360) : (robot.dir - 90);
                robot.dir = newdir;
                break;
            default:
                break;
        }
        initMap();
        if (updateMap) updateMapManual();
    }

    public void updateMapManual () {
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    private boolean isInVerticalBound (int newY) {
        return (newY >= 2 && newY < row) ? true : false;
    }

    private boolean isInHorizontalBound (int newX) {
        return (newX >= 0 && (newX + 2) < col) ? true : false;
    }

    private void canMove (int newX, int newY, int what) {
        //newX = X - 1
        //newY = Y + 1
        if (!isInVerticalBound(newY)) return;
        if (!isInHorizontalBound(newX)) return;

        for (int j = 0; j <= 2; j++) {
            for (int i = 0; i <= 2; i++) {
                if ((mapState[newY-j][newX+i] != FREE && mapState[newY-j][newX+i] != FINISH) && mapState[newY-j][newX+i] != what) {
                    return;
                }
            }
        }

        switch (what) {
            case ROBOT:
                robot.x = newX;
                robot.y = newY;
                break;
            case FINISH:
                finish.x = newX;
                finish.y = newY;
                break;
        }

        return;
    }

    //TOUCHING AREA

    public static final int TOUCH = 0;
    public static final int RELEASE = 1;
    public static final int HOLD = 2;

    boolean isCurrentlyDragging = false;
    int touched;
    boolean canTouch = false;
    int newx, newy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (canTouch) {
            Float x = event.getX();
            Float y = event.getY();
            //Touch coordinate
            int X = ((int) (x / cellSize));
            int Y = ((int) (y / cellSize));

            X = X < col ? X : col-1;
            Y = Y < row ? Y : row-1;

            if (!isCurrentlyDragging) {
                touched = mapState[Y][X];
                isCurrentlyDragging = true;
                if (touched != ROBOT && touched != FINISH) isCurrentlyDragging = false;
            }

            X = X-1;
            Y = Y+1;
            String newPos = "";
            if (isCurrentlyDragging && event.getAction() == HOLD) {
                switch (touched) {
                    case ROBOT:
                        canMove(X, Y, ROBOT);
                        break;
                    case FINISH:
                        canMove(X, Y, FINISH);
                        break;
                    default:
                        break;
                }
            } else isCurrentlyDragging = false;
            initMap();
            this.post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }

        return true;
    }

//    public void sendWaypoint () {
//        String newPos = ( row - (finish.y)) +","+(finish.x+1);
//        BluetoothCommunication.writeMsg(newPos.getBytes(Charset.defaultCharset()));
//    }

    public void toggleTouchScreen () {
        canTouch = !canTouch;
    }

    public void setMapDescriptor (String s) {
        mapDescriptor = s;
    }

    public void setMapDescriptor2 (String s) {
        mapDescriptor2 = s;
    }

    public String getMapDescriptor () {
        return mapDescriptor;
    }

    public String getMapDescriptor2 () {
        return mapDescriptor2;
    }
}

