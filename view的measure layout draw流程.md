## view的measure layout draw流程

---
[toc]
### view的onMeasure()
> 关键类 MeasureSpec 
>MeasureSpec类帮助我们来测量View，它是一个32位的int值，高两位为specMode （测量的模式），低30位为specSize （测量的大小）。其有两个方法getXXX()分别用来获取模式或大小。

![MeasureSpec](https://github.com/h417840395/ViewSection/blob/master/pic4md/3985563-d3bf0905aeb8719b.png)
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
> 关于onMeasure()：
```
//View.java
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
> 那么MeasureSpec又是如何确定的？

>对于DecorView，其确定是通过屏幕的大小，和自身的布局参数LayoutParams。这部分很简单，根据LayoutParams的布局格式（match_parent，wrap_content或指定大小），将自身大小，和屏幕大小相比，设置一个不超过屏幕大小的宽高，以及对应模式。

> 对于其他View（包括ViewGroup），其确定是通过父布局的MeasureSpec和自身的布局参数LayoutParams。

![么MeasureSpec又是如何确定的](https://github.com/h417840395/ViewSection/blob/master/pic4md/3985563-e3f20c6662effb7b.png)

### ViewGroup的measure流程
![onMeasure()](https://github.com/h417840395/ViewSection/blob/master/pic4md/3985563-d1a57294428ff668.png)
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
  //作用：
/ 根据父视图的MeasureSpec & 布局参数LayoutParams，计算单个子View的MeasureSpec
//即子view的确切大小由两方面共同决定：父view的MeasureSpec 和 子view的LayoutParams属性 


public static int getChildMeasureSpec(int spec, int padding, int childDimension) {  

 //参数说明
 * @param spec 父view的详细测量值(MeasureSpec) 
 * @param padding view当前尺寸的的内边距和外边距(padding,margin) 
 * @param childDimension 子视图的布局参数（宽/高）

    //父view的测量模式
    int specMode = MeasureSpec.getMode(spec);     

    //父view的大小
    int specSize = MeasureSpec.getSize(spec);     

    //通过父view计算出的子view = 父大小-边距（父要求的大小，但子view不一定用这个值）   
    int size = Math.max(0, specSize - padding);  

    //子view想要的实际大小和模式（需要计算）  
    int resultSize = 0;  
    int resultMode = 0;  

    //通过父view的MeasureSpec和子view的LayoutParams确定子view的大小  


    // 当父view的模式为EXACITY时，父view强加给子view确切的值
   //一般是父view设置为match_parent或者固定值的ViewGroup 
    switch (specMode) {  
    case MeasureSpec.EXACTLY:  
        // 当子view的LayoutParams>0，即有确切的值  
        if (childDimension >= 0) {  
            //子view大小为子自身所赋的值，模式大小为EXACTLY  
            resultSize = childDimension;  
            resultMode = MeasureSpec.EXACTLY;  

        // 当子view的LayoutParams为MATCH_PARENT时(-1)  
        } else if (childDimension == LayoutParams.MATCH_PARENT) {  
            //子view大小为父view大小，模式为EXACTLY  
            resultSize = size;  
            resultMode = MeasureSpec.EXACTLY;  

        // 当子view的LayoutParams为WRAP_CONTENT时(-2)      
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {  
            //子view决定自己的大小，但最大不能超过父view，模式为AT_MOST  
            resultSize = size;  
            resultMode = MeasureSpec.AT_MOST;  
        }  
        break;  

    // 当父view的模式为AT_MOST时，父view强加给子view一个最大的值。（一般是父view设置为wrap_content）  
    case MeasureSpec.AT_MOST:  
        // 道理同上  
        if (childDimension >= 0) {  
            resultSize = childDimension;  
            resultMode = MeasureSpec.EXACTLY;  
        } else if (childDimension == LayoutParams.MATCH_PARENT) {  
            resultSize = size;  
            resultMode = MeasureSpec.AT_MOST;  
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {  
            resultSize = size;  
            resultMode = MeasureSpec.AT_MOST;  
        }  
        break;  

    // 当父view的模式为UNSPECIFIED时，父容器不对view有任何限制，要多大给多大
    // 多见于ListView、GridView  
    case MeasureSpec.UNSPECIFIED:  
        if (childDimension >= 0) {  
            // 子view大小为子自身所赋的值  
            resultSize = childDimension;  
            resultMode = MeasureSpec.EXACTLY;  
        } else if (childDimension == LayoutParams.MATCH_PARENT) {  
            // 因为父view为UNSPECIFIED，所以MATCH_PARENT的话子类大小为0  
            resultSize = 0;  
            resultMode = MeasureSpec.UNSPECIFIED;  
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {  
            // 因为父view为UNSPECIFIED，所以WRAP_CONTENT的话子类大小为0  
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
![onLayout()](https://github.com/h417840395/ViewSection/blob/master/pic4md/3985563-8aefac42b3912539.png)
> viewGroup的layout()(确定子view的位置)->viewGroup的onLayout()(遍历所有子view并调用他们的layout())->子view的layout(）->子view的onLayout()
```
public void layout(int l, int t, int r, int b) {  

    // 当前视图的四个顶点
    int oldL = mLeft;  
    int oldT = mTop;  
    int oldB = mBottom;  
    int oldR = mRight;  

    // setFrame（） / setOpticalFrame（）：确定View自身的位置
    // 即初始化四个顶点的值，然后判断当前View大小和位置是否发生了变化并返回  
 boolean changed = isLayoutModeOptical(mParent) ?
            setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);

    //如果视图的大小和位置发生变化，会调用onLayout（）
    if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {  

        // onLayout（）：确定该View所有的子View在父容器的位置     
        onLayout(changed, l, t, r, b);      
  ...

}


//确定View自身的位置的方法
protected boolean setFrame(int left, int top, int right, int bottom) {
    ...
// 通过以下赋值语句记录下了视图的位置信息，即确定View的四个顶点
// 即确定了视图的位置
    mLeft = left;
    mTop = top;
    mRight = right;
    mBottom = bottom;

    mRenderNode.setLeftTopRightBottom(mLeft, mTop, mRight, mBottom);
}

/*
* setOpticalFrame（）源码分析
**/

private boolean setOpticalFrame(int left, int top, int right, int bottom) {
    Insets parentInsets = mParent instanceof View ?
            ((View) mParent).getOpticalInsets() : Insets.NONE;
    Insets childInsets = getOpticalInsets();

// setOpticalFrame（）实际上是调用setFrame（）
    return setFrame(
            left   + parentInsets.left - childInsets.left,
            top    + parentInsets.top  - childInsets.top,
            right  + parentInsets.left + childInsets.right,
            bottom + parentInsets.top  + childInsets.bottom);
}

```

> 不同的是ViewGroup先在layout()中确定自己的布局（ setFrame（） / setOpticalFrame（）），然后在onLayout()方法中再调用子View的layout()方法，让子View布局。在Measure过程中，ViewGroup一般是先测量子View的大小，然后再确定自身的大小。

> viewGroup的复写模板
```
@Override  
protected void onLayout(boolean changed, int l, int t, int r, int b) {  

  // 参数说明
 * @param changed 当前View的大小和位置改变了 
 * @param l   即left，父View的左部位置
 * @param t   即top，父View的顶部位置
 * @param r   即right，父View的右部位置
 * @param b  即bottom，父View的底部位置

        // 循环所有子View
        for (int i=0; i<getChildCount(); i++) {
            View child = getChildAt(i);   

            // 计算当前子View的四个位置值
            // 计算的逻辑需要自己实现，也是自定义View的关键
            ...

            // 对计算后的位置值进行赋值
            int mLeft  = Left
            int mTop  = Top
            int mRight = Right
            int mBottom = Bottom

            // 调用子view的layout()并传递计算过的参数
            // 从而计算出子View的位置
            child.layout(mLeft, mTop, mRight, mBottom);
        }
    }
}

作者：Carson_Ho
链接：http://www.jianshu.com/p/158736a2549d
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```

> 详见:[从源码解析View的layout和draw流程](http://liuwangshu.cn/application/view/8-layout-sourcecode.html)
### View的draw流程
![onDraw()](https://github.com/h417840395/ViewSection/blob/master/pic4md/3985563-594f6b3cde8762c7.png)
```
public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;   
        // Step 1, draw the background, if needed
      //  绘制本身View内容  默认为空实现，  自定义View时需要进行复写
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
       //  默认为空实现 单一View中不需要实现，ViewGroup中已经实现该方法
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

> 每一步具体细节详见:[自定义View Draw过程- 最易懂的自定义View原理系列（4）](http://www.jianshu.com/p/95afeb7c8335)

