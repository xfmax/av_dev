surfaceView是Android提供的一种更加适用于频繁更新ui，通过子线程来更新ui，保证主线程不会因为频繁更新ui而产生阻塞（卡顿）。

看到这里，大家是不是有点小疑问了，不是说不能在子线程更新ui吗？
哈哈，如果有这样想的小伙伴，建议去看看google的文档，你会发现自己看了好多别人的博客，而别人的博客书写上措辞上多少有点不严谨，官方的说法是不建议，不是不能，所以我在这里强烈建议大家多多去官网看文档，少看点别人的博客，对大家只有好处，没有坏处。


[注]：有兴趣，你可以尝试在Activity的onCreate方法里new一个Thread并start，在run方法中更新ui试试，你看看系统会不会报异常？有兴趣可以深入研究，在这我就不做过多的解释了。

接下来进入主题，上代码：

```java
package com.example.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class SurfaceViewImpl extends SurfaceView implements SurfaceHolder.Callback2, Runnable {
    private Canvas mCanvas;
    private boolean isDrawing = false;
    private SurfaceHolder mHolder;
    private Context mContext;
    private Path mPath;
    private Paint mPaint;

    public SurfaceViewImpl(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        //获取到Holder并添加回调
        mHolder = getHolder();
        mHolder.addCallback(this);
        //跟绘制有关的，例如画笔的设置，绘制的路径
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        mPaint.setAntiAlias(true);
        mPath = new Path();
        //设置获取到焦点，并且在touch模式下可以获取到焦点
        setFocusable(false);
        setFocusableInTouchMode(false);
        setKeepScreenOn(true);
    }

    public SurfaceViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SurfaceViewImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
    }

    @Override
    public void run() {
        long start = SystemClock.currentThreadTimeMillis();
        while (isDrawing) {
            draw();
            long end = SystemClock.currentThreadTimeMillis();
            if (100 > end - start) {
                try {
                    Thread.sleep(100 - end + start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void reset() {
        mPath.reset();
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);
            mCanvas.drawPath(mPath, mPaint);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }
}

```


