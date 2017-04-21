package ru.dedoxyribose.yandexschooltest;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import ru.dedoxyribose.yandexschooltest.dagger.component.AppSessionComponent;
import ru.dedoxyribose.yandexschooltest.dagger.component.DaggerAppSessionComponent;
import ru.dedoxyribose.yandexschooltest.dagger.module.AppSessionModule;
import ru.dedoxyribose.yandexschooltest.dagger.module.ServerApiModule;

/**
 * Created by Ryan on 16.03.2017.
 */


@ReportsCrashes(
        formUri = "https://collector.tracepot.com/f5ffe174",
        logcatArguments = { "-t", "1500", "-v", "long", "time" }
)

public class YandexSchoolTestApplication extends Application {

    private static final String APP_TAG = "YSTAPP";

    private static AppSessionComponent sAppSessionComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(APP_TAG, "Application start");

        if (ACRA.isACRASenderServiceProcess()) {
            Log.d(APP_TAG, "isACRA");
        }
        else {
            buildComponent(false, getApplicationContext());
            sAppSessionComponent.getAppSession();
        }

    }

    /**
     * создать компонент для даггера
     * @param mockMode
     * @param context
     */
    public static void buildComponent(boolean mockMode, Context context) {
        sAppSessionComponent = DaggerAppSessionComponent.builder()
                .appSessionModule(new AppSessionModule(context, mockMode))
                .serverApiModule(new ServerApiModule(mockMode))
                .build();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);

    }

    /**
     * получить компонент даггера
     * @return
     */
    public static AppSessionComponent getAppSessionComponent() {
        return sAppSessionComponent;
    }
}
