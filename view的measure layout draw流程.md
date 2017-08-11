## view的measure layout draw流程

---
[toc]
### view的onMeasure()
> 关键类 MeasureSpec 
>MeasureSpec类帮助我们来测量View，它是一个32位的int值，高两位为specMode （测量的模式），低30位为specSize （测量的大小）。其有两个方法getXXX()分别用来获取模式或大小。
```
public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        private static final int MODE_MASK  = 0x3 << MODE_SHIFT;
        /**
         * Measure specification mode: The parent has not imposed any constraint
         * on the child. It can be whatever size it wants.
         */
        public static final int UNSPECIFIED = 0 << MODE_SHIFT;
        /**
         * Measure specification mode: The parent has determined an exact size
         * for the child. The child is going to be given those bounds regardless
         * of how big it wants to be.
         */
        public static final int EXACTLY     = 1 << MODE_SHIFT;
        /**
         * Measure specification mode: The child can be as large as it wants up
         * to the specified size.
         */
        public static final int AT_MOST     = 2 << MODE_SHIFT;
...
 public static int getMode(int measureSpec) {
            return (measureSpec & MODE_MASK);
        }
  public static int getSize(int measureSpec) {
            return (measureSpec & ~MODE_MASK);
        }
...        
}
```
> 测量模式分为三种:
> - UNSPECIFIED：未指定模式，View想多大就多大，父容器不做限制，一般用于系统内部的测量。(基本不咋用)
> - EXACTLY：精确模式，对应于match_parent属性和确切的数值，父容器测量出View所需要的大小，也就是specSize的值。(大小match_parent时，parent的大小是确定的，因此此view大小为exactly)
> - AT_MOST：最大模式，对应于wrap_comtent属性，只要尺寸不超过父控件允许的最大尺寸就行。(此view的大小不能超过parent因此为atMost,在未修改setMeasuredDimension()方法时,view仅仅支持EXACTLY模式,即不支持warp_content。若要支持要修改setMeasuredDimension()，并且设定view最小值——warp_content。)
> 关于onMeasure()(View.java)：
```
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
             getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
 }
 //setMeasuredDimension()是赋值的关键方法,查看getDefaultSize()
public static int getDefaultSize(int size, int measureSpec) {
    int result = size;
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    switch (specMode) {
    case MeasureSpec.UNSPECIFIED:
        result = size;
        break;
    case MeasureSpec.AT_MOST:
 //  在此可证明，如果不修改setMeasuredDimension()的参数，AT_MOST状态不生效，即不支持warp_content
    case MeasureSpec.EXACTLY:
        result = specSize;
        break;
    }
    return result;
} 
```
### ViewGroup的measure流程
> 对于ViewGroup，它不只要measure自己本身，还要遍历的调用子元素的measure()方法，ViewGroup中没有定义onMeasure()(ViewGroup是一个抽象类，需要子类去实现onMeasure(),因为每个viewGroup的表现形式各不相同，如LinearLayout、FrameLayout、etc)方法，但他定义了measureChildren()方法（ViewGroup.java）：
```
protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
       final int size = mChildrenCount;
       final View[] children = mChildren;
       for (int i = 0; i < size; ++i) {
           final View child = children[i];
           if ((child.mViewFlags & VISIBILITY_MASK) != GONE) {
            // 遍历子视图，调用 measureChild(）
               measureChild(child, widthMeasureSpec, heightMeasureSpec);
           }
       }
   }
 
   protected void measureChild(View child, int parentWidthMeasureSpec,
           int parentHeightMeasureSpec) {
       final LayoutParams lp = child.getLayoutParams();
       final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
               mPaddingLeft + mPaddingRight, lp.width);
       final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
               mPaddingTop + mPaddingBottom, lp.height);
       child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
   }
   
   //根据父容器的MeasureSpec的模式再结合子元素的LayoutParams属性来得出子元素的MeasureSpec属性
   public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
    int specMode = MeasureSpec.getMode(spec);
    int specSize = MeasureSpec.getSize(spec);
    int size = Math.max(0, specSize - padding);
    int resultSize = 0;
    int resultMode = 0;
    switch (specMode) {
    // Parent has imposed an exact size on us
    case MeasureSpec.EXACTLY:
        if (childDimension >= 0) {
            resultSize = childDimension;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            // Child wants to be our size. So be it.
            resultSize = size;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            // Child wants to determine its own size. It can't be
            // bigger than us.
            resultSize = size;
            resultMode = MeasureSpec.AT_MOST;
        }
        break;
    // Parent has imposed a maximum size on us
    case MeasureSpec.AT_MOST:
        if (childDimension >= 0) {
            // Child wants a specific size... so be it
            resultSize = childDimension;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            // Child wants to be our size, but our size is not fixed.
            // Constrain child to not be bigger than us.
            resultSize = size;
            resultMode = MeasureSpec.AT_MOST;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            // Child wants to determine its own size. It can't be
            // bigger than us.
            resultSize = size;
            resultMode = MeasureSpec.AT_MOST;
        }
        break;
    // Parent asked to see how big we want to be
    case MeasureSpec.UNSPECIFIED:
        if (childDimension >= 0) {
            // Child wants a specific size... let him have it
            resultSize = childDimension;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            // Child wants to be our size... find out how big it should
            // be
            resultSize = 0;
            resultMode = MeasureSpec.UNSPECIFIED;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            // Child wants to determine its own size.... find out how
            // big it should be
            resultSize = 0;
            resultMode = MeasureSpec.UNSPECIFIED;
        }
        break;
    }
    return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
}
```
> 极端情况下，系统可能要多次measure才能获得最终的宽高，此时在onMeasure()获得的狂傲可能是不准确的，最好在onLayout()方法中去获取。
> 在activity的onCreate（），onStart（），onResume（）是无法正确取得view的宽高信息的。因为view的measure过程与activity生命周期不一致，因此在这三个周期内获取view的大小不可行，如果view还未测量完毕则取值都为0。
>获取view大小的四种方式：详见[**《android开发艺术探索》p190**]()
> - Activity的onWindowFocusChanged() 
> - view.post(runnable)
> - ViewTreeOberver
> - view.mearsure()
### layout过程
> viewGroup的layout()(确定子view的位置)->viewGroup的onLayout()(遍历所有子view并调用他们的layout())->子view的layout(）->子view的onLayout()
> 详见:[从源码解析View的layout和draw流程](http://liuwangshu.cn/application/view/8-layout-sourcecode.html)
### View的draw流程
```
public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;   
        // Step 1, draw the background, if needed
        int saveCount;
        if (!dirtyOpaque) {
            drawBackground(canvas);
        }
...
   // Step 2, save the canvas' layers
        int paddingLeft = mPaddingLeft;
        final boolean offsetRequired = isPaddingOffsetRequired();
        if (offsetRequired) {
            paddingLeft += getLeftPaddingOffset();
        }
...
  // Step 3, draw the content
        if (!dirtyOpaque) onDraw(canvas);
        // Step 4, draw the children
        dispatchDraw(canvas);
...
   // Step 5, draw the fade effect and restore layers
        final Paint p = scrollabilityCache.paint;
        final Matrix matrix = scrollabilityCache.matrix;
        final Shader fade = scrollabilityCache.shader;
...
  // Step 6, draw decorations (scrollbars)
        onDrawScrollBars(canvas);
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }
   }
```
> 从源码的注释我们看到draw流程有六个步骤，其中第2步和第5步可以跳过：
> - 如果有设置背景，则绘制背景
> - //保存canvas层
> - 绘制自身内容
> - 如果有子元素则绘制子元素(通过dispatchDraw()，遍历子元素并调用其draw(),将事件传递下去)
> - //绘制效果
> - 绘制装饰品(scrollbars)







