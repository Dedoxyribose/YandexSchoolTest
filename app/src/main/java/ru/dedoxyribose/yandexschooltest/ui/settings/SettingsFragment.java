package ru.dedoxyribose.yandexschooltest.ui.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SwitchCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.ui.about.AboutActivity;
import ru.dedoxyribose.yandexschooltest.ui.recordlist.RecordListFragment;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardFragment;
import ru.dedoxyribose.yandexschooltest.util.Singletone;


public class SettingsFragment extends StandardFragment  {

    private SwitchCompat msSync;
    private SwitchCompat msDict;
    private SwitchCompat msReturn;

    private TextView mTvAbout;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
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

        mTvAbout=(TextView) view.findViewById(R.id.tvAbout);

        msSync=(SwitchCompat) view.findViewById(R.id.sSync);
        msDict=(SwitchCompat) view.findViewById(R.id.sDict);
        msReturn=(SwitchCompat) view.findViewById(R.id.sReturn);

        msSync.setChecked(Singletone.getInstance().isSyncTranslation());
        msDict.setChecked(Singletone.getInstance().isShowDict());
        msReturn.setChecked(Singletone.getInstance().isReturnTranslate());

        msSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Singletone.getInstance().setSyncTranslation(b);
                Singletone.getInstance().saveSettings();
            }
        });

        msDict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Singletone.getInstance().setShowDict(b);
                Singletone.getInstance().saveSettings();
            }
        });

        msReturn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Singletone.getInstance().setReturnTranslate(b);
                Singletone.getInstance().saveSettings();
            }
        });

        mTvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });

    }


}

