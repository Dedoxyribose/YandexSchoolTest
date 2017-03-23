package ru.dedoxyribose.yandexschooltest;

import android.app.Application;

import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.yandex.speechkit.SpeechKit;

/**
 * Created by Ryan on 16.03.2017.
 */
public class YandexSchoolTestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Singletone.init(getApplicationContext());
        RetrofitHelper.init();
        SpeechKit.getInstance().configure(getApplicationContext(), getString(R.string.sdk_key));

    }
}
