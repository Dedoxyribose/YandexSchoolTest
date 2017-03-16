package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.arellomobile.mvp.MvpAppCompatActivity;

import ru.dedoxyribose.yandexschooltest.util.Singletone;
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
