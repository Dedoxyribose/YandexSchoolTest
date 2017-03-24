package ru.dedoxyribose.yandexschooltest.ui.histories;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.ui.recordlist.RecordListFragment;
import ru.dedoxyribose.yandexschooltest.ui.recordlist.RecordListPresenter;
import ru.dedoxyribose.yandexschooltest.ui.recordlist.RecordListView;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardFragment;


public class HistoriesFragment extends StandardFragment  {

    private TabLayout mTabLayout;
    private ImageView mIvClear;
    private ViewPager mViewPager;

    private SectionsPagerAdapter mPagerAdapter;

    public HistoriesFragment() {
        // Required empty public constructor
    }


    public static HistoriesFragment newInstance() {
        HistoriesFragment fragment = new HistoriesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_histories, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*if (activity instanceof StartActivityFragmentInteractionListener) {
            mListener = (StartActivityFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement StartActivityFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTabLayout=(TabLayout) view.findViewById(R.id.tabLayout);
        mIvClear=(ImageView) view.findViewById(R.id.ivClear);
        mViewPager=(ViewPager) view.findViewById(R.id.viewPager);


        mPagerAdapter=new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int i=(position==0)?1:0;

                if (mPagerAdapter.getRegisteredFragment(i)!=null) {
                    ((RecordListFragment)mPagerAdapter.getRegisteredFragment(i)).update();
                }

                updateClearButtonState();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.setTabTextColors(ContextCompat.getColor(getActivity(), R.color.colorDarkGray),
                ContextCompat.getColor(getActivity(), R.color.colorBlackText));

        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.colorBlackText));

        mIvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())!=null) {
                    ((RecordListFragment)mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).clearClicked();
                }
            }
        });

    }

    public void focusLost() {
        if (mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())!=null) {
            ((RecordListFragment)mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).update();
        }
    }

    public void updateClearButtonState() {
        if (mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())!=null) {

            mIvClear.setVisibility(
                    ((RecordListFragment)mPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).getClearButtonState()?
                            View.VISIBLE:View.GONE);
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
                    return RecordListFragment.newInstance(RecordListFragment.TYPE_FAVORITE);
                case 1:
                    return RecordListFragment.newInstance(RecordListFragment.TYPE_HISTORY);
            }

            return null;

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

            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.Favorite);
                case 1:
                    return getString(R.string.History);
            }
            return null;
        }

        public Fragment getRegisteredFragment(int i) {
            return registeredFragments.get(i);
        }


    }


}

