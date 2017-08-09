package com.example.blade.aboutviews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by blade on 2017/8/7.
 * <p>
 * 关于view滑动的六种方式
 * layout()、offsetLeftAndRight()与offsetTopAndBottom()、
 * LayoutParams、动画、
 * scollTo与scollBy和Scroller；
 */

public class CustomView extends View implements GestureDetector.OnGestureListener {

    private int LastX;
    private int LastY;

//    GestureDetector detector = new GestureDetector(this);
//    ViewDragHelper.Callback mCallback=new ViewDragHelper.Callback() {
//        @Override
//        public boolean tryCaptureView(View child, int pointerId) {
//            return false;
//        }
//
//        @Override
//        public void onViewReleased(View releasedChild, float xvel, float yvel) {
//            super.onViewReleased(releasedChild, xvel, yvel);
//        }
//
//        @Override
//        public int clampViewPositionHorizontal(View child, int left, int dx) {
//            return super.clampViewPositionHorizontal(child, left, dx);
//        }
//
//        @Override
//        public int clampViewPositionVertical(View child, int top, int dy) {
//            return super.clampViewPositionVertical(child, top, dy);
//        }
//    }
//
//    ViewDragHelper mHelper = ViewDragHelper.create(this,mCallback );

    private Scroller mScroller;

    public CustomView(Context context) {
        super(context);

        mScroller = new Scroller(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);

    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);

    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
////        return super.onInterceptTouchEvent(ev);
////   return  mHelper.shouldInterceptTouchEvent(ev)
//    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//
//    }


    /**
     * 每次手指触发动作都会调用此方法，每次都获取手指作在位置。
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        mHelper.processTouchEvent(event);
//        return true ;

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                /**当动作为按下时，记下坐标。*/
                LastX = x;
                LastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                int offSetX = x - LastX;
                int offSetY = y - LastY;

//                return detector.onTouchEvent(event);

            /**（1）view进行绘制的时候会调用onLayout()方法来设置显示的位置，
             * 因此我们同样也可以通过修改View的left、top、right、bottom这四种属性来控制View的坐标。*/

//               VelocityTracker tracker= VelocityTracker.obtain();
//                tracker.addMovement(event);
//                tracker.computeCurrentVelocity(1000);
//                int vx= (int) tracker.getXVelocity();
//                tracker.clear();
//                tracker.recycle();


            layout(getLeft() + offSetX, getTop() + offSetY, getRight() + offSetX, getBottom() + offSetY);

            /**（2）对left和right进行偏移、对top和bottom进行偏移*/
            offsetLeftAndRight(offSetX);
            offsetTopAndBottom(offSetY);

            /**（3）
             * LayoutParams主要保存了一个View的布局参数，因此我们可以通过LayoutParams来改变View的布局的参数从而达到了改变View的位置的效果
             * 因为父控件是LinearLayout，所以我们用了LinearLayout.LayoutParams，
             * 如果父控件是RelativeLayout则要使用RelativeLayout.LayoutParams。
             * 除了使用布局的LayoutParams外，我们还可以用ViewGroup.MarginLayoutParams来实现：*/
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            layoutParams.leftMargin = getLeft() + offSetX;
            layoutParams.topMargin = getTop() + offSetY;
            setLayoutParams(layoutParams);

                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            layoutParams.leftMargin = getLeft() + offSetX;
            layoutParams.topMargin = getTop() + offSetY;
            setLayoutParams(layoutParams);

            /**(4)scollBy最终要调用scollTo。
             * scollTo、scollBy移动的是View的内容，
             * 如果在ViewGroup中使用则是移动他所有的子View。*/
//                这里要实现CustomView随着我们手指移动的效果的话，我们就需要将偏移量设置为负值。
            ((View) getParent()).scrollBy(-offSetX, -offSetY);

            break;
        }

        return true;
    }


    /**
     * (5)我们用scollTo/scollBy方法来进行滑动时，这个过程是瞬间完成的,
     * 这里我们可以使用Scroller来实现有过度效果的滑动.
     * Scroller本身是不能实现View的滑动的，它需要配合View的computeScroll()方法才能弹性滑动的效果。
     */
    @Override
    public void computeScroll() {

//        if (mHelper.continueSettling(true)){
//            ViewCompat.postInvalidateOnAnimation(this);
//        }

        super.computeScroll();
        /**系统会在绘制View的时候在draw()方法中调用该方法，
         * 这个方法中我们调用父类的scrollTo()方法并通过Scroller来不断获取当前的滚动值，
         * 每滑动一小段距离我们就调用invalidate()方法不断的进行重绘，重绘就会调用computeScroll()方法，
         * 这样我们就通过不断的移动一个小的距离并连贯起来就实现了平滑移动的效果：*/
        if (mScroller.computeScrollOffset()) {
            ((View) getParent()).scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //通过不断的重绘不断的调用computeScroll方法
            invalidate();
        }
    }


    /**
     * 在CustomView中写一个smoothScrollTo()方法，
     * 调用Scroller.startScroll()方法，在2000毫秒内沿X轴平移delta像素：
     */
    public void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        //1000秒内滑向destX
        mScroller.startScroll(scrollX, 0, delta, 0, 2000);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }


}
