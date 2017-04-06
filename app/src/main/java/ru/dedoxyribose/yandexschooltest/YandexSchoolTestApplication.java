package ru.dedoxyribose.yandexschooltest;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.yandex.speechkit.SpeechKit;

/**
 * Created by Ryan on 16.03.2017.
 */


@ReportsCrashes(
        formUri = "https://collector.tracepot.com/f5ffe174",
        logcatArguments = { "-t", "1500", "-v", "long", "time" }
)

public class YandexSchoolTestApplication extends Application {

    private static final String APP_TAG = "YSTAPP";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(APP_TAG, "Application start");
        if (ACRA.isACRASenderServiceProcess()) {

            Log.d(APP_TAG, "isACRA");
        }
        else {
            Singletone.init(getApplicationContext());
            RetrofitHelper.init();
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);

    }
}
