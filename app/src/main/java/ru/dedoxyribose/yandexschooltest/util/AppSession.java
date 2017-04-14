package ru.dedoxyribose.yandexschooltest.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.DaoMaster;
import ru.dedoxyribose.yandexschooltest.model.entity.DaoSession;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;

/**
 * Created by Ryan on 19.02.2017.
 */
public class AppSession {

    private static final String TAG = "AppSession";
    private Context mContext;
    private List<Lang> mLangs;
    private String mLastLangFrom;
    private String mLastLangTo;
    private boolean mSyncTranslation;
    private boolean mShowDict;
    private boolean mReturnTranslate;
    private String mLastText;
    private String mLocale;

    public AppSession(Context context){
        mContext=context.getApplicationContext();
        configureGreenDao(context);
        initGson();
        mLangs=getDaoSession().getLangDao().loadAll();
        Collections.sort(mLangs, new Comparator<Lang>() {
            @Override
            public int compare(Lang lang, Lang t1) {
                return lang.getName().compareTo(t1.getName());
            }
        });
        loadSettings();
    }

    private static Gson sGson;

    public Context getContext() {
        return mContext;
    }


    ///------------------DAO

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private Database mDatabase;


    public void configureGreenDao(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "green_db");
        mDatabase = helper.getWritableDb();
        mDaoMaster = new DaoMaster(mDatabase);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
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
                .registerTypeAdapter(SupportedLangs.class, new SupportedLangs.SupportedLangsConverter())
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

    //-------------------------Settings

    public void loadSettings() {
        Log.d(TAG, "loadSettings");
        SharedPreferences sPref = mContext.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        mLastLangFrom = sPref.getString("lastLangFrom", "ru");
        mLastLangTo = sPref.getString("lastLangTo", "en");
        mSyncTranslation = sPref.getBoolean("syncTranslation", true);
        mShowDict = sPref.getBoolean("showDict", true);
        mReturnTranslate = sPref.getBoolean("returnTranslate", true);
        mLastText = sPref.getString("lastText", "");
        mLocale = sPref.getString("locale", "");
    }

    public void saveSettings() {
        Log.d(TAG, "saveSettings");

        SharedPreferences sPref = mContext.getSharedPreferences("settings", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("lastLangFrom", mLastLangFrom);
        ed.putString("lastLangTo", mLastLangTo);
        ed.putBoolean("syncTranslation", mSyncTranslation);
        ed.putBoolean("showDict", mShowDict);
        ed.putBoolean("returnTranslate", mReturnTranslate);
        ed.putString("lastText", mLastText);
        ed.putString("locale", mLocale);
        ed.commit();
    }


    //--------------


    public String getLastLangFrom() {
        return mLastLangFrom;
    }

    public String getLastLangTo() {
        return mLastLangTo;
    }

    public void setLastLangFrom(String lastLangFrom) {
        this.mLastLangFrom = lastLangFrom;
    }

    public void setLastLangTo(String lastLangTo) {
        this.mLastLangTo = lastLangTo;
    }

    public boolean isSyncTranslation() {
        return mSyncTranslation;
    }

    public boolean isShowDict() {
        return mShowDict;
    }

    public boolean isReturnTranslate() {
        return mReturnTranslate;
    }

    public void setSyncTranslation(boolean syncTranslation) {
        this.mSyncTranslation = syncTranslation;
    }

    public void setShowDict(boolean showDict) {
        this.mShowDict = showDict;
    }

    public void setReturnTranslate(boolean returnTranslate) {
        this.mReturnTranslate = returnTranslate;
    }

    public String getLastText() {
        return mLastText;
    }

    public void setLastText(String lastText) {
        this.mLastText = lastText;
    }

    public String getLocale() {
        return mLocale;
    }

    public void setLocale(String locale) {
        this.mLocale = locale;
    }
}
