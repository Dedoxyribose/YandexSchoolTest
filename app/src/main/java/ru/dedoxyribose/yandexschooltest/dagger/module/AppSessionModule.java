package ru.dedoxyribose.yandexschooltest.dagger.module;

import android.content.Context;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.dedoxyribose.yandexschooltest.util.AppSession;

/**
 * Created by Ryan on 13.04.2017.
 */

@Module
public class AppSessionModule {

    private Context mContext;
    private boolean mMockMode;

    public AppSessionModule(Context context, boolean mockMode) {
        this.mContext = context.getApplicationContext();
        this.mMockMode=mockMode;
    }

    @Provides
    @Singleton
    AppSession provideAppSession() {
        if (mMockMode)
            return Mockito.mock(AppSession.class);
        else return new AppSession(mContext);
    }
}