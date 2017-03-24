package ru.dedoxyribose.yandexschooltest.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.event.SelectRecordEvent;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.ui.histories.HistoriesFragment;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardActivity;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateFragment;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.dedoxyribose.yandexschooltest.util.Utils;
import ru.dedoxyribose.yandexschooltest.widget.NonSwipeableViewPager;

public class MainActivity extends StandardActivity implements MainView {

    @InjectPresenter
    MainPresenter mPresenter;

    private NonSwipeableViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;

    private SectionsPagerAdapter mSectionPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (NonSwipeableViewPager) findViewById(R.id.vpPages);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bnBar);

        mSectionPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_search:
                                mViewPager.setCurrentItem(0);
                                break;
                            case R.id.action_history:
                                mViewPager.setCurrentItem(1);
                                break;
                            case R.id.action_settings:
                                mViewPager.setCurrentItem(2);
                                break;
                        }
                        return true;
                    }
                });

        try{
            removeTextLabel(mBottomNavigationView, R.id.action_search);
            removeTextLabel(mBottomNavigationView, R.id.action_history);
            removeTextLabel(mBottomNavigationView, R.id.action_settings);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position!=1) {
                    if (mSectionPagerAdapter.getRegisteredFragment(1)!=null) {
                        ((HistoriesFragment)mSectionPagerAdapter.getRegisteredFragment(1)).focusLost();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private static void removeTextLabel(@NonNull BottomNavigationView bottomNavigationView, @IdRes int menuItemId) {
        View view = bottomNavigationView.findViewById(menuItemId);
        if (view == null) return;
        if (view instanceof MenuView.ItemView) {
            ViewGroup viewGroup = (ViewGroup) view;
            int padding = 0;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                if (v instanceof ViewGroup) {
                    padding = v.getHeight();
                    viewGroup.removeViewAt(i);
                }
            }
            viewGroup.setPadding(view.getPaddingLeft(), (viewGroup.getPaddingTop() + padding) / 2, view.getPaddingRight(), view.getPaddingBottom());
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return TranslateFragment.newInstance();
                case 1:
                    return HistoriesFragment.newInstance();
                default:
                    return TranslateFragment.newInstance();
            }

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Fragment fragment = (Fragment)super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        @Override
        public int getCount() {

            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        public Fragment getRegisteredFragment(int i) {
            return registeredFragments.get(i);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d("aa", "onBackPressed");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectRecordEvent(SelectRecordEvent selectRecordEvent) {

        mViewPager.setCurrentItem(0);

        Menu menu = mBottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            item.setChecked(i==1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
