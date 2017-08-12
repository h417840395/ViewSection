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

作者：Carson_Ho
链接：http://www.jianshu.com/p/1dab927b2f36
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
