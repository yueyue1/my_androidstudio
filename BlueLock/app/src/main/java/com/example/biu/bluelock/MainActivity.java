package com.example.biu.bluelock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private LinearLayout mTabLock;
    private LinearLayout mTabFrd;
    private LinearLayout mTabSetting;

    private Fragment mTab01;
    private Fragment mTab02;
    private Fragment mTab03;

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mfragments;

    private ImageButton mImgLock;
    private ImageButton mImgFrd;
    private ImageButton mImgSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvents();
        setSelect(0);
    }

    
    private void initEvents() {
        mTabSetting.setOnClickListener(this);
        mTabLock.setOnClickListener(this);
        mTabFrd.setOnClickListener(this);

    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLock= (LinearLayout) findViewById(R.id.Lock);
        mTabFrd= (LinearLayout) findViewById(R.id.Frd);
        mTabSetting= (LinearLayout) findViewById(R.id.Setting);
        mImgLock= (ImageButton) findViewById(R.id.btnLock);
        mImgFrd= (ImageButton) findViewById(R.id.btnFrd);
        mImgSetting= (ImageButton) findViewById(R.id.btnSet);
        mfragments=new ArrayList<>();
        mTab01=new LockFragment();
        mTab02=new FrdFregment();
        mTab03=new SettingFragment();
        mfragments.add(mTab01);
        mfragments.add(mTab02);
        mfragments.add(mTab03);
        mAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mfragments.get(position);
            }

            @Override
            public int getCount() {
                return mfragments.size();
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true,new DepthPageTransformer());
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int i = 0;
                i++;
            }

            @Override
            public void onPageSelected(int position) {
                int current=mViewPager.getCurrentItem();
                setSelect(current);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private void setSelect(int i){
        resetImag();
        switch (i){
            case 0:
                mImgLock.setImageResource(R.mipmap.tab_lock_select);
                break;
            case 1:
                mImgFrd.setImageResource(R.mipmap.tab_friend_select);
                break;
            case 2:
                mImgSetting.setImageResource(R.mipmap.tab_setting_select);
                break;
        }
        mViewPager.setCurrentItem(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Lock:
                setSelect(0);
                break;
            case R.id.Frd:
                setSelect(1);
                break;
            case R.id.Setting:
                setSelect(2);
                break;

        }

    }

    private void resetImag() {
        mImgLock.setImageResource(R.mipmap.tab_lock_normal);
        mImgFrd.setImageResource(R.mipmap.tab_friend_normal);
        mImgSetting.setImageResource(R.mipmap.tab_setting_normal);

    }
}
