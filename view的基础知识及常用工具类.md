## view的基础知识及常用工具类
[TOC]
***
### 什么是view
ViewGroup **extends** View
```
//深序遍历
 findViewById()
```
***
### view相关的参数
![获取坐标的方法](https://github.com/h417840395/ViewSetion/blob/master/pic4md/20160221170553904.png)


> view的方法
```
getHeight()//获取View自身高度
getWidth()//获取View自身宽度

getTop()//获取View自身顶边到其父布局顶边的距离
getLeft()//获取View自身左边到其父布局左边的距离
getRight()//获取View自身右边到其父布局左边的距离
getBottom()//获取View自身底边到其父布局顶边的距离
```
> MotionEvent的方法
```
getX()//获取点击事件距离控件左边的距离，即视图坐标
getY()//获取点击事件距离控件顶边的距离，即视图坐标
getRawX()//获取点击事件距离整个屏幕左边距离，即绝对坐标
getRawY()//获取点击事件距离整个屏幕顶边的的距离，即绝对坐标
```

> Android3.0开始，View增加了额外参数：X，Y，translationX，translationY。
> view在平移过程中，top和left是原始左上角坐标，不会发生改变，而此改变的是X，Y，translationX，translationY。
```
x=left+tanslationX;
Y=top+tanslationY;
//x和y是view左上角坐标，translationX，translationY是View左上角相对父容器的偏移量
```
***
### MotionEvent
> MotionEvent常见动作
```


public static final int ACTION_DOWN             = 0;
    
    /**
     * Constant for {@link #getActionMasked}: A pressed gesture has finished, the
     * motion contains the final release location as well as any intermediate
     * points since the last down or move event.
     */
    public static final int ACTION_UP               = 1;
    
    /**
     * Constant for {@link #getActionMasked}: A change has happened during a
     * press gesture (between {@link #ACTION_DOWN} and {@link #ACTION_UP}).
     * The motion contains the most recent point, as well as any intermediate
     * points since the last down or move event.
     */
    public static final int ACTION_MOVE             = 2;
    
    /**
     * Constant for {@link #getActionMasked}: The current gesture has been aborted.
     * You will not receive any more points in it.  You should treat this as
     * an up event, but not perform any action that you normally would.
     */
    public static final int ACTION_CANCEL           = 3;
    
    /**
     * Constant for {@link #getActionMasked}: A movement has happened outside of the
     * normal bounds of the UI element.  This does not provide a full gesture,
     * but only the initial location of the movement/touch.
     */
    public static final int ACTION_OUTSIDE          = 4;

    /**
     * Constant for {@link #getActionMasked}: A non-primary pointer has gone down.
     * <p>
     * Use {@link #getActionIndex} to retrieve the index of the pointer that changed.
     * </p><p>
     * The index is encoded in the {@link #ACTION_POINTER_INDEX_MASK} bits of the
     * unmasked action returned by {@link #getAction}.
     * </p>
     */
```

>方法模板
```
//复写view的onTouchEvent() 每次触摸都回调此方法
   @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
               
                break;

            case MotionEvent.ACTION_MOVE:
              
                break;
			.........
        }

        return true;
    }
```
***
### TouchSlop
> TouchSlop 是系统所能识别出的滑动最小距离，当两次滑动键的距离小于此常量不视为滑动。此常量与设备有关；
```
                ViewConfiguration.get(Context).getScaledTouchSlop()
```
***

### VelocityTracker
> VelocityTracker  在onTouchEvent()中获取速度
```
               VelocityTracker tracker= VelocityTracker.obtain();//获取VelocityTracker
               tracker.addMovement(event);//在onTouchEvent()中得到event对象
               
                tracker.computeCurrentVelocity(1000);//首先要计算速度 参数为时间（ms）
                int vx= (int) tracker.getXVelocity();//例如获取x轴速度
                
                tracker.clear();
                tracker.recycle();//最后一定要回收
```
***

### GestureDetector
>GestureDetector 手势检测，用于辅助检测单击，双击，滑动，长按等。
>详见**《Android开发艺术探索》p127**
```
              GestureDetector detector=new GestureDetector(this);
              // 首先获取GestureDetector，view实现OnGestureListener即可实现检测功能，
              //实现OnDoubleTapListner可检测双击
              
              return detector.onTouchEvent(event);
              //在onTouchEven()中将event托管给GestureDetector 
              //detector.onTouchEvent(event)返回值为布尔型
```
***
### Scroller
>Scroller 用于实现弹性滑动，其本身不能实现弹性滑动，需要配合view的computeScroll()实现
```
//获取scroller
 public CustomView(Context context, AttributeSet attrs) {
      super(context, attrs);
      mScroller = new Scroller(context);
  }
```
> 系统会在绘制View的时候在draw()方法中调用 **computeScroll()**，
> 即 invalidate()->onDraw->computeScroll()->invalidate()循环。
> 而Scroller的getCurrX(),getCurrY()方法可以获得当前的滑动坐标，用父视图的**scrollTo**方法滑动到当前位置
> 通过computeScrollOffset()跳出循环

```
//复写view的方法
@Override
    public void computeScroll() {
        super.computeScroll();
      
        if (mScroller.computeScrollOffset()) {
            ((View) getParent()).scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //通过不断的重绘不断的调用computeScroll方法
            invalidate();
        }
    }
```
>startScroll(int startX, int startY, int dx, int dy, int duration)
>通过getSrollX(),getSrollY()来获取父视图中content滑动到点坐标
```
	//创建用于滚动的方法
   public void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
       
		//startScroll(int startX, int startY, int dx, int dy, int duration)
        mScroller.startScroll(scrollX, 0, delta, 0, 2000); //2000秒内滑向destX
        invalidate();
    }
```
>  更多关于Scroller详见：[从源码解析Scroller](http://liuwangshu.cn/application/view/4-scroller-sourcecode.html)

***
### ViewDragHelper
>定义ViewDragHelper，通常定义在ViewGroup内部
```
//传入的第一个参数为viewGroup，第二个参数为下面的回调函数
ViewDragHelper mHelper=ViewDragHelper.create(this, mCallback);

ViewDragHelper.Callback mCallback=new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
			//如果当前触摸的view==child，此时开始检测
            return 当前被触摸的view==child;
        }
    }
```
> 拦截事件
```
//复写viewGroup的onInterceptTouchEvent()，将事件托管给ViewDragHelper 
   @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
		   return  mHelper.shouldInterceptTouchEvent(ev)
    }

//将事件传递给ViewDragHelper 
 @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHelper.processTouchEvent(event);
        return true ;
    }
```
> 处理computeScroll()
> ViewDragHelper 内部通过scroller实现平滑移动所以要复写此方法
```
    @Override
    public void computeScroll() {
        
        if (mHelper.continueSettling(true)){
	        ViewCompat.postInvalidateOnAnimation(this);
        }
   }
        
```
> 实现滑动等功能
> clampViewPositionHorizontal(),clampViewPositionVertical()两个方法分别实现横纵的滑动事件
> 当返回0时不滑动，返回left或top时滑动
> onViewReleased()用于实现手指释放后的逻辑
```
ViewDragHelper.Callback mCallback=new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
           // toDo当view被释放时的逻辑
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return left;
        }
    }
```
> 其他详见**《Android开发艺术探索》p98**
