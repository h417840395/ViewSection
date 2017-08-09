## 实现view滑动的方法
[toc]
### 实现滑动的思想 
> 滑动的方式基本思想都是类似的：当触摸事件传到View时，系统记下触摸点的坐标，手指移动时系统记下移动后的触摸的坐标并算出偏移量，并通过偏移量来修改View的坐标。
### layout()
> view进行绘制的时候会调用onLayout()方法来设置显示的位置，因此我们同样也可以通过修改View的left、top、right、bottom这四种属性来控制View的坐标。首先我们要自定义一个View，在onTouchEvent()方法中获取触摸点的坐标：
```
   public boolean onTouchEvent(MotionEvent event) {
        //获取到手指处的横坐标和纵坐标
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
...
```
> 接下来我们在ACTION_MOVE事件中计算偏移量，再调用layout()方法重新放置这个自定义View的位置：
```
case MotionEvent.ACTION_MOVE:
    //计算移动的距离
    int offsetX = x - lastX;
    int offsetY = y - lastY;
    //调用layout方法来重新放置它的位置
    layout(getLeft()+offsetX, getTop()+offsetY,
            getRight()+offsetX , getBottom()+offsetY);
    break;
```
### offsetLeftAndRight()与offsetTopAndBottom()
> 将ACTION_MOVE中的代码替换成如下代码：
```
case MotionEvent.ACTION_MOVE:
    //计算移动的距离
    int offsetX = x - lastX;
    int offsetY = y - lastY;
    //对left和right进行偏移
    offsetLeftAndRight(offsetX);
    //对top和bottom进行偏移
    offsetTopAndBottom(offsetY);
    break;
```
### LayoutParams（改变布局参数）
> LayoutParams主要保存了一个View的布局参数，因此我们可以通过LayoutParams来改变View的布局的参数从而达到了改变View的位置的效果。同样的我们将ACTION_MOVE中的代码替换成如下代码：
```
LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) getLayoutParams();
              layoutParams.leftMargin = getLeft() + offsetX;
              layoutParams.topMargin = getTop() + offsetY;
              setLayoutParams(layoutParams);
```
> 因为父控件是LinearLayout，所以我们用了LinearLayout.LayoutParams，如果父控件是RelativeLayout则要使用RelativeLayout.LayoutParams。除了使用布局的LayoutParams外，我们还可以用ViewGroup.MarginLayoutParams来实现：
```
ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
layoutParams.leftMargin = getLeft() + offsetX;
layoutParams.topMargin = getTop() + offsetY;
setLayoutParams(layoutParams);
```
### 动画
> [Todo]()
### scollTo与scollBy
> scrollBy实际也是调用scrollTo
> view内部mScrollX值等于View左边缘与其**内容**左边缘之间的距离，通过getScrollX获取；mScrollY值等于View上边缘与其**内容**上边缘之间的距离，通过getScrollY获取；

> scollTo(x,y)表示移动到一个具体的坐标点，而scollBy(dx,dy)则表示移动的增量为dx、dy。其中scollBy最终也是要调用scollTo的。scollTo、scollBy移动的是View的内容，如果在ViewGroup中使用则是移动他所有的子View。我们将ACTION_MOVE中的代码替换成如下代码：
```
((View)getParent()).scrollBy(-offsetX,-offsetY);
//这里要实现CustomView随着我们手指移动的效果的话，我们就需要将偏移量设置为负值。
```
### Scroller
> [Scroller](https://github.com/h417840395/ViewSection/blob/master/view%E7%9A%84%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86%E5%8F%8A%E5%B8%B8%E7%94%A8%E5%B7%A5%E5%85%B7%E7%B1%BB.md)
### ViewDragHelper
> [ViewDragHelper](https://github.com/h417840395/ViewSection/blob/master/view%E7%9A%84%E5%9F%BA%E7%A1%80%E7%9F%A5%E8%AF%86%E5%8F%8A%E5%B8%B8%E7%94%A8%E5%B7%A5%E5%85%B7%E7%B1%BB.md)
### 各种滑动方式的对比
> - scrollTo/scrollBy：操作简单，适合对View**内容**的滑动
> - 动画：如果使用补间动画只能实现无交互的动画效果；如果使用属性动画可以实现交互，但仅支持（Android 3.0+）
> - 改变布局参数：操作复杂，使用有交互的View






