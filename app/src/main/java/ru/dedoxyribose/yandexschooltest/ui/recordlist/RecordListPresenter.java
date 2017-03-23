package ru.dedoxyribose.yandexschooltest.ui.recordlist;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.util.SortedList;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.event.RecordChangedEvent;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.RecordDao;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.ui.chooselang.ChooseLangActivity;
import ru.dedoxyribose.yandexschooltest.ui.fullscreen.FullscreenActivity;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateFragment;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateView;
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
public class RecordListPresenter extends StandardMvpPresenter<RecordListView>{

    private List<Record> mCurList;
    private List<Record> mSearchList;
    private List<Record> mFullList;

    private int mType;

    public void setType(int type) {
        mType=type;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        mFullList = getDaoSession().getRecordDao().queryBuilder()
                .where((mType==RecordListFragment.TYPE_HISTORY)?
                        RecordDao.Properties.InHistory.eq(true)
                        :RecordDao.Properties.InFavorite.eq(true)).orderDesc(
                        (mType==RecordListFragment.TYPE_HISTORY)?RecordDao.Properties.HistoryTime:RecordDao.Properties.FavoriteTime)
                        .list();

        mCurList=mFullList;
        getViewState().setData(mFullList);
        getViewState().notifyDataSetChanged();

        EventBus.getDefault().register(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void iconClicked(int i) {

        Record record = mCurList.get(i);

        if (!record.isInFavorite()) {
            record.setInFavorite(true);
            if (record.getFavoriteTime()==0) record.setFavoriteTime(System.currentTimeMillis());
        }
        else {
            record.setInFavorite(false);
        }

        if (record.isInFavorite() || record.isInHistory())
            getDaoSession().insertOrReplace(record);
        else getDaoSession().delete(record);

        getViewState().setData(mCurList);
        getViewState().notifyItemChanged(i);

        EventBus.getDefault().post(new RecordChangedEvent(record,
                mType==RecordListFragment.TYPE_HISTORY?RecordChangedEvent.SENDER_HISTORY:RecordChangedEvent.SENDER_FAVORITE));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordChangedEvent(RecordChangedEvent event) {

        Log.d(APP_TAG, TAG+"onRecordChangedEvent");

        if ((event.getSender()==RecordChangedEvent.SENDER_HISTORY && mType==RecordListFragment.TYPE_HISTORY) ||
                (event.getSender()==RecordChangedEvent.SENDER_FAVORITE && mType==RecordListFragment.TYPE_FAVORITE)) return;

        boolean found=false;

        for (int i=0; i<mFullList.size(); i++) {
            Record record=mFullList.get(i);

            if (record.getId().equals(event.getRecord().getId())) {

                Log.d(APP_TAG, "well, found");
                Log.d(APP_TAG, "record.ht="+record.getHistoryTime());
                Log.d(APP_TAG, "event.record.ht="+event.getRecord().getHistoryTime());

                if (record.getHistoryTime()!=event.getRecord().getHistoryTime() && mType==RecordListFragment.TYPE_HISTORY) {

                    Log.d(APP_TAG, "gonna replace");

                    mFullList.remove(i);
                    getViewState().setData(mFullList);
                    getViewState().notifyItemRemoved(i);
                    break;
                }

                found=true;

                if (mType==RecordListFragment.TYPE_HISTORY) {
                    if (!event.getRecord().isInHistory()) {
                        mFullList.remove(i);
                        getViewState().setData(mFullList);
                        getViewState().notifyDataSetChanged();
                    }
                    else {
                        mFullList.set(i, event.getRecord());
                        getViewState().setData(mFullList);
                        getViewState().notifyItemChanged(i);
                    }
                }
                else {
                    if (!event.getRecord().isInFavorite()) {
                        mFullList.remove(i);
                        getViewState().setData(mFullList);
                        getViewState().notifyDataSetChanged();
                    }
                    else {
                        mFullList.set(i, event.getRecord());
                        getViewState().setData(mFullList);
                        getViewState().notifyItemChanged(i);
                    }
                }

                break;
            }
        }

        if (!found && event.getRecord().isInFavorite() && mType==RecordListFragment.TYPE_FAVORITE) {

            boolean inserted=false;
            for (int i=0; i<mFullList.size(); i++) {
                if (mFullList.get(i).getFavoriteTime()<event.getRecord().getFavoriteTime()) {
                    mFullList.add(i, event.getRecord());
                    inserted=true;
                    break;
                }
            }

            if (!inserted) {
                mFullList.add(event.getRecord());
            }

            getViewState().setData(mFullList);
            getViewState().notifyDataSetChanged();

        }

        if (!found && event.getRecord().isInHistory() && mType==RecordListFragment.TYPE_HISTORY) {

            boolean inserted=false;
            for (int i=0; i<mFullList.size(); i++) {
                if (mFullList.get(i).getHistoryTime()<event.getRecord().getHistoryTime()) {
                    mFullList.add(i, event.getRecord());
                    getViewState().setData(mFullList);
                    getViewState().notifyDataSetChanged();
                    inserted=true;
                    break;
                }
            }

            if (!inserted) {
                mFullList.add(event.getRecord());
            }

            getViewState().setData(mFullList);
            getViewState().notifyDataSetChanged();

        }

    }


    public void update() {

        if (mType==RecordListFragment.TYPE_HISTORY) return;

        if (mFullList==null) return;

        for (int i=0; i<mFullList.size(); i++) {
            if (!mFullList.get(i).isInFavorite()) {
                mFullList.remove(i);
                i--;
            }
        }

        mCurList=mFullList;
        getViewState().setData(mFullList);
        getViewState().notifyDataSetChanged();
    }
}