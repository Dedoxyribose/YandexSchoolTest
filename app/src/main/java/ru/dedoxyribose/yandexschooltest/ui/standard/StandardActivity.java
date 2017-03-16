package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.Singletone.Singletone;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;

/**
 * Created by Ryan on 26.08.2016.
 */
public class StandardActivity extends MvpAppCompatActivity {


    public boolean mIsAlive=false;

    protected DaoSession mDaoSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsAlive=true;

        mDaoSession= Singletone.getDaoSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIsAlive=false;
    }

    public AppCompatActivity getActivity()
    {
        return this;
    }

    protected DaoSession getDaoSession()
    {
        return mDaoSession;
    }

}
