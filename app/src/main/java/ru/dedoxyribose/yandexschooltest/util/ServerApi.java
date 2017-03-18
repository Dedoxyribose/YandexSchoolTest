package ru.dedoxyribose.yandexschooltest.util;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;

import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;

/**
 * Created by Ryan on 07.01.2017.
 */
public interface ServerApi {

    @GET(RetrofitHelper.DICTIONARY_URL+"lookup")
    Call<Record> lookup(@Query("key") String key, @Query("lang") String direction, @Query("text") String text, @Query("ui") String ui);

    @FormUrlEncoded
    @POST(RetrofitHelper.TRANSLATE_URL+"translate")
    Call<Record> translate(@Field("key") String key, @Field("lang") String direction, @Field("text") String text);

    @FormUrlEncoded
    @POST(RetrofitHelper.TRANSLATE_URL+"getLangs")
    Call<SupportedLangs> getLangs(@Field("key") String key, @Field("ui") String ui);

    @GET(RetrofitHelper.TRANSLATE_URL+"detect")
    Call<ResponseBody> detect(@Query("key") String key, @Query("text") String text);

}
