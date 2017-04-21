package ru.dedoxyribose.yandexschooltest.ui.standard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.arellomobile.mvp.MvpAppCompatActivity;

import ru.dedoxyribose.yandexschooltest.YandexSchoolTestApplication;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;

/**
 * Created by Ryan on 26.08.2016.
 */

/**
 * Базовое активити, получающее реализацию зависимостей на все необходимые ресурсы
 */
public class StandardActivity extends MvpAppCompatActivity {


    private AppSession mAppSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppSession = YandexSchoolTestApplication.getAppSessionComponent().getAppSession();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public AppCompatActivity getActivity() {
        return this;
    }

    protected DaoSession getDaoSession()
    {
        return mAppSession.getDaoSession();
    }

    protected AppSession getAppSession() {
        return mAppSession;
    }

}
