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

    public static Retrofit retrofit;
    private static ServerApi sServerApi;

    public static void init() {

        if (retrofit==null) {
            Gson gson = Singletone.getGson();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            retrofit=new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();

            sServerApi = retrofit.create(ServerApi.class);
        }
    }

    public static ServerApi getServerApi() {
        return sServerApi;
    }

}
