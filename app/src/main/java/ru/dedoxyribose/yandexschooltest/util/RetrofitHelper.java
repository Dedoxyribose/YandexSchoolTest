package ru.dedoxyribose.yandexschooltest.util;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ryan on 07.01.2017.
 */
public class RetrofitHelper {

    public static final String DICTIONARY_URL = "https://dictionary.yandex.net/api/v1/dicservice.json/";
    public static final String TRANSLATE_URL = "https://translate.yandex.net/api/v1.5/tr.json/";

    public static final String API_URL = DICTIONARY_URL;

    private Retrofit mRetrofit;
    private ServerApi mServerApi;
    FakeInterceptor mFakeInterceptor;

    public RetrofitHelper(boolean mockMode) {

        Gson gson = AppSession.getGson();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        if (mockMode) {
            mFakeInterceptor=new FakeInterceptor();
            httpClient.addInterceptor(mFakeInterceptor);
        }

        mRetrofit=new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        mServerApi = mRetrofit.create(ServerApi.class);

    }

    public Retrofit getRetrofit() {
        return mRetrofit;

    }

    public ServerApi getServerApi() {
        return mServerApi;
    }

    public FakeInterceptor getFakeInterceptor() {return mFakeInterceptor;}

}
