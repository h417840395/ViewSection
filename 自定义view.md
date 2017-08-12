## 自定义view

---
[toc]
### 常用回调方法
> - onFinishInflate():从XML加载后回调，可获取子view
> - onSizeChanged():大小改变时回调
> - onMeasure():回调该方法进行测量，如果要支持warp_content肯定复写此方法
> - onLayout():回调该方法来显示确定的位置
> - onDraw():绘制view的显示的内容
> - onTouchEvent():监听触摸的回调
> - Invalidate():重绘view（只能在UI线程使用）
> - postInvalidate():重绘view（在非UI线程使用）
### 自定义view的常见方式
> 自定义view须知详见[**《android开发艺术探索》p201**]()
#### 对现有控件进行拓展
> 处理warp_content(复写onMeasure())
> 处理paddding(onDraw()中处理，如果继承viewGroup还需要在onLayout()和onMeasure()中考虑padding和子view的margin)
> view的margin属性是由父视图进行控制，padding由自身进行控制
#### 创建复合控件
> 在values目录下创建 attrs.xml：
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="CustomView">
        <attr name="rect_color" format="color" />
    </declare-styleable>
</resources>
```
> 在构造函数中通过 TypedArray 获取xml中定义的属性
```
public CustomView(Context context, AttributeSet attrs) {
       super(context, attrs);
       //中通过 TypedArray 获取xml中定义的属性
       TypedArray mTypedArray=context.obtainStyledAttributes(attrs,R.styleable.CustomView);
       //提取CustomView属性集合的rect_color属性，如果没设置默认值为Color.RED
       mColor=mTypedArray.getColor(R.styleable.CustomView_rect_color,Color.RED);
       //获取资源后要及时回收！！！！
       mTypedArray.recycle();
      
      
   }
  
```
> 使用LayoutInflater的inflate()方法，第二三个参数分别为this和ture，将不布局填充到自定义的布局中
```
        LayoutInflater.from(context).inflate(R.layout.view_customtitle, this, true);

```
#### 重写veiw实现全新控件
> 继承view关键在于onDraw（）
> 继承viewGroup关键在于onMeasure(),onLayout(),onTouchEvent()等，在于对子布局的测量布局和对事件的控制
> onMeasure()时用getChildCount() == 0判断是否有子布局
> onLayout()时用child.getVisibility() != View.GONE判断子布局的显示状态
> 详见:[自定义ViewGroup](http://liuwangshu.cn/application/view/11-custom-viewgroup.html)




