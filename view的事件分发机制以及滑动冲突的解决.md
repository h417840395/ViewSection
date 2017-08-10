# view的事件分发机制以及滑动冲突的解决

---
[toc]
### 什么是点击事件分发
> 当我们点击屏幕，就产生了触摸事件，这个事件被封装成了一个类：MotionEvent。而当这个MotionEvent产生后，那么系统就会将这个MotionEvent传递给View的层级，MotionEvent在View的层级传递的过程就是点击事件分发。

### 点击事件分发的重要方法

> - dispatchTouchEvent(MotionEvent ev)：用来进行事件的分发。如果事件能传递到此view，那么此方法一定会调用。
> - onInterceptTouchEvent(MotionEvent ev)：用来进行事件的拦截，在dispatchTouchEvent()中调用，需要注意的是**View没有提供该方法**。默认返回false，即都不拦截。
> - onTouchEvent(MotionEvent ev)：用来处理点击事件，在dispatchTouchEvent()方法中进行。调用默认返回false，即未消费。
三个方法间的关系如下伪代码：
```
public boolean dispatchTouchEvent(MotionEvent ev) {
boolean result=false;

//自己拦截不，拦截了自己干，不拦截给儿子
if(onInterceptTouchEvent(ev)){
      result=super.onTouchEvent(ev);
 }else{
      result=child.dispatchTouchEvent(ev);
}
return result;
```
>OnTouchListener(),onTouchEvent(),onClickListner()优先级依次递减，若OnTouchListener()的onTouch()方法返回ture则表示事件被消费，onTouchEvent()失效。如下。
```
public boolean onTouchEvent(MotionEvent event) {
     ...省略
       final int action = event.getAction();
       if (((viewFlags & CLICKABLE) == CLICKABLE ||
               (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) ||
               (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE) {
           switch (action) {
               case MotionEvent.ACTION_UP:
                   boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                   if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                       // take focus if we don't have it already and we should in
                       // touch mode.
                       boolean focusTaken = false;
                      
                       if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                           // This is a tap, so remove the longpress check
                           removeLongPressCallback();
                           // Only perform take click actions if we were in the pressed state
                           if (!focusTaken) {
                               // Use a Runnable and post this rather than calling
                               // performClick directly. This lets other visual state
                               // of the view update before click actions start.
                               if (mPerformClick == null) {
                                   mPerformClick = new PerformClick();
                               }
                               if (!post(mPerformClick)) {
                                   performClick();
                               }
                           }
                       }
      ...省略    
       }
       return true;
      }          
      return false;
   }
```

> 其他常见结论详见：[**《Android开发艺术探索》p142**]()
### Activity对点击事件的分发过程
> 当一个点击事件产生后，其传递过程为：Activity->Window->View
> 当点击事件发生时，事件最先传递给当前Activity，由其dispatchTouchEvent进行分发。
```
  public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
        //事件给window，若返回ture则结束
            return true;
        }
        //window返回false则自己搞
        return onTouchEvent(ev);
    }
```
> window为抽象类，其实现为PhoneWindow
```
public Window getWindow() {
    return mWindow;
}

 final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor) {
        attachBaseContext(context);
        mFragments.attachHost(null /*parent*/);
        
       // 可见mWindow就是PhoneWindow！！！！
        mWindow = new PhoneWindow(this);
...省略
｝
```
>在PhoneWindow中,将事件传递给mDecor
```
  @Override
    public boolean superDispatchTouchEvent(MotionEvent event) {
        return mDecor.superDispatchTouchEvent(event);
    }

```
> 在PhoneWindow中，超找mDecor，可见这个DecorView就是Activity中的根View。接着查看DecorView的源码，发现DecorView是PhoneWindow类的内部类，并且继承FrameLayout。
> ((ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content)).getChildAt(0);即可取得Activity设置的view，即setContentView()设置的view。



```
 @Override
    public final View getDecorView() {
        if (mDecor == null || mForceDecorInstall) {
            installDecor();
        }
        return mDecor;
    }
    
    
    protected DecorView generateDecor() {
     return new DecorView(getContext(), -1);
 }
```
> 由此可见Activity的构成, 详情可见:[从源码解析Activity的构成](http://liuwangshu.cn/application/view/6-activity-constitute.html)
![Activity的构成]()
> 这也就说明为何隐藏toolbar操作要在setContentView()之前。


>事件到达viewGroup后，在dispatchTouchEvent(event)中进行分发，查看ViewGroup的dispatchTouchEvent()方法
> 详见[**《Android开发艺术探索》p146**]()
```
@Override
   public boolean dispatchTouchEvent(MotionEvent ev) {
      ...省略
      
     // 当事件未拦截并且被子view成功处理后mFirstTouchTarget便被赋值，那么当事件被拦截时mFirstTouchTarget 肯定为 null。
     // 此时当事件ACTION_MOVE和ACTION_UP发生时actionMasked == MotionEvent.ACTION_DOWN || mFirstTouchTarget != null肯定为false，则不在调用 onInterceptTouchEvent(ev),那么统一序列中的事件都由此viewGroup处理
      
           if (actionMasked == MotionEvent.ACTION_DOWN
                   || mFirstTouchTarget != null) {
               final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
               
               //但是FLAG_DISALLOW_INTERCEPT为false时仍然会调用onInterceptTouchEvent(ev)。这个标记通过子view的requestDisallowInterceptTouchEvent()设置。
               //设置后viewGroupe无法拦截除了ACTION_DOWN外的其他事件。因为当ACTION_DOWN发生时，viewGroup总会清除FLAG_DISALLOW_INTERCEPT。
               if (!disallowIntercept) {
                   intercepted = onInterceptTouchEvent(ev);
                   ev.setAction(action); // restore action in case it was changed
               } else {
                   intercepted = false;
               }
           } else {
               // There are no touch targets and this action is not an initial down
               // so this view group continues to intercept touches.
               intercepted = true;
           }
          ...省略
       return handled;
   }
```
> 当viewGroup不拦截事件是，交给子view处理，源码如下：
```
 public boolean dispatchTouchEvent(MotionEvent ev) {
 ...省略
              final View[] children = mChildren;
              for (int i = childrenCount - 1; i >= 0; i--) {
                            final int childIndex = customOrder
                                    ? getChildDrawingOrder(childrenCount, i) : i;
                            final View child = (preorderedList == null)
                                    ? children[childIndex] : preorderedList.get(childIndex);
                            // If there is a view that has accessibility focus we want it
                            // to get the event first and if not handled we will perform a
                            // normal dispatch. We may do a double iteration but this is
                            // safer given the timeframe.
                            if (childWithAccessibilityFocus != null) {
                                if (childWithAccessibilityFocus != child) {
                                    continue;
                                }
                                childWithAccessibilityFocus = null;
                                i = childrenCount - 1;
                            }
                            if (!canViewReceivePointerEvents(child)
                                    || !isTransformedTouchPointInView(x, y, child, null)) {
                                ev.setTargetAccessibilityFocus(false);
                                continue;
                            }
                            newTouchTarget = getTouchTarget(child);
                            if (newTouchTarget != null) {
                                // Child is already receiving touch within its bounds.
                                // Give it the new pointer in addition to the ones it is handling.
                                newTouchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }
                            resetCancelNextUpFlag(child);
                            if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                // Child wants to receive touch within its bounds.
                                mLastTouchDownTime = ev.getDownTime();
                                if (preorderedList != null) {
                                    // childIndex points into presorted list, find original index
                                    for (int j = 0; j < childrenCount; j++) {
                                        if (children[childIndex] == mChildren[j]) {
                                            mLastTouchDownIndex = j;
                                            break;
                                        }
                                    }
                                } else {
                                    mLastTouchDownIndex = childIndex;
                                }
                                mLastTouchDownX = ev.getX();
                                mLastTouchDownY = ev.getY();
                                
                                newTouchTarget = addTouchTarget(child, idBitsToAssign);
                                alreadyDispatchedToNewTouchTarget = true;
                                break;
                            }
                            // The accessibility focus didn't handle the event, so clear
                            // the flag and do a normal dispatch to all children.
                            ev.setTargetAccessibilityFocus(false);
                        }
 ...省略
}
```
> 先遍历ViewGroup的子元素，判断子元素是否能够接收到点击事件，如果子元素能够接收到则交由子元素来处理。接下来dispatchTransformedTouchEvent()
```
private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel,
           View child, int desiredPointerIdBits) {
       final boolean handled;
       // Canceling motions is a special case.  We don't need to perform any transformations
       // or filtering.  The important part is the action, not the contents.
       final int oldAction = event.getAction();
       if (cancel || oldAction == MotionEvent.ACTION_CANCEL) {
           event.setAction(MotionEvent.ACTION_CANCEL);
           
        // 重点在此，有儿子给给儿子干，没有自己干!!!
           if (child == null) {
               handled = dispatchTouchEvent(event);
           } else {
               handled = child.dispatchTouchEvent(event);
           }
           event.setAction(oldAction);
           return handled;
       }
 ...省略      
}
```
>事件有viewGroup传递给view后,如果OnTouchListener不为null并且onTouch()方法返回true，则表示事件被消费，就不会执行onTouchEvent(event)。如上文所述OnTouchListener的onTouch()优先于onTouchEvent(event)
```
public boolean dispatchTouchEvent(MotionEvent event) {
      ...省略
       boolean result = false;
       if (onFilterTouchEventForSecurity(event)) {
           //noinspection SimplifiableIfStatement
           ListenerInfo li = mListenerInfo;
           
          // 如果OnTouchListener不为null并且onTouch()方法返回true，则表示事件被消费
           if (li != null && li.mOnTouchListener != null
                   && (mViewFlags & ENABLED_MASK) == ENABLED
                   && li.mOnTouchListener.onTouch(this, event)) {
               result = true;
           }
          // 则不会执行onTouchEvent(event)。
           if (!result && onTouchEvent(event)) {
               result = true;
           }
       }
    ...省略
       return result;
   }
```
>查看view的onTouchEvent()方法的部分源码：
```
public boolean onTouchEvent(MotionEvent event) {
     ...省略
       final int action = event.getAction();
       //当view的CLICKABLE和LONG_CLICKABLE满足其一事件就会被消费
       if (((viewFlags & CLICKABLE) == CLICKABLE ||
               (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) ||
               (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE) {
           switch (action) {
               case MotionEvent.ACTION_UP:
                   boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                   if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                       // take focus if we don't have it already and we should in
                       // touch mode.
                       boolean focusTaken = false;
                      
                       if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                           // This is a tap, so remove the longpress check
                           removeLongPressCallback();
                           // Only perform take click actions if we were in the pressed state
                           if (!focusTaken) {
                               // Use a Runnable and post this rather than calling
                               // performClick directly. This lets other visual state
                               // of the view update before click actions start.
                               if (mPerformClick == null) {
                                   mPerformClick = new PerformClick();
                               }
                               if (!post(mPerformClick)) {
                               //ACTION_UP发生后会调用performClick(),从而回调onClickListner的onClick(),这就是为什么onClick的优先级低于onTouchEvent()。
                                   performClick();
                               }
                           }
                       }
      ...省略    
       }
       return true;
      }          
      return false;
   }
```
### 滑动冲突的解决
> - 外部拦截法
复写viewGroup的onInterceptTouchEvent()方法模板如下：
```
   @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
         //ACTION_DOWN必须返回false，如果父容器拦截此事件，那么后续动作都会由父容器处理（见上文），后面的都毫无意义。 
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:

                if (父容器要拦截此事件) {
                    intercepted=true;
                } else {
                    intercepted=false;
                }

                break;
            case MotionEvent.ACTION_UP:
            //必须返回false
                intercepted = false;
                break;
        }

        return intercepted;
    }
```

> - 内部拦截法
> 复写子View的dispatchTouchEvent(MotionEvent ev)方法模板如下：
```
 @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            要保证事件可以传递到这个view，因此在ACTION_DOWN时，不能使父视图拦截事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (父视图需要此事件) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.dispatchTouchEvent(ev);
    }
```

