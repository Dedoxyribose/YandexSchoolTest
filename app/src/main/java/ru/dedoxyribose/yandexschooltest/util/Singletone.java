package ru.dedoxyribose.yandexschooltest.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;

import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.DaoMaster;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;

/**
 * Created by Ryan on 19.02.2017.
 */
public class Singletone {

    private static final String TAG = "Singletone";
    private static Singletone sInstance;
    private Context mContext;
    private List<Lang> mLangs;

    private Singletone(Context context){

    }

    public static void init(Context context) {
        if (sInstance==null)
        {
            sInstance=new Singletone(context);
            sInstance.mContext=context.getApplicationContext();
            configureGreenDao(context);
            initGson();
        }
    }

    public static Singletone getInstance() {
        return sInstance;
    }

    private static Gson sGson;

    public Context getContext() {
        return mContext;
    }


    ///------------------DAO

    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;
    private static Database mDatabase;


    public static void configureGreenDao(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "green_db");
        mDatabase = helper.getWritableDb();
        mDaoMaster = new DaoMaster(mDatabase);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }


    //-------------------------Gson


    public static void initGson() {
        sGson= new GsonBuilder()
                .registerTypeAdapter(Word.class, new Word.WordConverter())
                .registerTypeAdapter(Def.class, new Def.DefConverter())
                .registerTypeAdapter(Example.class, new Example.ExampleConverter())
                .registerTypeAdapter(Record.class, new Record.RecordConverter())
                .registerTypeAdapter(Translation.class, new Translation.TranslationConverter())
                .create();
    }

    public static Gson getGson() {
        return sGson;
    }


    //-------------------------Langs

    public List<Lang> getLangs() {
        return mLangs;
    }

    public void setLangs(List<Lang> langs) {
        this.mLangs = langs;
    }
}
