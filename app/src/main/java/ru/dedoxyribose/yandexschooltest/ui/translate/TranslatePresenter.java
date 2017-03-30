package ru.dedoxyribose.yandexschooltest.ui.translate;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
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
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.ui.chooselang.ChooseLangActivity;
import ru.dedoxyribose.yandexschooltest.ui.fullscreen.FullscreenActivity;
import ru.dedoxyribose.yandexschooltest.ui.recordlist.RecordListFragment;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
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

    private volatile Record mCurRecord;
    private String mCurText="";

    private boolean mGotDictionaryResponse;
    private boolean mGotTranslationResponse;

    private Record mDictionaryRecord;
    private Record mTranslationRecord;

    private int mRequestNum=0;

    private long mLastChangeTextTime=0;
    private Thread mWaiter;
    private boolean mPresenterDestroyed=false;

    private Lang mLangFrom;
    private Lang mLangTo;
    private boolean mWasDetermined=false;

    private boolean mTextSpeechProgress=false;
    private boolean mTranslateSpeechProgress=false;

    private Vocalizer mTextVocalizer;
    private Vocalizer mTranslateVocalizer;

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        Log.d(APP_TAG, TAG+this.toString());

        getViewState().setTranslationButtonsEnabled(false);

        mWaiter=new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        Thread.sleep(200);

                        if (mLastChangeTextTime!=0 && System.currentTimeMillis()-INPUT_TIMEOUT>=mLastChangeTextTime
                                && Singletone.getInstance().isSyncTranslation()) {
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

        mLangFrom=Utils.getLangByCode(Singletone.getInstance().getLastLangFrom(), Singletone.getInstance().getLangs());
        mLangTo=Utils.getLangByCode(Singletone.getInstance().getLastLangTo(), Singletone.getInstance().getLangs());

        if (mLangTo==null) {
            mLangTo=Singletone.getInstance().getLangs().get(0);
        }

        if (mLangFrom==null) mLangFrom=Utils.getLangByCode(mLangTo.getCode().equals("ru")?"en":"ru", Singletone.getInstance().getLangs());

        //TODO обработать ошибку, если вообще почему-то нет языков, или мало и т.д.

        showLangs();

        EventBus.getDefault().register(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenterDestroyed=true;
        if (mWaiter!=null && mWaiter.isAlive()) mWaiter.interrupt();

        if (mTextVocalizer!=null) mTextVocalizer.cancel();
        if (mTranslateVocalizer!=null) mTranslateVocalizer.cancel();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void attachView(TranslateView view) {
        super.attachView(view);


    }

    public void clearClicked() {
        getViewState().setText("");
        mRequestNum++;

    }

    public void textChanged(CharSequence charSequence) {

        Log.d(APP_TAG, TAG+"textChanged()");

        mLastChangeTextTime=System.currentTimeMillis();

        mCurText=charSequence.toString();

        getViewState().showError(false, null, null, false);

        showLangs();
    }

    public void returnPressed() {

        Log.d(APP_TAG, TAG+"returnPressed");

        if (Singletone.getInstance().isReturnTranslate()) {
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

        final int curReqNum=mRequestNum;

        final String direction=mLangFrom.getCode()+"-"+mLangTo.getCode();

        final boolean noDict=!Singletone.getInstance().isShowDict();

        if (!noDict) {

            RetrofitHelper.getServerApi().lookup(getContext().getString(R.string.dict_key),
                    direction, mCurText, "ru").enqueue(
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

        RetrofitHelper.getServerApi().translate(getContext().getString(R.string.trans_key),
                direction, mCurText).enqueue(
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

        if (mCurRecord==null || mCurRecord.getText()==null || !mCurRecord.getText().equals(mCurText) || mWasDetermined)
            makeCall(true);
        else if (mCurRecord!=null && mCurRecord.getText()!=null && mCurRecord.getText().equals(mCurText)) {
            mWasDetermined=false;
            saveCurRecord();
        }

    }

    private void saveCurRecord() {

        Log.d(APP_TAG, TAG+"saveCurRecord");

        Record record=getDaoSession().getRecordDao().load(mCurRecord.getId());

        if (record!=null) {
            mCurRecord=record.copy();
            mCurRecord.setHistoryTime(System.currentTimeMillis());
            mCurRecord.setInHistory(true);
        }
        else {
            mCurRecord.setInHistory(true);
            mCurRecord.setHistoryTime(System.currentTimeMillis());
        }

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

        getViewState().showLoading(true);
        getViewState().showError(false, null, null, false);

        final int curReqNum=mRequestNum;

        if (!mWasDetermined) {
            doMakeCall(finalCall);
            showLangs();
        }
        else {

            RetrofitHelper.getServerApi().detect(getContext().getString(R.string.trans_key), mCurText).enqueue(
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
                                        mLangFrom=Utils.getLangByCode(code, Singletone.getInstance().getLangs());

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

                            getViewState().showLoading(false);
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

            mGotDictionaryResponse=true;
            mGotTranslationResponse=true;

            mDictionaryRecord=null;
            mTranslationRecord=null;
            mCurRecord=null;

            getViewState().showLoading(false);

            if (errorCode==-1)
                getViewState().showError(true, getContext().getString(R.string.UnknownError), null, true);
            else if (errorCode==0)
                getViewState().showError(true, getContext().getString(R.string.ConnectionError),
                        getContext().getString(R.string.CheckConnection), true);
            else
            {
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

                getViewState().showLoading(false);

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

        Singletone.getInstance().setLastLangFrom(mLangFrom.getCode());
        Singletone.getInstance().setLastLangTo(mLangTo.getCode());
        Singletone.getInstance().saveSettings();

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
                    Singletone.getInstance().getLangs());

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

                Singletone.getInstance().setLastLangFrom(data.getStringExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE));
                Singletone.getInstance().saveSettings();

            }
            else {
                if (newLang!=null && mLangFrom!=null && newLang.getCode().equals(mLangFrom.getCode())) mLangFrom=mLangTo;
                mLangTo=newLang;

                if (mLangTo!=null) {
                    mLangTo.setAskedTime(System.currentTimeMillis());
                    getDaoSession().getLangDao().insertOrReplace(mLangTo);
                }

                Singletone.getInstance().setLastLangTo(data.getStringExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE));
                Singletone.getInstance().saveSettings();

            }
            showLangs();

            if (mCurText.length()>0) makeCall(false);
        }
        else if (requestCode == TranslateFragment.REQUEST_CODE_RECOGNIZE && resultCode == RecognizerActivity.RESULT_OK) {
            final String result = data.getStringExtra(RecognizerActivity.EXTRA_RESULT);
            setCurText(result);
            makeFinalCall();
        }

    }

    private void getDefaultLangTo() {

        if (mLangFrom==null) mLangTo=Utils.getLangByCode("ru", Singletone.getInstance().getLangs());
        else {
            if (mLangFrom.getCode().equals("ru"))  mLangTo=Utils.getLangByCode("en", Singletone.getInstance().getLangs());
            else mLangTo=Utils.getLangByCode("ru", Singletone.getInstance().getLangs());
        }

        if (mLangTo==null) mLangTo=Singletone.getInstance().getLangs().get(0);
        if (mLangFrom==mLangTo) mLangTo=Singletone.getInstance().getLangs().get(1);

        //TODO handle errors
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

    public void speakClicked() {

        if (mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null) {
            mTextSpeechProgress=true;
            mTextVocalizer=Vocalizer.createVocalizer(Utils.getSpeechCodeForLang(mLangFrom.getCode()), mCurText, true);
            mTextVocalizer.setListener(new VocalizerListener() {
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
                }

                @Override
                public void onPlayingDone(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onPlayingDone");
                    mTextSpeechProgress=false;
                    showLangs();
                }

                @Override
                public void onVocalizerError(Vocalizer vocalizer, Error error) {
                    Log.d(APP_TAG, TAG+"onVocalizerError");
                    mTextSpeechProgress=false;
                    showLangs();
                    Toast.makeText(getContext(), getContext().getString(R.string.UnableToSynthesizeSpeech), Toast.LENGTH_SHORT).show();
                }
            });
            mTextVocalizer.start();

            showLangs();

            makeFinalCall();
        }
    }

    public void speakTrslClicked() {

        if (mCurRecord!=null && mCurRecord.getTranslation()!=null && mCurRecord.getDirection()!=null &&
                Utils.getSpeechCodeForLang(mCurRecord.getDirection().substring(3))!=null) {
            mTranslateSpeechProgress=true;
            Log.d(APP_TAG, TAG+"direction="+mCurRecord.getDirection());
            mTranslateVocalizer=Vocalizer.createVocalizer(
                    Utils.getSpeechCodeForLang(mCurRecord.getDirection().substring(3)), mCurRecord.getTranslation(), true);
            mTranslateVocalizer.setListener(new VocalizerListener() {
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
                }

                @Override
                public void onPlayingDone(Vocalizer vocalizer) {
                    Log.d(APP_TAG, TAG+"onPlayingDone");
                    mTranslateSpeechProgress=false;
                    showLangs();
                }

                @Override
                public void onVocalizerError(Vocalizer vocalizer, Error error) {
                    Log.d(APP_TAG, TAG+"onVocalizerError");
                    mTranslateSpeechProgress=false;
                    showLangs();
                    Toast.makeText(getContext(), getContext().getString(R.string.UnableToSynthesizeSpeech), Toast.LENGTH_SHORT).show();
                }
            });
            mTranslateVocalizer.start();

            showLangs();

            saveCurRecord();
        }
    }

    private void setCurText(String text) {
        mCurText=text;
        getViewState().setText(text);
    }

    private void updateSpeechButtonStates(){
        getViewState().setRecognitionEnabled(mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null);
        getViewState().setTextSpeechStatus(mLangFrom!=null && Utils.getSpeechCodeForLang(mLangFrom.getCode())!=null
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
        getViewState().setDefData(Utils.generateViewModelList(mCurRecord));
        getViewState().setTranslationButtonsEnabled(!Utils.isEmpty(mCurRecord.getTranslation()));

        mLangFrom=Utils.getLangByCode(mCurRecord.getDirection().substring(0,2), Singletone.getInstance().getLangs());
        mLangTo=Utils.getLangByCode(mCurRecord.getDirection().substring(3,5), Singletone.getInstance().getLangs());

        showLangs();

        updateSpeechButtonStates();
        updateFavoriteButton();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFullReloadNeededEvent(FullReloadNeededEvent event) {

        if (event.getSender()==FullReloadNeededEvent.SENDER_FAVORITE && mCurRecord!=null) {
            mCurRecord.setInFavorite(false);
            getViewState().setFavoriteOn(false);
        }

    }
}