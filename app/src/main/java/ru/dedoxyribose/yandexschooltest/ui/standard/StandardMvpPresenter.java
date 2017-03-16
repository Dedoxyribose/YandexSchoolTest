package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.content.Context;
import android.content.res.Resources;

import com.arellomobile.mvp.MvpPresenter;

import ru.dedoxyribose.yandexschooltest.Singletone.Singletone;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;


/**
 * Created by Ryan on 20.02.2017.
 */
public class StandardMvpPresenter<MvpView extends com.arellomobile.mvp.MvpView> extends MvpPresenter<MvpView> {

    protected static String TAG;
    private DaoSession mDaoSession;

    public StandardMvpPresenter() {
        TAG=getClass().getName().substring(0,Math.min(getClass().getName().length(), 21));
        mDaoSession= Singletone.getDaoSession();
    }

    protected Resources getResources()
    {
        return Singletone.getInstance().getContext().getResources();
    }

    protected Context getContext()
    {
        return Singletone.getInstance().getContext();
    }

    protected DaoSession getDaoSession()
    {
        return mDaoSession;
    }
}
