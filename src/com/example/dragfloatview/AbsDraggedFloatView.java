package com.example.dragfloatview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * @author daiwj
 * 
 * @param <T>
 */
public abstract class AbsDraggedFloatView<T> extends ImageView implements Runnable, OnClickListener {
	
	public final String TAG = getClass().getSimpleName();
	
	private WindowManager wm;
	private WindowManager.LayoutParams wmlp;
	
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	private int mTouchSlop = 0;
	private int mDefaultLocationY = 0;
	
	private int mClickX = 0;
	private int mClickY = 0;
	private int mBottomLimitHeight = 0;
	private int mAutoBackTimemillis = 0;
	private int mStepCount = 0;
	private int mStepDistanceX = 0;
	private int mStepDistanceY = 0;
	
	private boolean mIsMoved = false;
	private boolean mIsClick = false;
	
	private static final int STEP_DIRETION_LEFT = 1;
	private static final int STEP_DIRECTION_RIGHT = 2;
	private int stepDirection = -1;

	private DisplayMetrics dm;
	
	public void setBottomLimitHeight(int bottomLimitHeight) {
		this.mBottomLimitHeight = bottomLimitHeight;
	}
	
	public void setAutoBackTimemillis(int autoBackTimemillis) {
		this.mAutoBackTimemillis = autoBackTimemillis;
	}
	
	public AbsDraggedFloatView(Context activity) {
		this(activity, null);
	}
	
	public AbsDraggedFloatView(Context activity, AttributeSet attrs) {
		super(activity, attrs);
		dm = getResources().getDisplayMetrics();
		wm = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE));
		wmlp = new WindowManager.LayoutParams();
		mTouchSlop = (int) (getResources().getDisplayMetrics().density * 5);
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels;
		mDefaultLocationY = mScreenHeight / 2 - 140;
		mAutoBackTimemillis = 100;
		mBottomLimitHeight = (int) (50 * getResources().getDisplayMetrics().density);
		setOnClickListener(this);
		setBottomLimitHeight(200);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(dpToPx(72), dpToPx(72));
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int motionX = (int) event.getRawX();
		final int motionY = (int) event.getRawY();
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mIsClick = true;
			mClickX = motionX; 
			mClickY = motionY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsClick) {
				if (!mIsMoved) {
					if (Math.abs(motionX - mClickX) >= mTouchSlop || Math.abs(motionY - mClickY) >= mTouchSlop) {
						mIsMoved = true;
					}
				} else {
					wmlp.x = motionX - getWidth() / 2;
					wmlp.y = motionY - getHeight();
					wm.updateViewLayout(this, wmlp);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mIsClick && !mIsMoved) {
				mIsClick = false;
				performClick();
			} else {
				keepSideIfNeed(motionX, motionY);
				mIsClick = mIsMoved = false;
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean performClick() {
		return super.performClick();
	}
	
	private void keepSideIfNeed(int currentLocationX, int currentLocationY) {
		if (currentLocationX != 0 || currentLocationX != mScreenWidth) {
			mStepCount = (int) Math.ceil(mAutoBackTimemillis / 10 + 0.5f);
			stepDirection = currentLocationX < mScreenWidth / 2 ? STEP_DIRETION_LEFT : STEP_DIRECTION_RIGHT;
			int movedX = stepDirection == STEP_DIRETION_LEFT ? currentLocationX : (mScreenWidth - currentLocationX);
			mStepDistanceX = movedX / mStepCount;
			if (mScreenHeight - (currentLocationY + getHeight()) < mBottomLimitHeight) {
				mStepDistanceY = (currentLocationY - mDefaultLocationY) / mStepCount;
			} else {
				mStepDistanceY = 0;
			}
			postDelayed(this, mAutoBackTimemillis / 10);
		}
	}
	
	@Override
	public void run() {
		if (mStepCount > 0) {
			if (stepDirection == STEP_DIRETION_LEFT) { // 往左边靠边
				wmlp.x -= mStepDistanceX;
				wmlp.y -= mStepDistanceY;
			} else {
				wmlp.x += mStepDistanceX;
				wmlp.y -= mStepDistanceY;
			}
			wm.updateViewLayout(this, wmlp);
			mStepCount--;
			postDelayed(this, mAutoBackTimemillis / 10);
		} 
	}
	
	@SuppressLint("RtlHardcoded")
	public AbsDraggedFloatView<T> create(boolean show) {
		wmlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmlp.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
		wmlp.format = PixelFormat.RGBA_8888;
		wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmlp.gravity = Gravity.LEFT | Gravity.TOP;
		wmlp.x = mScreenWidth;
		wmlp.y = mDefaultLocationY;
		wm.addView(this, wmlp);
		toggle(show);
		return this;
	}
	
	public void show() {
		setVisibility(VISIBLE);
	}
	
	public void hide() {
		setVisibility(INVISIBLE);
	}
	
	public void toggle(boolean toggle) {
		setVisibility(toggle ? VISIBLE : INVISIBLE);
	}
	
	public void destroy() {
		if (wm != null) {
			wm.removeView(this);
			wm = null;
			wmlp = null;
		}
	}

	@Override
	public void onClick(View v) {
		onFloatViewClick(this);
	}
	
	public void onFloatViewClick(View view) {
	}

	public abstract void applyData(T data);
	
	private int dpToPx(float dp) {
		return (int) (dm.density * dp + 0.5f);
	}
}
