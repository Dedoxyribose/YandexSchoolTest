package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.content.Context;
import android.content.res.Resources;

import com.arellomobile.mvp.MvpPresenter;

import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;
import ru.dedoxyribose.yandexschooltest.util.Utils;


/**
 * Created by Ryan on 20.02.2017.
 */
public class StandardMvpPresenter<MvpView extends com.arellomobile.mvp.MvpView> extends MvpPresenter<MvpView> {

    protected static String APP_TAG= Utils.getAppTag();
    protected String TAG;
    private DaoSession mDaoSession;

    public StandardMvpPresenter() {
        TAG=this.toString();
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
