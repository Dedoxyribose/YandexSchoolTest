package ru.dedoxyribose.yandexschooltest.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;

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

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Word.class, new Word.WordConverter())
                .registerTypeAdapter(Def.class, new Def.DefConverter())
                .registerTypeAdapter(Example.class, new Example.ExampleConverter())
                .registerTypeAdapter(Record.class, new Record.RecordConverter())
                .registerTypeAdapter(Translation.class, new Translation.TranslationConverter())
                .registerTypeAdapter(SupportedLangs.class, new SupportedLangs.SupportedLangsConverter())
                .create();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (mockMode) {
            mFakeInterceptor=new FakeInterceptor();
            httpClient.addInterceptor(mFakeInterceptor);
        } else httpClient.addInterceptor(logging);

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
