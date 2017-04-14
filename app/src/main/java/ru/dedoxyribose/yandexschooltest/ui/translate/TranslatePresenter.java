package ru.dedoxyribose.yandexschooltest.ui.translate;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.arellomobile.mvp.InjectViewState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.event.FullReloadNeededEvent;
import ru.dedoxyribose.yandexschooltest.event.RecordChangedEvent;
import ru.dedoxyribose.yandexschooltest.event.SelectRecordEvent;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.RecordDao;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.ui.chooselang.ChooseLangActivity;
import ru.dedoxyribose.yandexschooltest.ui.fullscreen.FullscreenActivity;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.Utils;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;
import ru.yandex.speechkit.gui.RecognizerActivity;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class TranslatePresenter extends StandardMvpPresenter<TranslateView>{

    public static final long INPUT_TIMEOUT=500;
    private static final long VOCALIZER_TIMEOUT = 20000;

    private volatile Record mCurRecord;
    private String mCurText="";

    private boolean mGotDictionaryResponse;
    private boolean mGotTranslationResponse;
    private boolean mLoading=false;

    private Call mCurDictionaryCall;
    private Call mCurTranslationCall;
    private Call mCurLangCall;

    private Record mDictionaryRecord;
    private Record mTranslationRecord;

    private int mRequestNum=0;

    private long mLastChangeTextTime=0;
    private Thread mWaiter;

    private Lang mLangFrom;
    private Lang mLangTo;
    private boolean mWasDetermined=false;

    private boolean mTextSpeechProgress=false;
    private boolean mTranslateSpeechProgress=false;

    private Vocalizer mTextVocalizer;
    private Vocalizer mTranslateVocalizer;


    public TranslatePresenter() {

        getViewState().setTranslationButtonsEnabled(false);

        mWaiter=new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        Thread.sleep(200);

                        if (mLastChangeTextTime!=0 && System.currentTimeMillis()-INPUT_TIMEOUT>=mLastChangeTextTime
                                && getAppSession().isSyncTranslation()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCurRecord==null || mCurRecord.getText()==null || !mCurRecord.getText().equals(mCurText)) {
                                        Log.d(APP_TAG, TAG+"mCurRecord="+mCurRecord);
                                        if (mCurRecord!=null) Log.d(APP_TAG, TAG+"mCurRecord.text="+mCurRecord.getText());
                                        if (mCurRecord!=null) Log.d(APP_TAG, TAG+"mCurText="+mCurText);
                                        Log.d(APP_TAG, TAG+"not final call, on timer");
                                        makeCall(false);
                                    }

                                }
                            });
                        }
                    }
                }
                catch (InterruptedException e){}

            }
        });
        mWaiter.start();

        mLangFrom=Utils.getLangByCode(getAppSession().getLastLangFrom(), getAppSession().getLangs());
        mLangTo=Utils.getLangByCode(getAppSession().getLastLangTo(), getAppSession().getLangs());

        if (mLangTo==null) {
            mLangTo= getAppSession().getLangs().get(0);
        }

        if (mLangFrom==null) mLangFrom=Utils.getLangByCode(mLangTo.getCode().equals("ru")?"en":"ru", getAppSession().getLangs());

        if (mLangTo==null || mLangFrom==null || getAppSession().getLangs().size()<3) {
            getViewState().toFailActivity();
            return;
        }

        if (mLangFrom.getAskedTime()==0) {
            mLangFrom.setAskedTime(System.currentTimeMillis());
            getDaoSession().getLangDao().insertOrReplace(mLangFrom);
        }

        if (mLangTo.getAskedTime()==0) {
            mLangTo.setAskedTime(System.currentTimeMillis());
            getDaoSession().getLangDao().insertOrReplace(mLangTo);
        }

        mCurText= getAppSession().getLastText();
        if (!Utils.isEmpty(mCurText)) {
            getViewState().setText(mCurText);

            List<Record> lastRecord=getDaoSession().getRecordDao().queryBuilder().where(RecordDao.Properties.InHistory.eq(true))
                    .orderDesc(RecordDao.Properties.HistoryTime).limit(1).list();

            if (lastRecord.size()>0 && lastRecord.get(0).getLowText().equals(mCurText.toLowerCase())
                    && lastRecord.get(0).getDirection().equals(mLangFrom+"-"+mLangTo)) {
                mCurRecord = lastRecord.get(0).copy();
                getViewState().setDefData(Utils.generateViewModelList(mCurRecord));
                getViewState().setTranslationButtonsEnabled(!Utils.isEmpty(mCurRecord.getTranslation()));
                getViewState().setMainText(mCurRecord.getTranslation());

            }
            else makeFinalCall();
        }

        showLangs();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        Log.d(APP_TAG, TAG+" onFirstViewAttach");



    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(APP_TAG, TAG+" onDestroy");

        if (mWaiter!=null && mWaiter.isAlive()) mWaiter.interrupt();

        if (mTextVocalizer!=null) mTextVocalizer.cancel();
        if (mTranslateVocalizer!=null) mTranslateVocalizer.cancel();

        EventBus.getDefault().unregister(this);

        getAppSession().setLastText(mCurText);
        getAppSession().saveSettings();
    }

    @Override
    public void attachView(TranslateView view) {
        super.attachView(view);

        Log.d(APP_TAG, TAG+" attachView");

    }

    public void clearClicked() {
        getViewState().setText("");
        mRequestNum++;
        getViewState().showLoading(false);

        if (!getAppSession().isSyncTranslation()) {
            mCurRecord=null;
            getViewState().setDefData(new ArrayList<ListItem>());
            getViewState().setMainText("");
        }

    }

    public void textChanged(CharSequence charSequence) {

        Log.d(APP_TAG, TAG+"textChanged()");

        mLastChangeTextTime=System.currentTimeMillis();

        mCurText=charSequence.toString();

        getViewState().showError(false, null, null, false);

        updateSpeechButtonStates();

        showLangs();
    }

    public void returnPressed() {

        Log.d(APP_TAG, TAG+"returnPressed");

        if (getAppSession().isReturnTranslate()) {
            if (mCurText.length()>0) {

                makeFinalCall();
                getViewState().hideSoftKeyboard();
                getViewState().clearTextFocus();
            }
        }
        else {
            mCurText=mCurText+"\n";
            getViewState().setText(mCurText);
        }

    }

    private void doMakeCall(final boolean finalCall) {

        Log.d(APP_TAG, TAG+"doMakeCall()");

        setLoading(true);

        mCurRecord=null;
        mDictionaryRecord=null;
        mTranslationRecord=null;

        final int curReqNum=mRequestNum;

        final String direction=mLangFrom.getCode()+"-"+mLangTo.getCode();

        final boolean noDict=!getAppSession().isShowDict();

        if (mCurDictionaryCall!=null) {
            mCurDictionaryCall.cancel();
            mCurDictionaryCall=null;
        }

        if (mCurTranslationCall!=null) {
            mCurTranslationCall.cancel();
            mCurTranslationCall=null;
        }

        String ui="ru";
        if (!Locale.getDefault().getLanguage().equals("ru")) ui="en";

        if (!noDict) {

            mCurDictionaryCall=getServerApi().lookup(getContext().getString(R.string.dict_key),
                    direction, mCurText, ui);
            mCurDictionaryCall.enqueue(
                    new Callback<Record>() {
                        @Override
                        public void onResponse(Call<Record> call, Response<Record> response) {
                            gotResponse(curReqNum, finalCall,  true, response.isSuccessful(), Utils.extractErrorCode(response),
                                    response.isSuccessful()?response.body():null, direction, false);
                        }

                        @Override
                        public void onFailure(Call<Record> call, Throwable t) {
                            gotResponse(curReqNum, finalCall, true, false, 0, null, direction, false);
                            if (t!=null) t.printStackTrace();
                        }
                    });
        }

        mCurTranslationCall=getServerApi().translate(getContext().getString(R.string.trans_key),
                direction, mCurText);
        mCurTranslationCall.enqueue(
                new Callback<Record>() {
                    @Override
                    public void onResponse(Call<Record> call, Response<Record> response) {
                        gotResponse(curReqNum, finalCall, false, response.isSuccessful(), Utils.extractErrorCode(response),
                                response.isSuccessful()?response.body():null, direction, noDict);
                    }

                    @Override
                    public void onFailure(Call<Record> call, Throwable t) {
                        gotResponse(curReqNum, finalCall, false, false, 0, null, direction, noDict);
                        if (t!=null) t.printStackTrace();
                    }
                });

    }

    private void makeFinalCall() {

        Log.d(APP_TAG, TAG+"makeFinalCall()");

        if (mCurRecord==null || mCurRecord.getText()==null || !mCurRecord.getText().equals(mCurText)
                || (mCurRecord.getTranslation().trim().toLowerCase().equals(mCurRecord.getText().trim().toLowerCase()))
                || mWasDetermined)
            makeCall(true);
        else if (mCurRecord!=null && mCurRecord.getText()!=null && mCurRecord.getText().equals(mCurText)) {
            mWasDetermined=false;
            if (!mLoading) saveCurRecord();
        }

    }

    private void saveCurRecord() {

        Log.d(APP_TAG, TAG+"saveCurRecord");

        if (mCurRecord==null) return;

        Record record=getDaoSession().getRecordDao().load(mCurRecord.getId());

        if (record!=null)
            mCurRecord=record.copy();

        mCurRecord.setHistoryTime(System.currentTimeMillis());
        mCurRecord.setInHistory(true);
        getDaoSession().getRecordDao().insertOrReplace(mCurRecord);

        EventBus.getDefault().post(new RecordChangedEvent(mCurRecord, RecordChangedEvent.SENDER_TRANSLATION));
    }

    private void makeCall(final boolean finalCall) {

        Log.d(APP_TAG, TAG+"makeCall(), mCurText="+mCurText);

        if (mCurText.length()==0) {
            mCurRecord=null;
            getViewState().setDefData(new ArrayList<ListItem>());
            getViewState().setMainText(null);
            updateFavoriteButton();
            mLastChangeTextTime=0;
            getViewState().setTranslateSpeechStatus(false, mTranslateSpeechProgress);
            getViewState().setTranslationButtonsEnabled(false);
            return;
        }

        mLastChangeTextTime=0;

        mRequestNum++;

        mGotDictionaryResponse=false;
        mGotTranslationResponse=false;

        setLoading(true);
        getViewState().showError(false, null, null, false);

        final int curReqNum=mRequestNum;

        if (mCurLangCall!=null) {
            mCurLangCall.cancel();
            mCurLangCall=null;
        }

        if (!mWasDetermined) {
            doMakeCall(finalCall);
            showLangs();
        }
        else {

            mCurLangCall=getServerApi().detect(getContext().getString(R.string.trans_key), mCurText);
            mCurLangCall.enqueue(
                    new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (curReqNum!=mRequestNum) return;

                            if (!response.isSuccessful()) {
                                getViewState().showLoading(false);
                                getViewState().showError(true, getContext().getString(R.string.Error),
                                        Utils.getErrorTextForCode(getContext(), Utils.extractErrorCode(response)), true);
                            }
                            else {

                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    Log.d(APP_TAG, TAG+jsonObject.toString());
                                    if (jsonObject.optString("lang")!=null) {
                                        String code=jsonObject.optString("lang");
                                        mLangFrom=Utils.getLangByCode(code, getAppSession().getLangs());

                                        if (mLangFrom==null) {
                                            getViewState().showLangs(getContext().getString(R.string.UnableToDetermine),
                                                    mLangTo.getName(), false);
                                            getViewState().showLoading(false);
                                            return;
                                        }
                                        showLangs();
                                        doMakeCall(finalCall);
                                    }
                                    else onFailure(call, null);
                                }
                                catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                    onFailure(call, null);
                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                            if (curReqNum!=mRequestNum) return;

                            if (t!=null) t.printStackTrace();

                            setLoading(false);

                            if (t instanceof IOException) {
                                getViewState().showError(true, getContext().getString(R.string.ConnectionError),
                                        getContext().getString(R.string.CheckConnection), true);
                            }
                            else {
                                getViewState().showError(true, getContext().getString(R.string.UnknownError),
                                        null, true);
                            }
                        }
                    }
            );

        }

    }


    public void gotResponse(int reqNum, boolean finalCall, boolean dictionary, boolean successful,
                            int errorCode, Record record, String direction, boolean noDict) {

        Log.d(APP_TAG, TAG+"gotResponse, successfull="+successful+" errorCode="+errorCode);
        if (reqNum!=mRequestNum) return;


        if (!successful && !(dictionary && errorCode==501)) {

            setLoading(false);

            mGotDictionaryResponse=true;
            mGotTranslationResponse=true;

            mDictionaryRecord=null;
            mTranslationRecord=null;
            mCurRecord=null;

            if (errorCode==-1)
                getViewState().showError(true, getContext().getString(R.string.UnknownError), null, true);
            else if (errorCode==0)
                getViewState().showError(true, getContext().getString(R.string.ConnectionError),
                        getContext().getString(R.string.CheckConnection), true);
            else {
                String text=Utils.getErrorTextForCode(getContext(), errorCode);
                getViewState().showError(true, getContext().getString(R.string.Error), text, false);
            }

        }
        else {
            if (dictionary) {
                mGotDictionaryResponse=true;
                mDictionaryRecord=record;
            }
            else {
                mGotTranslationResponse=true;
                mTranslationRecord=record;
            }

            if ((mGotDictionaryResponse && mGotTranslationResponse) || (mGotTranslationResponse && noDict)) {

                setLoading(false);

                if (mDictionaryRecord == null || mDictionaryRecord.getText() == null || mDictionaryRecord.getText().length() < 1) {
                    mCurRecord = mTranslationRecord;
                    mCurRecord.setText(mCurText);
                    getViewState().setMainText(mCurRecord.getTranslation());
                } else {
                    mCurRecord = mDictionaryRecord;
                    mCurRecord.setDirection(direction);
                    getViewState().setMainText(mCurRecord.getTranslation());
                }
                getViewState().setDefData(Utils.generateViewModelList(mCurRecord));
                getViewState().setTranslationButtonsEnabled(!Utils.isEmpty(mCurRecord.getTranslation()));

                if (finalCall) {
                    mWasDetermined = false;
                    saveCurRecord();
                }

                updateSpeechButtonStates();
                updateFavoriteButton();
            }
        }
    }

    private void setLoading(boolean loading) {

        if (loading!=mLoading) {
            if (loading) getViewState().incrementIdling();
            else getViewState().decrementIdling();
        }

        mLoading=loading;
        getViewState().showLoading(mLoading);

    }

    public void repeatClicked() {

        Log.d(APP_TAG, TAG+"repeatClicked");
        if (mCurText.length()>0) {
            makeFinalCall();
        }
    }

    public void fromClicked() {
        Intent intent=new Intent(getContext(), ChooseLangActivity.class);
        intent.putExtra(ChooseLangActivity.ARG_LANG_POSITION, ChooseLangActivity.LANG_POSITION_FROM);
        intent.putExtra(ChooseLangActivity.ARG_CUR_LANG, mLangFrom==null?"00":mLangFrom.getCode());
        getViewState().openChooseLang(intent);
    }

    public void toClicked() {
        Intent intent=new Intent(getContext(), ChooseLangActivity.class);
        intent.putExtra(ChooseLangActivity.ARG_LANG_POSITION, ChooseLangActivity.LANG_POSITION_TO);
        intent.putExtra(ChooseLangActivity.ARG_CUR_LANG, mLangTo.getCode());
        getViewState().openChooseLang(intent);
    }

    public void exchangeClicked() {

        Log.d(APP_TAG, TAG+"exchangeClicked()");

        if (mLangFrom==null) return;

        Lang pl=mLangFrom;
        mLangFrom=mLangTo;
        mLangTo=pl;
        mWasDetermined=false;
        showLangs();

        getAppSession().setLastLangFrom(mLangFrom.getCode());
        getAppSession().setLastLangTo(mLangTo.getCode());
        getAppSession().saveSettings();

        if (mCurText.length()>0 && mCurRecord!=null && mCurRecord.getTranslation()!=null) {
            setCurText(mCurRecord.getTranslation());
            makeFinalCall();
        }
    }

    private void showLangs() {
        String to=mLangTo.getName();
        String from=(mLangFrom==null)?getContext().getString(R.string.DetermineLang):mLangFrom.getName();
        getViewState().showLangs(from, to, mWasDetermined && mLangFrom!=null);

        updateSpeechButtonStates();
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TranslateFragment.REQ_CODE_GET_LANG && resultCode == Activity.RESULT_OK) {

            Lang newLang=Utils.getLangByCode(data.getStringExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE),
                    getAppSession().getLangs());

            if (data.getIntExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_POS, 0)==ChooseLangActivity.LANG_POSITION_FROM) {

                if (newLang!=null && newLang.getCode().equals(mLangTo.getCode())) mLangTo=mLangFrom;

                mLangFrom=newLang;
                if (mLangFrom==null) mWasDetermined=true;
                else {
                    mWasDetermined=false;
                    mLangFrom.setAskedTime(System.currentTimeMillis());
                    getDaoSession().getLangDao().insertOrReplace(mLangFrom);
                }

                if (mLangTo==null) {
                    getDefaultLangTo();
                }

                getAppSession().setLastLangFrom(data.getStringExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE));
                getAppSession().saveSettings();

            }
            else {
                if (newLang!=null && mLangFrom!=null && newLang.getCode().equals(mLangFrom.getCode())) mLangFrom=mLangTo;
                mLangTo=newLang;

                if (mLangTo!=null) {
                    mLangTo.setAskedTime(System.currentTimeMillis());
                    getDaoSession().getLangDao().insertOrReplace(mLangTo);
                }

                getAppSession().setLastLangTo(data.getStringExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE));
                getAppSession().saveSettings();

            }
            showLangs();

            if (mCurText.length()>0) makeCall(true);
        }
        else if (requestCode == TranslateFragment.REQUEST_CODE_RECOGNIZE && resultCode == RecognizerActivity.RESULT_OK) {
            final String result = data.getStringExtra(RecognizerActivity.EXTRA_RESULT);
            setCurText(mCurText+result);
            makeFinalCall();
        }

    }

    private void getDefaultLangTo() {

        if (mLangFrom==null) mLangTo=Utils.getLangByCode("ru", getAppSession().getLangs());
        else {
            if (mLangFrom.getCode().equals("ru"))  mLangTo=Utils.getLangByCode("en", getAppSession().getLangs());
            else mLangTo=Utils.getLangByCode("ru", getAppSession().getLangs());
        }

        if (mLangTo==null) mLangTo= getAppSession().getLangs().get(0);
        if (mLangFrom==mLangTo) mLangTo= getAppSession().getLangs().get(1);

        if (mLangTo==null || mLangFrom==null || getAppSession().getLangs().size()<3) {
            getViewState().toFailActivity();
            return;
        }

    }

    public void outsideTouch() {
        getViewState().hideSoftKeyboard();
        getViewState().clearTextFocus();
    }

    public void textLostFocus() {

        Log.d(APP_TAG, TAG+"textLostFocus");

            makeFinalCall();
    }

    public void synonymClicked(String word) {

        exchangeClicked();
        setCurText(word);
        makeFinalCall();
    }

    public void micClicked() {

        if (mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null) {

            Intent intent = new Intent(getContext(), RecognizerActivity.class);
            intent.putExtra(RecognizerActivity.EXTRA_MODEL, Recognizer.Model.NOTES);
            intent.putExtra(RecognizerActivity.EXTRA_LANGUAGE, Utils.getSpeechCodeForLang(mLangFrom.getCode()));
            intent.putExtra(RecognizerActivity.EXTRA_SHOW_PARTIAL_RESULTS, true);

            getViewState().startRecognition(intent);
        }

    }

    public AsyncTask makeAsyncTaskForVocalizer(final Vocalizer vocalizer) {
        return new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    Thread.sleep(VOCALIZER_TIMEOUT);
                } catch (InterruptedException e) { }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                Log.d(TAG, "asyncTaskForVocalizer onPostExecute");
                vocalizer.cancel();
            }

            @Override
            protected void onCancelled() {
                Log.d(TAG, "asyncTaskForVocalizer cancelled");
                super.onCancelled();
            }
        };
    }

    public void speakClicked() {

        if (mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null) {
            mTextSpeechProgress=true;
            mTextVocalizer=Vocalizer.createVocalizer(Utils.getSpeechCodeForLang(mLangFrom.getCode()), mCurText, true);
            final AsyncTask asyncTask = makeAsyncTaskForVocalizer(mTextVocalizer).execute();
            VocalizerListener listener = new VocalizerListener() {
                @Override
                public void onSynthesisBegin(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onSynthesisBegin");
                }

                @Override
                public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis) {
                    Log.d(APP_TAG, TAG+"onSynthesisDone");
                }

                @Override
                public void onPlayingBegin(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onPlayingBegin");
                    asyncTask.cancel(true);
                }

                @Override
                public void onPlayingDone(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onPlayingDone");
                    asyncTask.cancel(true);
                    mTextSpeechProgress=false;
                    showLangs();
                }

                @Override
                public void onVocalizerError(Vocalizer vocalizer, Error error) {
                    Log.d(APP_TAG, TAG+"onVocalizerError");
                    asyncTask.cancel(true);
                    makeTextSpeechError();
                }
            };
            mTextVocalizer.setListener(listener);
            mTextVocalizer.start();

            showLangs();

            makeFinalCall();
        }
    }

    private void makeTextSpeechError() {
        mTextSpeechProgress=false;
        showLangs();
        Toast.makeText(getContext(), getContext().getString(R.string.UnableToSynthesizeSpeech), Toast.LENGTH_SHORT).show();
    }

    public void speakTrslClicked() {

        if (mCurRecord!=null && mCurRecord.getTranslation()!=null && mCurRecord.getDirection()!=null &&
                Utils.getSpeechCodeForLang(mCurRecord.getDirection().substring(3))!=null) {
            mTranslateSpeechProgress=true;
            Log.d(APP_TAG, TAG+"direction="+mCurRecord.getDirection());
            mTranslateVocalizer=Vocalizer.createVocalizer(
                    Utils.getSpeechCodeForLang(mCurRecord.getDirection().substring(3)), mCurRecord.getTranslation(), true);
            final AsyncTask asyncTask = makeAsyncTaskForVocalizer(mTranslateVocalizer).execute();
            VocalizerListener listener =new VocalizerListener() {
                @Override
                public void onSynthesisBegin(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onSynthesisBegin");
                }

                @Override
                public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis) {
                    Log.d(APP_TAG, TAG+"onSynthesisDone");
                }

                @Override
                public void onPlayingBegin(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onPlayingBegin");
                    asyncTask.cancel(true);
                }

                @Override
                public void onPlayingDone(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onPlayingDone");
                    asyncTask.cancel(true);
                    mTranslateSpeechProgress=false;
                    showLangs();
                }

                @Override
                public void onVocalizerError(Vocalizer vocalizer, Error error) {
                    Log.d(APP_TAG, TAG+"onVocalizerError");
                    asyncTask.cancel(true);
                    makeTrslSpeechError();
                }
            };
            mTranslateVocalizer.setListener(listener);
            mTranslateVocalizer.start();

            showLangs();

            saveCurRecord();
        }
    }

    private void makeTrslSpeechError() {
        mTranslateSpeechProgress=false;
        showLangs();
        Toast.makeText(getContext(), getContext().getString(R.string.UnableToSynthesizeSpeech), Toast.LENGTH_SHORT).show();
    }

    private void setCurText(String text) {
        mCurText=text;
        getViewState().setText(text);
    }

    private void updateSpeechButtonStates(){
        getViewState().setRecognitionEnabled(mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null);
        getViewState().setTextSpeechStatus(mCurText.length()>0, mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null
                && mCurText.length()<=100, mTextSpeechProgress);
        getViewState().setTranslateSpeechStatus(mCurRecord!=null && mCurRecord.getDirection()!=null &&
                Utils.getSpeechCodeForLang(mCurRecord.getDirection().substring(3))!=null
                && !Utils.isEmpty(mCurRecord.getTranslation()) && mCurRecord.getTranslation().length()<=100, mTranslateSpeechProgress);
    }


    public void bigClicked() {
        if (mCurRecord!=null && mCurRecord.getTranslation()!=null && mCurRecord.getTranslation().length()>0) {
            Intent intent=new Intent(getContext(), FullscreenActivity.class);
            intent.putExtra(FullscreenActivity.ARG_TEXT, mCurRecord.getTranslation());
            getViewState().startFullscreen(intent);

            saveCurRecord();
        }
    }

    public void mainTextClicked() {

        copyTextToClipBoard();

        getViewState().hideSoftKeyboard();
        getViewState().clearTextFocus();
    }

    private void copyTextToClipBoard() {

        if (mCurRecord!=null && mCurRecord.getTranslation()!=null) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Activity.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("translation", mCurRecord.getTranslation());
            clipboard.setPrimaryClip(clip);

            getViewState().showToast(getContext().getString(R.string.TranslationCopied));
        }

    }

    public void shareClicked() {
        if (mCurRecord!=null && !Utils.isEmpty(mCurRecord.getTranslation())) {
            getViewState().share(mCurRecord.getTranslation());
            saveCurRecord();
        }
    }

    private void updateFavoriteButton() {
        if (mCurRecord!=null && !Utils.isEmpty(mCurRecord.getText())) {
            if (getDaoSession().getRecordDao().load(mCurRecord.getId())!=null) {
                getViewState().setFavoriteOn(getDaoSession().getRecordDao().load(mCurRecord.getId()).isInFavorite());
                return;
            }
        }
        getViewState().setFavoriteOn(false);
    }

    public void favoriteClicked() {

        if (mCurRecord==null || Utils.isEmpty(mCurRecord.getText())) return;

        saveCurRecord();

        mCurRecord.setInFavorite(!mCurRecord.isInFavorite());
        if (mCurRecord.isInFavorite()) mCurRecord.setFavoriteTime(System.currentTimeMillis());
        getDaoSession().insertOrReplace(mCurRecord);
        getViewState().setFavoriteOn(mCurRecord.isInFavorite());

        EventBus.getDefault().post(new RecordChangedEvent(mCurRecord, RecordChangedEvent.SENDER_TRANSLATION));

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordChangedEvent(RecordChangedEvent event) {

        Log.d(APP_TAG, TAG + "onRecordChangedEvent");

        if (event.getSender() == RecordChangedEvent.SENDER_TRANSLATION) return;

        if (mCurRecord!=null && event.getRecord().getId().equals(mCurRecord.getId())) {
            mCurRecord.setInFavorite(event.getRecord().isInFavorite());
            mCurRecord.setFavoriteTime(event.getRecord().getFavoriteTime());
            getViewState().setFavoriteOn(mCurRecord.isInFavorite());
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectRecordEvent(SelectRecordEvent selectRecordEvent) {

        Log.d(APP_TAG, TAG + "onRecordChangedEvent");

        mCurRecord=selectRecordEvent.getRecord().copy();
        mCurText=mCurRecord.getText();
        getViewState().setText(mCurRecord.getText());
        mRequestNum++;

        getViewState().showLoading(false);

        if (mCurRecord.getType()==Record.TYPE_SENTENSE) {
            mTranslationRecord = mCurRecord;
            getViewState().setMainText(mCurRecord.getTranslation());
        } else {
            mDictionaryRecord = mCurRecord;
            getViewState().setMainText(mCurRecord.getTranslation());
        }
        if (getAppSession().isShowDict())
            getViewState().setDefData(Utils.generateViewModelList(mCurRecord));
        else getViewState().setDefData(new ArrayList<ListItem>());
        getViewState().setTranslationButtonsEnabled(!Utils.isEmpty(mCurRecord.getTranslation()));

        mLangFrom=Utils.getLangByCode(mCurRecord.getDirection().substring(0,2), getAppSession().getLangs());
        mLangTo=Utils.getLangByCode(mCurRecord.getDirection().substring(3,5), getAppSession().getLangs());

        getAppSession().setLastLangFrom(mLangFrom.getCode());
        getAppSession().setLastLangTo(mLangTo.getCode());
        getAppSession().saveSettings();

        showLangs();

        updateSpeechButtonStates();
        updateFavoriteButton();

        saveCurRecord();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFullReloadNeededEvent(FullReloadNeededEvent event) {

        if (event.getSender()==FullReloadNeededEvent.SENDER_FAVORITE && mCurRecord!=null) {
            mCurRecord.setInFavorite(false);
            getViewState().setFavoriteOn(false);
        }

    }


    public void keyboardClosed() {
        if (mCurText.length()>0) {

            makeFinalCall();
            getViewState().hideSoftKeyboard();
            getViewState().clearTextFocus();
        }
    }
}