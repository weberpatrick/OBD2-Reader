package obd2.dhbw.de.obd2_reader.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import obd2.dhbw.de.obd2_reader.R;

/**
 * Created by Ricar on 26.04.2016.
 */
public class TachometerView
       extends View
{
//  TODO http://mindtherobot.com/blog/272/android-custom-ui-making-a-vintage-thermometer/

    private final String LOG_TAG = TachometerView.class.getName();

    private Bitmap background; // holds the cached static part
    private Paint backgroundPaint;

    // drawing tools
    private RectF rimRect;
    private Paint rimPaint;
    private Paint rimCirclePaint;

    private RectF faceRect;
    private Bitmap faceTexture;
    private Paint facePaint;
    private Paint rimShadowPaint;

    private Paint scalePaint;
    private RectF scaleRect;

    private Paint titlePaint;
    private Path titlePath;

    // scale configuration
    private static final int totalNicks = 100;
    private static final float degreesPerNick = 360.0f / totalNicks;
    private static final int centerDegree = 40; // the one in the top center (12 o'clock)
    private static final int minDegrees = -30;
    private static final int maxDegrees = 110;

//	***************************************************************************
//	CONSTRUCTOR AREA
//	***************************************************************************

    public TachometerView(Context context)
    {
        super(context);
        init();
    }

    public TachometerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public TachometerView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    private void drawBackground(Canvas canvas)
    {
        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);

        if (background == null) {
            Log.w(LOG_TAG, "Background not created");
        } else {
            canvas.drawBitmap(background, 0, 0, backgroundPaint);
        }
    }

    private void drawRim(Canvas canvas)
    {
        // first, draw the metallic body
        canvas.drawOval(rimRect, rimPaint);
        // now the outer rim circle
        canvas.drawOval(rimRect, rimCirclePaint);
    }

    private void drawFace(Canvas canvas)
    {
        canvas.drawOval(faceRect, facePaint);
        // draw the inner rim circle
        canvas.drawOval(faceRect, rimCirclePaint);
        // draw the rim shadow inside the face
        canvas.drawOval(faceRect, rimShadowPaint);
    }

    private void drawScale(Canvas canvas) {
        canvas.drawOval(scaleRect, scalePaint);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        for (int i = 0; i < totalNicks; ++i) {
            float y1 = scaleRect.top;
            float y2 = y1 - 0.020f;

            canvas.drawLine(0.5f, y1, 0.5f, y2, scalePaint);

            if (i % 5 == 0) {
                int value = nickToDegree(i);

                if (value >= minDegrees && value <= maxDegrees)
                {
                    String valueString = String.valueOf(value);
                    Log.d(LOG_TAG, "value: " + valueString);
                    canvas.drawText(valueString, 0.5f, y2 - 0.015f, scalePaint);
                }
            }

            canvas.rotate(degreesPerNick, 0.5f, 0.5f);
        }
        canvas.restore();
    }

    private int nickToDegree(int nick)
    {
        int rawDegree = ((nick < totalNicks / 2) ? nick : (nick - totalNicks)) * 2;
        int shiftedDegree = rawDegree + centerDegree;
        return shiftedDegree;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        drawBackground(canvas);

        float scale = (float) getWidth();
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(scale, scale);

//        drawLogo(canvas);
//        drawHand(canvas);

        canvas.restore();

//        if (handNeedsToMove()) moveHand();
    }

    private void regenerateBackground()
    {
        // free the old bitmap
        if (background != null) {
            background.recycle();
        }

        background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(background);
        float scale = (float) getWidth();
        backgroundCanvas.scale(scale, scale);

        drawRim(backgroundCanvas);
        drawFace(backgroundCanvas);
        drawScale(backgroundCanvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(LOG_TAG, "Size changed to " + w + "x" + h);

        regenerateBackground();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
//        Log.d(LOG_TAG, "Width spec: "  + MeasureSpec.toString(widthMeasureSpec));
//        Log.d(LOG_TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        int chosenDimension = Math.min(chosenWidth, chosenHeight);

        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int mode, int size)
    {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else { // (mode == MeasureSpec.UNSPECIFIED)
            return 300;
        }
    }

    private void init()
    {
        rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);

        // the linear gradient is a bit skewed for realism
        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setShader(new LinearGradient(0.40f, 0.0f, 0.60f, 1.0f,
                Color.rgb(0xf0, 0xf5, 0xf0),
                Color.rgb(0x30, 0x31, 0x30),
                Shader.TileMode.CLAMP));

        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
        rimCirclePaint.setStrokeWidth(0.005f);

        float rimSize = 0.02f;
        faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        faceTexture = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.plastic);
        BitmapShader paperShader = new BitmapShader(faceTexture,
                Shader.TileMode.MIRROR,
                Shader.TileMode.MIRROR);
        Matrix paperMatrix = new Matrix();
        facePaint = new Paint();
        facePaint.setFilterBitmap(true);
        paperMatrix.setScale(1.0f / faceTexture.getWidth(),
                1.0f / faceTexture.getHeight());
        paperShader.setLocalMatrix(paperMatrix);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setShader(paperShader);

        rimShadowPaint = new Paint();
        rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, faceRect.width() / 2.0f,
                new int[] { 0x00000000, 0x00000500, 0x50000500 },
                new float[] { 0.96f, 0.96f, 0.99f },
                Shader.TileMode.MIRROR));
        rimShadowPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setColor(0x9f004d0f);
        scalePaint.setStrokeWidth(0.005f);
        scalePaint.setAntiAlias(true);

        scalePaint.setTextSize(0.045f);
        scalePaint.setTypeface(Typeface.SANS_SERIF);
        scalePaint.setTextScaleX(0.8f);
        scalePaint.setTextAlign(Paint.Align.CENTER);

        float scalePosition = 0.10f;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
                faceRect.right - scalePosition, faceRect.bottom - scalePosition);

        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
    }
}
