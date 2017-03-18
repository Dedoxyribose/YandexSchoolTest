package ru.dedoxyribose.yandexschooltest.ui.translate;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.ui.chooselang.ChooseLangActivity;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.ServerApi;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.dedoxyribose.yandexschooltest.util.Utils;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class TranslatePresenter extends StandardMvpPresenter<TranslateView>{

    public static final long INPUT_TIMEOUT=500;

    private Record mCurRecord;
    private String mCurText="";
    private String mCurDirection="en-ru";

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

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        mWaiter=new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        Thread.sleep(200);

                        if (mLastChangeTextTime!=0 && System.currentTimeMillis()-INPUT_TIMEOUT>=mLastChangeTextTime) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    makeCall();
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
        mLangTo=Utils.getLangByCode(Singletone.getInstance().getLastLangFrom(), Singletone.getInstance().getLangs());

        if (mLangTo==null) {
            mLangTo=Singletone.getInstance().getLangs().get(0);
        }

        if (mLangFrom==null) mLangFrom=Utils.getLangByCode(mLangTo.getCode().equals("ru")?"en":"ru", Singletone.getInstance().getLangs());

        //TODO обработать ошибку, если вообще почему-то нет языков, или мало и т.д.

        showLangs();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenterDestroyed=true;
        if (mWaiter!=null && mWaiter.isAlive()) mWaiter.interrupt();
    }

    @Override
    public void attachView(TranslateView view) {
        super.attachView(view);


    }

    public void clearClicked() {
        getViewState().setText("");
    }

    public void textChanged(CharSequence charSequence) {

        mLastChangeTextTime=System.currentTimeMillis();

        mCurText=charSequence.toString();

        getViewState().showError(false, null, null, false);
    }

    public void returnPressed() {

        Log.d(TAG, "returnPressed");
        if (mCurText.length()>0) {

            makeCall();
        }
    }

    private void makeCall() {

        mLastChangeTextTime=0;

        mRequestNum++;
        final int curReqNum=mRequestNum;

        mGotDictionaryResponse=false;
        mGotTranslationResponse=false;

        getViewState().showLoading(true);
        getViewState().showError(false, null, null, false);

        String direction=mLangFrom.getCode()+"-"+mLangTo.getCode();

        RetrofitHelper.getServerApi().lookup(getContext().getString(R.string.dict_key),
                direction, mCurText, "ru").enqueue(
                new Callback<Record>() {
                    @Override
                    public void onResponse(Call<Record> call, Response<Record> response) {
                        gotResponse(curReqNum, true, response.isSuccessful(), Utils.extractErrorCode(response),
                                response.isSuccessful()?response.body():null);
                    }

                    @Override
                    public void onFailure(Call<Record> call, Throwable t) {
                        gotResponse(curReqNum, true, false, 0, null);
                        if (t!=null) t.printStackTrace();
                    }
                });

        RetrofitHelper.getServerApi().translate(getContext().getString(R.string.trans_key),
                direction, mCurText).enqueue(
                new Callback<Record>() {
                    @Override
                    public void onResponse(Call<Record> call, Response<Record> response) {
                        gotResponse(curReqNum, false, response.isSuccessful(), Utils.extractErrorCode(response),
                                response.isSuccessful()?response.body():null);
                    }

                    @Override
                    public void onFailure(Call<Record> call, Throwable t) {
                        gotResponse(curReqNum, false, false, 0, null);
                        if (t!=null) t.printStackTrace();
                    }
                });
    }


    public void gotResponse(int reqNum, boolean dictionary, boolean successful, int errorCode, Record record) {

        Log.d(TAG, "gotResponse, successfull="+successful+" errorCode="+errorCode);
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

            if (mGotDictionaryResponse && mGotTranslationResponse) {

                getViewState().showLoading(false);

                if (mDictionaryRecord==null || mDictionaryRecord.getText()==null || mDictionaryRecord.getText().length()<1) {
                    mCurRecord=mTranslationRecord;
                    mCurRecord.setText(mCurText);
                    getViewState().setMainText(mCurRecord.getTranslation());
                }
                else {
                    mCurRecord=mDictionaryRecord;
                    mCurRecord.setDirection(mCurDirection);
                    getViewState().setMainText(mCurRecord.getTranslation());
                }
                getViewState().setDefData(Utils.generateViewModelList(mCurRecord));
            }
        }
    }

    public void repeatClicked() {

        Log.d(TAG, "repeatClicked");
        if (mCurText.length()>0) {

            makeCall();
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
        Lang pl=mLangFrom;
        mLangFrom=mLangTo;
        mLangTo=pl;
        showLangs();
    }

    private void showLangs() {
        String to=mLangTo.getName();
        String from=(mLangFrom==null)?getContext().getString(R.string.DetermineLang):mLangFrom.getName();
        if (mLangFrom!=null && mWasDetermined) from+=("\n"+getContext().getString(R.string.DeterminedAutomatically));
        getViewState().showLangs(from, to);
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TranslateFragment.REQ_CODE_GET_LANG && resultCode == Activity.RESULT_OK) {
            Lang newLang=Utils.getLangByCode(data.getStringExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE),
                    Singletone.getInstance().getLangs());
            if (data.getIntExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_POS, 0)==ChooseLangActivity.LANG_POSITION_FROM) {
                if (newLang!=null && newLang.getCode().equals(mLangTo.getCode())) mLangTo=mLangFrom;
                mLangFrom=newLang;
                if (mLangFrom==null) mWasDetermined=true;
            }
            else {
                if (newLang!=null && newLang.getCode().equals(mLangFrom.getCode())) mLangFrom=mLangTo;
                mLangTo=newLang;
            }
            showLangs();
        }

    }
}