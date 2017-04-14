package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.content.Context;
import android.content.res.Resources;

import com.arellomobile.mvp.MvpPresenter;

import ru.dedoxyribose.yandexschooltest.YandexSchoolTestApplication;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;
import ru.dedoxyribose.yandexschooltest.util.ServerApi;
import ru.dedoxyribose.yandexschooltest.util.Utils;


/**
 * Created by Ryan on 20.02.2017.
 */
public class StandardMvpPresenter<MvpView extends com.arellomobile.mvp.MvpView> extends MvpPresenter<MvpView> {

    protected static String APP_TAG= Utils.getAppTag();
    protected String TAG;
    private AppSession mAppSession;
    private ServerApi mServerApi;

    public StandardMvpPresenter() {
        TAG=this.toString();
        mAppSession = YandexSchoolTestApplication.getAppSessionComponent().getAppSession();
        mServerApi = YandexSchoolTestApplication.getAppSessionComponent().getServerApi();
    }

    protected Resources getResources()
    {
        return mAppSession.getContext().getResources();
    }

    protected Context getContext()
    {
        return mAppSession.getContext();
    }

    protected DaoSession getDaoSession()
    {
        return mAppSession.getDaoSession();
    }

    protected AppSession getAppSession() {
        return mAppSession;
    }

    protected ServerApi getServerApi() {
        return mServerApi;
    }
}
