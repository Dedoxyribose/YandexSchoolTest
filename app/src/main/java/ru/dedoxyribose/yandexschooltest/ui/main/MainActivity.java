package ru.dedoxyribose.yandexschooltest.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.IdlingResource;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.event.SelectRecordEvent;
import ru.dedoxyribose.yandexschooltest.ui.histories.HistoriesFragment;
import ru.dedoxyribose.yandexschooltest.ui.settings.SettingsFragment;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardActivity;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateFragment;
import ru.dedoxyribose.yandexschooltest.widget.NonSwipeableViewPager;
import ru.yandex.speechkit.SpeechKit;

public class MainActivity extends StandardActivity implements MainView {

    @InjectPresenter
    MainPresenter mPresenter;

    private NonSwipeableViewPager mViewPager;
    private TabLayout mTabLayout;

    private SectionsPagerAdapter mSectionPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechKit.getInstance().configure(getApplicationContext(), getString(R.string.sdk_key));

        mViewPager = (NonSwipeableViewPager) findViewById(R.id.vpPages);
        mTabLayout = (TabLayout) findViewById(R.id.tabPages);

        mSectionPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position!=1) {
                    //если средняя вкладка потеряла видимость, отправить сигнал, чтоб текущий фрагмент в ней обновил своё состояние
                    //(это касается удаления с экрана элементов избранного, с которых сняли "закладку слева")
                    if (mSectionPagerAdapter.getRegisteredFragment(1)!=null) {
                        ((HistoriesFragment)mSectionPagerAdapter.getRegisteredFragment(1)).focusLost();
                    }
                }

                for (int i=0; i<3; i++) {
                    ((ImageView)mTabLayout.getTabAt(i).getCustomView().findViewById(R.id.ivIcon)).setColorFilter(
                            ContextCompat.getColor(getActivity(), (i==position)?R.color.colorBlackText:R.color.colorGrayPic));
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ImageView iv1=new ImageView(getApplicationContext());
        iv1.setImageResource(R.drawable.ic_translate_black_24dp);
        iv1.setId(R.id.ivIcon);
        mTabLayout.getTabAt(0).setCustomView(iv1);

        ImageView iv2=new ImageView(getApplicationContext());
        iv2.setImageResource(R.drawable.ic_bookmark_black_24dp);
        iv2.setId(R.id.ivIcon);
        iv2.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGrayPic));
        mTabLayout.getTabAt(1).setCustomView(iv2);

        ImageView iv3=new ImageView(getApplicationContext());
        iv3.setImageResource(R.drawable.ic_settings_black_24dp);
        iv3.setId(R.id.ivIcon);
        iv3.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGrayPic));
        mTabLayout.getTabAt(2).setCustomView(iv3);

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
                    return SettingsFragment.newInstance();
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
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectRecordEvent(SelectRecordEvent selectRecordEvent) {

        //если пользователь тыкнул в элемент в истории/избр, перекидываем его на главную (первую) вкладку
        mViewPager.setCurrentItem(0);
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

    //обработка CountingIdlingResource, для тестов

    public void incrementIdlingResource() {
        mPresenter.incrementIdlingResource();
    }

    public void decrementIdlingResource() {
        mPresenter.decrementIdlingResource();
    }

    public IdlingResource getIdlingResource() {
        return mPresenter.getIdlingResource();
    }


}
