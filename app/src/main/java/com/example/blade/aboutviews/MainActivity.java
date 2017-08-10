package com.example.blade.aboutviews;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CustomView myView = (CustomView) findViewById(R.id.custom_view);

        /**（*）采用View动画来移动，在res目录新建anim文件夹并创建trans.xml：
         * 加载动画*/
//        myView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.tans));

        /**（*）使用属性动画让CustomView在1000毫秒内沿着X轴像右平移300像素：*/
//        ObjectAnimator.ofFloat(myView,"translationX",0,300).setDuration(1000).start();

//        myView.smoothScrollTo(-400,0);

        ((ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content)).getChildAt(0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    

}
