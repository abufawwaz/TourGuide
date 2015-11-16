package tourguide.tourguide;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.view.MotionEventCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class FrameLayoutWithHole extends FrameLayout {
    private TextPaint mTextPaint;
    private Context mContext;
    private TourGuide.MotionType mMotionType;
    private Paint mEraser;

    Bitmap mEraserBitmap;
    private Canvas mEraserCanvas;
//    private Paint mPaint;
//    private Paint transparentPaint;
    private View mViewHole; // This is the targeted view to be highlighted, where the hole should be placed
    private int mRadius;
    private int [] mPos;
    private float mDensity;
    private Overlay mOverlay;

    private ArrayList<AnimatorSet> mAnimatorSetArrayList;

    public void setViewHole(View viewHole) {
        this.mViewHole = viewHole;
        enforceMotionType();
    }

    public void addAnimatorSet(AnimatorSet animatorSet){
        if (mAnimatorSetArrayList==null){
            mAnimatorSetArrayList = new ArrayList<AnimatorSet>();
        }
        mAnimatorSetArrayList.add(animatorSet);
    }

    private void enforceMotionType(){
        if(TourGuide.DEBUG)
            Log.d(TourGuide.TAG, "enforceMotionType 1");
        
        if (mViewHole!=null) {Log.d(TourGuide.TAG,"enforceMotionType 2");
            if (mMotionType!=null && mMotionType == TourGuide.MotionType.ClickOnly) {
                if(TourGuide.DEBUG) {
                    Log.d(TourGuide.TAG, "enforceMotionType 3");
                    Log.d(TourGuide.TAG, "only Clicking");
                }
                mViewHole.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mViewHole.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
            } else if (mMotionType!=null && mMotionType == TourGuide.MotionType.SwipeOnly) {
                if(TourGuide.DEBUG) {
                    Log.d(TourGuide.TAG, "enforceMotionType 4");
                    Log.d(TourGuide.TAG, "only Swiping");
                }
                mViewHole.setClickable(false);
            }
        }
    }

    public FrameLayoutWithHole(Context context, View view) {
        this(context, view, TourGuide.MotionType.AllowAll);
    }

    public FrameLayoutWithHole(Context context, View view, TourGuide.MotionType motionType) {
        this(context, view, motionType, new Overlay());
    }

    public FrameLayoutWithHole(Context context, View view, TourGuide.MotionType motionType, Overlay overlay) {
        super(context);
        mContext = context;
        mViewHole = view;
        init(null, 0);
        enforceMotionType();
        mOverlay = overlay;

        int [] pos = new int[2];
        mViewHole.getLocationOnScreen(pos);
        mPos = pos;

        mDensity = context.getResources().getDisplayMetrics().density;
        int padding = (int)(20 * mDensity);

        if (mViewHole.getHeight() > mViewHole.getWidth()) {
            mRadius = mViewHole.getHeight()/2 + padding;
        } else {
            mRadius = mViewHole.getWidth()/2 + padding;
        }
        mMotionType = motionType;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
//        final TypedArray a = getContext().obtainStyledAttributes(
//                attrs, FrameLayoutWithHole, defStyle, 0);
//
//
//        a.recycle();
        setWillNotDraw(false);
        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

//        Point size = new Point();
//        size.x = mContext.getResources().getDisplayMetrics().widthPixels;
//        size.y = mContext.getResources().getDisplayMetrics().heightPixels;

//        mEraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
//        mEraserCanvas = new Canvas(mEraserBitmap);

//        mPaint = new Paint();
//        mPaint.setColor(0xcc000000);
//        transparentPaint = new Paint();
//        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
//        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mEraser = new Paint();
        mEraser.setColor(0xFFFFFFFF);
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    private boolean mCleanUpLock = false;

    protected void cleanUp(){
        if (getParent() != null) {
            if (mOverlay!=null && mOverlay.mExitAnimation!=null) {
                performOverlayExitAnimation();
            } else {
                ((ViewGroup) this.getParent()).removeView(this);
            }
        }
    }

    private void performOverlayExitAnimation(){
        if (!mCleanUpLock) {
            final FrameLayout _pointerToFrameLayout = this;
            mCleanUpLock = true;
            
            if(TourGuide.DEBUG)
                Log.d(TourGuide.TAG,"Overlay exit animation listener is overwritten...");
            
            mOverlay.mExitAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    ((ViewGroup) _pointerToFrameLayout.getParent()).removeView(_pointerToFrameLayout);
                }
            });
            this.startAnimation(mOverlay.mExitAnimation);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(mEraserBitmap != null){
            mEraserBitmap.recycle();
            mEraserBitmap = null;
        }

        mEraserBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mEraserCanvas = new Canvas(mEraserBitmap);
    }

    /* comment this whole method to cause a memory leak */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /* cleanup reference to prevent memory leak */

        if(mEraserCanvas != null)
            mEraserCanvas.setBitmap(null);

        if(mEraserBitmap != null) {
            mEraserBitmap.recycle();
            mEraserBitmap = null;
        }

        if (mAnimatorSetArrayList != null && mAnimatorSetArrayList.size() > 0){
            for(int i=0;i<mAnimatorSetArrayList.size();i++){
                mAnimatorSetArrayList.get(i).end();
                mAnimatorSetArrayList.get(i).removeAllListeners();
            }
        }
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
                "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_" ).append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid " ).append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")" );
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#" ).append(i);
            sb.append("(pid " ).append(event.getPointerId(i));
            sb.append(")=" ).append((int) event.getX(i));
            sb.append("," ).append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";" );
        }
        sb.append("]");

        if(TourGuide.DEBUG)
            Log.d(TourGuide.TAG, sb.toString());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //first check if the location button should handle the touch event
        dumpEvent(ev);
        int action = MotionEventCompat.getActionMasked(ev);
        if(mViewHole != null) {
            int[] pos = new int[2];
            mViewHole.getLocationOnScreen(pos);
            if(TourGuide.DEBUG) {
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] mViewHole.getHeight(): " + mViewHole.getHeight());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] mViewHole.getWidth(): " + mViewHole.getWidth());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Touch X(): " + ev.getRawX());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Touch Y(): " + ev.getRawY());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] X lower bound: " + pos[0]);
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] X higher bound: " + (pos[0] + mViewHole.getWidth()));
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Y lower bound: " + pos[1]);
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Y higher bound: " + (pos[1] + mViewHole.getHeight()));
            }

            if(ev.getRawY() >= pos[1] && ev.getRawY() <= (pos[1] + mViewHole.getHeight()) && ev.getRawX() >= pos[0] && ev.getRawX() <= (pos[0] + mViewHole.getWidth())) { //location button event
                
                if(TourGuide.DEBUG) {
                    Log.d(TourGuide.TAG, "to the BOTTOM!");
                    Log.d(TourGuide.TAG, "" + ev.getAction());
                }

                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mEraserBitmap.eraseColor(Color.TRANSPARENT);

        if (mOverlay!=null) {
            mEraserCanvas.drawColor(mOverlay.mBackgroundColor);
            int padding = (int) (10 * mDensity);
            if (mOverlay.mStyle == Overlay.Style.Rectangle) {
                mEraserCanvas.drawRect(mPos[0] - padding, mPos[1] - padding, mPos[0] + mViewHole.getWidth() + padding, mPos[1] + mViewHole.getHeight() + padding, mEraser);
            } else {
                mEraserCanvas.drawCircle(mPos[0] + mViewHole.getWidth() / 2, mPos[1] + mViewHole.getHeight() / 2, mRadius, mEraser);
            }
        }
        canvas.drawBitmap(mEraserBitmap, 0, 0, null);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mOverlay!=null && mOverlay.mEnterAnimation!=null) {
            this.startAnimation(mOverlay.mEnterAnimation);
        }
    }

}
