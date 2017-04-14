package ru.dedoxyribose.yandexschooltest.dagger.component;

import javax.inject.Singleton;

import dagger.Component;
import retrofit2.Retrofit;
import ru.dedoxyribose.yandexschooltest.dagger.module.AppSessionModule;
import ru.dedoxyribose.yandexschooltest.dagger.module.ServerApiModule;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.ServerApi;

/**
 * Created by Ryan on 13.04.2017.
 */

@Singleton
@Component(modules={AppSessionModule.class, ServerApiModule.class})
public interface AppSessionComponent {
    AppSession getAppSession();
    ServerApi getServerApi();
    Retrofit getRetrofit();
    RetrofitHelper getRetrofitHelper();
}
