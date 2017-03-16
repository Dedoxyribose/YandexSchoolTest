package ru.dedoxyribose.yandexschooltest;

import android.app.Application;

import ru.dedoxyribose.yandexschooltest.Singletone.Singletone;

/**
 * Created by Ryan on 16.03.2017.
 */
public class YandexSchoolTestApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();


        Singletone.init(getApplicationContext());
    }
}
