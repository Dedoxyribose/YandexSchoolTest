package ru.dedoxyribose.yandexschooltest.ui.translate;


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
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.ServerApi;
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

        RetrofitHelper.getServerApi().lookup(getContext().getString(R.string.dict_key),
                "en-ru", mCurText, "ru").enqueue(
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
                "en-ru", mCurText).enqueue(
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
}