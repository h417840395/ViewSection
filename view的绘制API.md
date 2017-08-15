# view的绘制API
[TOC]
---
### Canvas相关
> - ***canvas.translate(float dx, float dy)*** //坐标系原点移动至（x,y）
> - canvas.scale (float sx, float sy)//按照坐标系原点缩放
> - canvas.scale (float sx, float sy, float px, float py) //缩放 px py为缩放中心
> - ***canvas.rotate (float degrees)***//顺时针为+，degrees单位是度
> - canvas.rotate (float degrees, float px, float py) //设置旋转中心并旋转
> - canvas.skew (float sx, float sy) //sx:将画布在x方向上倾斜相应的角度，sx倾斜角度的tan值
> - ***canvas.save()*** // 把当前的画布的状态进行保存，然后放入特定的栈中
> - canvas.saveLayerXxx() //新建一个图层，并放入特定的栈中
> - ***canvas.restore()***//把栈中最顶层的画布状态取出来，并按照这个状态恢复当前的画布,***与save（）成对出现***
> - canvas.restoreToCount()	//弹出指定位置及其以上所有的状态，并按照指定位置的状态进行恢复
> - canvas.getSaveCount() //获取栈中内容的数量(即保存次数)
> - canvas.clipRect(left, top, right, bottom);  范围裁切
> - clipPath(Path p)其实和 clipRect() 用法完全一样，只是把参数换成了 Path，所以能裁切的形状更多一些
> - canvas.drawColor(@ColorInt int color) //颜色填充
> - canvas.drawRGB(100, 200, 100);   //颜色填充
> - canvas.drawARGB(100, 100, 200, 100);   //颜色填充
> - drawCircle(float centerX, float centerY, float radius, Paint paint)// 画圆
> - drawRect(float left, float top, float right, float bottom, Paint paint)// 画矩形
> - drawPoint(float x, float y, Paint paint) //画点
> - drawPoints(float[] pts, int offset, int count, Paint paint)  //批量画点 count为x、y坐标数，8即画四个点
/ drawPoints(float[] pts, Paint paint) //批量画点
> - drawLine(float startX, float startY, float stopX, float stopY, Paint paint) 画线
> - drawLines(float[] pts, int offset, int count, Paint paint) / drawLines(float[] pts, Paint paint) //批量划线（四个坐标即两个点确定一条直线，float[]中四个坐标为一组）
> - drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint)// 画圆角矩形
> - drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) //绘制弧形或扇形 boolean useCenter确定是否连接到圆心
> - ***drawPath(Path path, Paint paint)*** //画自定义图形
> - ***drawBitmap(Bitmap bitmap, float left, float top, Paint paint)***// 画 Bitmap
> - drawText(String text, float x, float y, Paint paint) //绘制文字

### Paint相关
> - ***Paint.setStyle(Style style)*** //设置绘制模式
> - ***Paint.setColor(int color)*** //设置颜色
> - paint.setStrokeCap(Paint.Cap.ROUND)// 画笔形状，决定末端是圆角还是方形
> - Paint.setStrokeWidth(float width) //设置线条宽度
> - Paint.setTextSize(float textSize) //设置文字大小
> - Paint.setStrokeJoin(Paint.Join join)//设置拐角形状；；MITER 尖角、 BEVEL 平角和 ROUND 圆角。默认为 MITER。
> - ***Paint.setAntiAlias(boolean aa)*** //设置抗锯齿开关
> - Paint.setShader(Shader shader) //设置着色器
    - LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, Shader.TileMode tile) //线性渐变
    - RadialGradient(float centerX, float centerY, float radius, int centerColor, int edgeColor, TileMode tileMode) //辐射渐变
    - SweepGradient(float cx, float cy, int color0, int color1) //扫描渐变
    - BitmapShader(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY)//用 Bitmap 的像素来作为图形或文字的填充
    - ComposeShader(Shader shaderA, Shader shaderB, PorterDuff.Mode mode) //混合两个Shader 
    - 关于Shader.TileMode tile的取值CLAMP, MIRROR 和 REPEAT详见[](http://hencoder.com/ui-1-2/)
    - 关于PorterDuff.Mode mode的17种算法详见
> - Paint.setColorFilter(ColorFilter colorFilter)//颜色过滤
    - LightingColorFilter(int mul, int add) // 参数里的 mul 和 add 都是和颜色值格式相同的 int 值，其中 mul 用来和目标像素相乘，add 用来和目标像素相加
    - PorterDuffColorFilter(int color, PorterDuff.Mode mode) //其中的 color 参数是指定的颜色， mode 参数是指定的 Mode。
    - ColorMatrixColorFilter
> -  Paint.setXfermode(Xfermode xfermode)// 严谨地讲， Xfermode 指的是你要绘制的内容和 Canvas 的目标位置的内容应该怎样结合计算出最终的颜色。但通俗地说，其实就是要你以绘制的内容作为源图像，以 View 中已有的内容作为目标图像，选取一个 PorterDuff.Mode 作为绘制内容的颜色处理方案???
> - setShadowLayer(float radius, float dx, float dy, int shadowColor)//在之后的绘制内容下面加一层阴影。radius 是阴影的模糊范围； dx dy 是阴影的偏移量； shadowColor 是阴影的颜色。
> - setMaskFilter(MaskFilter maskfilter) 
    - BlurMaskFilter(float radius, BlurMaskFilter.Blur style)//radius 参数是模糊的范围， style 是模糊的类型。一共有四种：
        - NORMAL: 内外都模糊绘制
        - SOLID: 内部正常绘制，外部模糊
        - INNER: 内部模糊，外部不绘制
        - OUTER: 内部不绘制，外部模糊
    - EmbossMaskFilter 浮雕效果的 MaskFilter。

### Path相关
> - addCircle(float x, float y, float radius, Direction dir) //添加圆
> - addOval(float left, float top, float right, float bottom, Direction dir) / addOval(RectF oval, Direction dir) //添加椭圆
> - addRect(float left, float top, float right, float bottom, Direction dir) / addRect(RectF rect, Direction dir) //添加矩形
> - addRoundRect(RectF rect, float rx, float ry, Direction dir) 
/ addRoundRect(float left, float top, float right, float bottom, float rx, float ry, Direction dir) 
/ addRoundRect(RectF rect, float[] radii, Direction dir)
/ addRoundRect(float left, float top, float right, float bottom, float[] radii, Direction dir) //添加圆角矩形
> - addPath(Path path) //添加另一个 Path
> - ***lineTo(float x, float y)*** / rLineTo(float x, float y) //画直线
> - quadTo(float x1, float y1, float x2, float y2)
/ rQuadTo(float dx1, float dy1, float dx2, float dy2)// 画二次贝塞尔曲线
> - cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) / rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) //画三次贝塞尔曲线
关于贝赛尔曲线：[安卓自定义View进阶-Path之贝塞尔曲线](http://www.gcssloop.com/customview/Path_Bezier)
> - ***moveTo(float x, float y)*** / rMoveTo(float x, float y) //移动到目标位置
> - arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) 
/ arcTo(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo) 
/ arcTo(RectF oval, float startAngle, float sweepAngle)// 画弧形
addArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle) 
/ addArc(RectF oval, float startAngle, float sweepAngle)
> - ***close()*** 封闭当前子图形
> - setPathEffect(PathEffect effect)
    - CornerPathEffect //把所有拐角变成圆角
    - DiscretePathEffect //把线条进行随机的偏离
    - DashPathEffect //使用虚线来绘制线条
    - PathDashPathEffect //这个方法比 DashPathEffect 多一个前缀 Path ，所以顾名思义，它是使用一个 Path 来绘制「虚线」
    - SumPathEffect //这是一个组合效果类的 PathEffect 。它的行为特别简单，就是分别按照两种 PathEffect 分别对目标进行绘制。
    - ComposePathEffect //这也是一个组合效果类的 PathEffect 。不过它是先对目标 Path 使用一个 PathEffect，然后再对这个改变后的 Path 使用另一个 PathEffect。
> - Path.setFillType(Path.FillType ft) 设置填充方式,与绘制闭合图形的参数Direction dir有关,常见取值如下，详见[HenCoder Android 开发进阶: 自定义 View 1-1 绘制基础](http://hencoder.com/ui-1-1/)
[安卓自定义View进阶-Path之完结篇](http://www.gcssloop.com/customview/Path_Over)
    - EVEN_ODD
    - WINDING （默认值）
    - INVERSE_EVEN_ODD
    - INVERSE_WINDING
### Matix相关 （矩阵）
> Matrix 做常见变换的方式：
> - 创建 Matrix 对象；Matrix matrix = new Matrix();
> - 调用 Matrix 的 pre/postTranslate/Rotate/Scale/Skew() 方法来设置几何变换；
> - 使用 Canvas.setMatrix(matrix) 或 ***Canvas.concat(matrix)*** 来把几何变换应用到 Canvas。
> - Matrix.setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) 用点对点映射的方式设置变换
>详见：
[安卓自定义View进阶-Matrix原理](http://www.gcssloop.com/customview/Matrix_Basic)
[安卓自定义View进阶-Matrix详解](http://www.gcssloop.com/customview/Matrix_Method)
[安卓自定义View进阶-Matrix Camera](http://www.gcssloop.com/customview/matrix-3d-camera)




