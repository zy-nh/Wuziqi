package com.example.zhuye.wuziqi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WuziqiPanel extends View {

    private int mPanelWidth;//棋盘宽度
    private float mLineHeight;//行高度
    private int MAX_LINE=10;//最大行数
    private int MAX_COUNT_IN_LINE =5;

    private Paint mPaint = new Paint();

    private Bitmap mWhitePiece;//白棋
    private Bitmap mBlackPiece;//黑棋

    private float ratioPieceOfLineHeight = 3 * 1.0f /4;//棋子大小比率

    //白棋先手，或者当前轮到白棋
    private boolean mIsWhite = true;
    //白棋黑棋集合
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();

    private boolean mIsGameOver;//判断结束
    private boolean mIsWhiteWinner;//判断赢家是谁

    public WuziqiPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //setBackgroundColor(0x44ff0000);//设置背景颜色
        init();
    }

    private void init() {
        //初始化棋盘
        mPaint.setColor(0x88000000);//设置颜色 透明灰
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);//画线

        //初始化棋子
        mWhitePiece = BitmapFactory.decodeResource(getResources(),R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize,heightSize);

        if (widthMode==MeasureSpec.UNSPECIFIED){
            width = heightSize;
        } else if (heightMode==MeasureSpec.UNSPECIFIED){
            width=widthSize;
        }
        setMeasuredDimension(width,width);
    }

    //初始化
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth=w;
        mLineHeight=mPanelWidth *1.0f /MAX_LINE;

        /*棋子大小根据控件大小和比率而定*/
        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mIsGameOver) return false;//游戏结束时不允许落子

        int action = event.getAction();
        if (action== MotionEvent.ACTION_UP){
            int x = (int) event.getX();
            int y = (int) event.getY();

            Point p = getValidPoint(x,y);//封装为Point
            //判断所要下棋的地方是否有棋子
            if (mWhiteArray.contains(p)||mBlackArray.contains(p)){
                return false;
            }
            if (mIsWhite){
                mWhiteArray.add(p);
            }else{
                mBlackArray.add(p);
            }
            invalidate();
            mIsWhite = !mIsWhite;
        }
        return true;
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int)(x/mLineHeight),(int)(y/mLineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);//绘制棋盘
        drawPieces(canvas);//绘制棋子
        checkGameOver();
    }

    private void checkGameOver() {

        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);

        if (whiteWin || blackWin){
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;

            String text = mIsWhiteWinner ? "白棋胜利":"黑棋胜利";
            Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkFiveInLine(List<Point> points) {

        for (Point p:points){
            int x = p.x;
            int y = p.y;
            
            boolean win = checkHorizontal(x,y,points);
            if (win) return true;
            win = checkVertical(x,y,points);
            if (win) return true;
            win = checkLeftDiagonal(x,y,points);
            if (win) return true;
            win = checkRightDiagonal(x,y,points);
            if (win) return true;
        }
        return false;
    }

    /**
     * 判断x，y位置的棋子，是否横向有相邻5个一致
     * @param x
     * @param y
     * @param points
     * @return
     * */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        //判断左边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;
        //判断右边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }
    /**
     * 判断x，y位置的棋子，是否纵向有相邻5个一致
     * @param x
     * @param y
     * @param points
     * @return
     * */
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        //判断上边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x,y-i))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;
        //判断下边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x,y+i))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }
    /**
     * 判断x，y位置的棋子，是否左斜边有相邻5个一致
     * @param x
     * @param y
     * @param points
     * @return
     * */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        //判断左下边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y+i))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;
        //判断右上边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y-i))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }
    /**
     * 判断x，y位置的棋子，是否右斜边有相邻5个一致
     * @param x
     * @param y
     * @param points
     * @return
     * */
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        //判断左上边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y-i))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;
        //判断右下边
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y+i))){
                count++;
            }else {
                break;
            }
        }
        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }

    private void drawPieces(Canvas canvas) {
        //绘制白棋
        for (int i=0,n=mWhiteArray.size();i<n;i++){
            Point whitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x+(1-ratioPieceOfLineHeight)/2)*mLineHeight,
                    (whitePoint.y+(1-ratioPieceOfLineHeight)/2)*mLineHeight,null);
        }
        //绘制黑棋
        for (int i=0,n=mBlackArray.size();i<n;i++){
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x+(1-ratioPieceOfLineHeight)/2)*mLineHeight,
                    (blackPoint.y+(1-ratioPieceOfLineHeight)/2)*mLineHeight,null);
        }
    }

    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;//防止精度损失

        for (int i=0;i<MAX_LINE;i++){
            int startX = (int) (lineHeight /2);
            int endX = (int) (w - lineHeight /2);
            int y = (int) ((0.5 + i)*lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);//画横线
            canvas.drawLine(y, startX, y,endX,mPaint);//画竖线
        }
    }

    public void start(){

        mWhiteArray.clear();//清空数据
        mBlackArray.clear();
        mIsGameOver = false;
        mIsWhiteWinner = false;
        invalidate();//再调用一次
    }

    /**
     * 存储VIew，并恢复，防止被打断
     * */
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER,mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY,mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY,mBlackArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
