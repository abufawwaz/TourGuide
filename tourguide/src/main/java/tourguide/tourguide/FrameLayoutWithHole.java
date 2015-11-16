package tourguide.tourguide;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private TourGuide.MotionType mMotionType;
    private Overlay mOverlay;
    private View mViewHole; // This is the targeted view to be highlighted, where the hole should be placed

    private int [] mPos = new int[2];
    private boolean mCleanUpLock = false;
    private RectF mRect;

    private ArrayList<AnimatorSet> mAnimatorSetArrayList;

    private Paint mOverlayPaint;

    private static final float[] GRADIENT_STOPS = new float[]{0f, 0.99f, 1f};
    RadialGradient mOverlayShader;

    public void addAnimatorSet(AnimatorSet animatorSet){
        if (mAnimatorSetArrayList==null){
            mAnimatorSetArrayList = new ArrayList<>();
        }
        mAnimatorSetArrayList.add(animatorSet);
    }

    private void enforceMotionType(){
        if(TourGuide.DEBUG)
            Log.d(TourGuide.TAG, "enforceMotionType 1");
        
        if (mViewHole!=null) {
            if(TourGuide.DEBUG)
                Log.d(TourGuide.TAG,"enforceMotionType 2");

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
        mOverlay = overlay;
        mMotionType = motionType;
        init(null, 0);
        setViewHole(view);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setWillNotDraw(false);

        mRect = new RectF();

        mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOverlayPaint.setStyle(Paint.Style.FILL);
    }

    public void setViewHole(View viewHole) {
        mViewHole = viewHole;
        enforceMotionType();
        mViewHole.getLocationOnScreen(mPos);

        if(mOverlay != null) {
            float density = getContext().getResources().getDisplayMetrics().density;

            if (mOverlay.mStyle == Overlay.Style.Rectangle) {
                int padding = mOverlay.mPadding > 0 ? mOverlay.mPadding : (int) (10 * density);
                mRect.set(mPos[0] - padding, mPos[1] - padding, mPos[0] + mViewHole.getWidth() + padding, mPos[1] + mViewHole.getHeight() + padding);
                mOverlayPaint.setColor(mOverlay.mBackgroundColor);
            } else {
                int padding = mOverlay.mPadding > 0 ? mOverlay.mPadding : (int) (20 * density);
                float cX = mPos[0] + mViewHole.getWidth() / 2f;
                float cY = mPos[1] + mViewHole.getHeight() / 2f;
                float radius = Math.max(mViewHole.getWidth(), mViewHole.getHeight()) / 2 + padding;

                mOverlayPaint.setShader(mOverlayShader);
                mOverlayShader = new RadialGradient(cX, cY, radius,new int[]{0x00000000, 0x00000000, mOverlay.mBackgroundColor}, GRADIENT_STOPS, Shader.TileMode.CLAMP);
            }
        }
    }

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
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ((ViewGroup) _pointerToFrameLayout.getParent()).removeView(_pointerToFrameLayout);
                }
            });
            this.startAnimation(mOverlay.mExitAnimation);
        }
    }

    /* comment this whole method to cause a memory leak */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /* cleanup reference to prevent memory leak */
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
//        int action = MotionEventCompat.getActionMasked(ev);
        if(mViewHole != null) {
            mViewHole.getLocationOnScreen(mPos);
            if(TourGuide.DEBUG) {
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] mViewHole.getHeight(): " + mViewHole.getHeight());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] mViewHole.getWidth(): " + mViewHole.getWidth());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Touch X(): " + ev.getRawX());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Touch Y(): " + ev.getRawY());
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] X lower bound: " + mPos[0]);
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] X higher bound: " + (mPos[0] + mViewHole.getWidth()));
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Y lower bound: " + mPos[1]);
                Log.d(TourGuide.TAG, "[dispatchTouchEvent] Y higher bound: " + (mPos[1] + mViewHole.getHeight()));
            }

            if(ev.getRawY() >= mPos[1] && ev.getRawY() <= (mPos[1] + mViewHole.getHeight()) && ev.getRawX() >= mPos[0] && ev.getRawX() <= (mPos[0] + mViewHole.getWidth())) { //location button event
                
                if(TourGuide.DEBUG) {
                    Log.d(TourGuide.TAG, "to the BOTTOM!");
                    Log.d(TourGuide.TAG, "" + ev.getAction());
                }

                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void drawOverlayRect(Canvas canvas){
        int w = getWidth();
        int h = getHeight();

        float top = Math.max(mRect.top, 0);
        float bottom = Math.min(mRect.bottom, h);

        if(top > 0)
            canvas.drawRect(0, 0, w, top, mOverlayPaint);

        if(bottom < h)
            canvas.drawRect(0, bottom, w, h, mOverlayPaint);

        if(mRect.left > 0)
            canvas.drawRect(0, top, mRect.left, bottom, mOverlayPaint);

        if(mRect.right < w)
            canvas.drawRect(mRect.right, top, w, bottom, mOverlayPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mOverlay != null){
            if (mOverlay.mStyle == Overlay.Style.Rectangle)
                drawOverlayRect(canvas);
            else
                canvas.drawRect(0, 0, getWidth(), getHeight(), mOverlayPaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mOverlay != null && mOverlay.mEnterAnimation != null)
            this.startAnimation(mOverlay.mEnterAnimation);
    }

}
