package ru.dedoxyribose.yandexschooltest.dagger.module;

import android.content.Context;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.ServerApi;

/**
 * Created by Ryan on 13.04.2017.
 */

//Возвращает Ретрофит, объект Серверного АПИ
@Module
public class ServerApiModule {

    private boolean mMockMode;

    public ServerApiModule(boolean mockMode) {
        this.mMockMode=mockMode;
    }

    @Provides
    @Singleton
    ServerApi provideServerApi(RetrofitHelper retrofitHelper) {
        return retrofitHelper.getServerApi();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(RetrofitHelper retrofitHelper) {
        return retrofitHelper.getRetrofit();
    }

    @Provides
    @Singleton
    RetrofitHelper provideRetrofitHepler() {
        return new RetrofitHelper(mMockMode);
    }
}
