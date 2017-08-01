package com.mitbbs.main1.mitbbs.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mitbbs.main1.R;
import com.mitbbs.main1.mitbbs.view.SwipeBackLayout;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jc on 2017/8/1.
 */
public abstract class BaseActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener,SwipeBackLayout.SwipeBackListener{
    protected String TAG = "BaseActivity";
    protected Context context;
    protected HashMap<String,Activity> activityHashMap;

    protected Toolbar toolbar;
    protected boolean pause;
    protected boolean lock;
    private boolean canSwipeBack = true;  //是否可以右滑返回，默认可以
    //右滑返回
    private SwipeBackLayout swipeBackLayout;
    private ImageView ivShadow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        context = this;
        addActivity(getClass().getSimpleName(),this);

        setContentView(attachLayout());
        bindViews();

    }

    /**
     *  关联布局
     * @return layout
     */
    protected abstract int attachLayout();

    /**
     * 初始化控件
     */
    protected abstract void bindViews();

    /**
     * 每个activity 都加入到map中进行管理
     * @param name
     * @param activity
     */
    protected void addActivity(String name,Activity activity){
        if (activityHashMap == null){
            activityHashMap = new HashMap<>();
        }
        activityHashMap.put(name,activity);
    }

    public void fiishActivityByName(String name){
        Activity temp = activityHashMap.get(name);
        if (temp != null && !temp.isFinishing()){
            temp.finish();
        }else {
            Log.e("BaseActivity" , name + "不存在");
        }
    }

    public void finishAllActivity(){
        for (Activity a :activityHashMap.values()){
            if (a != null && !a.isFinishing()){
                a.finish();
            }else {
                Log.e("BaseActivity" , a.getClass().getSimpleName() + "不存在");
            }
        }
    }



    /**
     * 初始化标题栏
     * @param title
     */
    protected void initToolbar(String title){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);

        if (toolbar == null || toolbar_title == null)return;
        toolbar.setTitle("");
        toolbar_title.setText(title);
        toolbar_title.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        ActionBar actioBar = getSupportActionBar();
        if (actioBar == null)return;
        actioBar.setDisplayHomeAsUpEnabled(true);
        Drawable drawable = ContextCompat.getDrawable(this,R.drawable.ic_arrow_back_white_24px);
        getSupportActionBar().setHomeAsUpIndicator(drawable);

        toolbar.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onResume() {
        super.onResume();
        pause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause = true;
    }


    public boolean lock(){
        Log.e(TAG,"lock = " + lock + " pause = " + pause);
        if (lock || pause)return true;
        else {
            lock = true;
            return false;
        }
    }

    public boolean isLock(){
        return lock || pause;
    }

    public void setLock(boolean lock){
        this.lock = lock;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }


    /**
     * 返回该界面是否可以右滑返回
     */
    private boolean canSwipeBack(){
        return canSwipeBack;
    }

    protected void startActivity(Class clazz){
        Intent intent = new Intent(context,clazz);
        startActivity(intent);
    }

    protected void startActivity(Class clazz, Object...objects){
        if (this.lock())return;

        if (objects.length%2 != 0){
            Log.e("BaseActivity","参数个数出错");
            return;
        }

        Intent intent = new Intent(context,clazz);
        for (int i =0;i<objects.length;i+=2){
            if (objects[i] instanceof String && objects[i+1] instanceof Serializable){
                intent.putExtra((String)objects[i],(Serializable)objects[i+1]);
            }else {
                Log.e("BaseActivity","参数类型出错");
                return;
            }
        }
        startActivity(intent);
        setLock(false);
    }

    protected void startActivityForResult(Class<?> clazz , int requestCode){
        Intent intent = new Intent(context,clazz);
        startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 先于setContentView调用，不然默认是可以右滑的
     * @param canSwipback
     */
    protected void setCanSwipeBack(boolean canSwipback){
        this.canSwipeBack = canSwipback;
    }

    protected void logE(String s){
        Log.e(TAG,": " +s);
    }
    protected void  logI(String s){
        Log.i(TAG,": "+s);
    }


    /****************************右滑返回***********************************/

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        if (!canSwipeBack()){
            super.setContentView(layoutResID);
            return;
        }

        View view;

        super.setContentView(getContainer());
    }

    private View getContainer() {
        RelativeLayout container = new RelativeLayout(this);
        swipeBackLayout = new SwipeBackLayout(this);
        swipeBackLayout.setOnSwipeBackListener(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            ivShadow = new ImageView(this);
            ivShadow.setBackgroundColor(0x90000000);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            );
            container.addView(ivShadow,params);
        }

        container.addView(swipeBackLayout);
        return container;
    }
}
