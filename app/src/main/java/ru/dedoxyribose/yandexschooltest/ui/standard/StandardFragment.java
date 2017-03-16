package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpFragment;

import java.util.ArrayList;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.Singletone.Singletone;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;

/**
 * Created by Ryan on 14.10.2016.
 */
public class StandardFragment extends MvpFragment
{

    protected String TAG="";

    protected DaoSession mDaoSession;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDaoSession= Singletone.getDaoSession();

        TAG=this.toString();
        if (TAG.length()>22) TAG=TAG.substring(0, 22);


        Log.d(TAG, "onViewCreated()");
        Log.d(TAG, "savedInstanceState="+savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, "onDetach()");
    }


    protected DaoSession getDaoSession()
    {
        return mDaoSession;
    }
}
