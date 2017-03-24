package ru.dedoxyribose.yandexschooltest.ui.recordlist;



import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.async.AsyncSession;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.event.FullReloadNeededEvent;
import ru.dedoxyribose.yandexschooltest.event.RecordChangedEvent;
import ru.dedoxyribose.yandexschooltest.event.SelectRecordEvent;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.RecordDao;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class RecordListPresenter extends StandardMvpPresenter<RecordListView>{

    private List<Record> mRecordList;

    private int mType;

    private boolean mIsSearch=false;
    private boolean mBusy=false;

    public void setType(int type) {
        mType=type;
    }

    private int mReqNum=0;

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        loadAll();

        EventBus.getDefault().register(this);

    }

    @Override
    public void attachView(RecordListView view) {
        super.attachView(view);

    }

    private void loadAll() {

        getViewState().showLoading(true);
        mBusy=true;

        AsyncSession asyncSession = getDaoSession().startAsyncSession();

        mReqNum++;
        final int curReqNum=mReqNum;

        asyncSession.setListenerMainThread(new AsyncOperationListener() {
            @Override
            public void onAsyncOperationCompleted(AsyncOperation operation) {

                if (curReqNum!=mReqNum) return;

                mIsSearch=false;

                getViewState().showLoading(false);

                mRecordList= (List<Record>) operation.getResult();

                getViewState().showEmpty(mRecordList.size()==0);

                getViewState().setData(mRecordList);
                getViewState().notifyDataSetChanged();

                mBusy=false;

                getViewState().updateClearButtonState();

            }
        });

        asyncSession.queryList(getDaoSession().getRecordDao().queryBuilder()
                .where((mType==RecordListFragment.TYPE_HISTORY)?
                        RecordDao.Properties.InHistory.eq(true)
                        :RecordDao.Properties.InFavorite.eq(true)).orderDesc(
                        (mType==RecordListFragment.TYPE_HISTORY)?RecordDao.Properties.HistoryTime:RecordDao.Properties.FavoriteTime)
                        .build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void iconClicked(int i) {

        Record record = mRecordList.get(i);

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

        getViewState().setData(mRecordList);
        getViewState().notifyItemChanged(i);

        EventBus.getDefault().post(new RecordChangedEvent(record,
                mType==RecordListFragment.TYPE_HISTORY?RecordChangedEvent.SENDER_HISTORY:RecordChangedEvent.SENDER_FAVORITE));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordChangedEvent(RecordChangedEvent event) {

        Log.d(APP_TAG, TAG+"onRecordChangedEvent");

        if (mBusy) return;

        if (mIsSearch) return;

        if ((event.getSender()==RecordChangedEvent.SENDER_HISTORY && mType==RecordListFragment.TYPE_HISTORY) ||
                (event.getSender()==RecordChangedEvent.SENDER_FAVORITE && mType==RecordListFragment.TYPE_FAVORITE)) return;

        boolean found=false;

        for (int i = 0; i< mRecordList.size(); i++) {
            Record record= mRecordList.get(i);

            if (record.getId().equals(event.getRecord().getId())) {

                Log.d(APP_TAG, "well, found");
                Log.d(APP_TAG, "record.ht="+record.getHistoryTime());
                Log.d(APP_TAG, "event.record.ht="+event.getRecord().getHistoryTime());

                if (record.getHistoryTime()!=event.getRecord().getHistoryTime() && mType==RecordListFragment.TYPE_HISTORY) {

                    Log.d(APP_TAG, "gonna replace");

                    mRecordList.remove(i);
                    getViewState().setData(mRecordList);
                    getViewState().notifyItemRemoved(i);
                    break;
                }

                found=true;

                if (mType==RecordListFragment.TYPE_HISTORY) {
                    if (!event.getRecord().isInHistory()) {
                        mRecordList.remove(i);
                        getViewState().setData(mRecordList);
                        getViewState().notifyDataSetChanged();
                    }
                    else {
                        mRecordList.set(i, event.getRecord());
                        getViewState().setData(mRecordList);
                        getViewState().notifyItemChanged(i);
                    }
                }
                else {
                    if (!event.getRecord().isInFavorite()) {
                        mRecordList.remove(i);
                        getViewState().setData(mRecordList);
                        getViewState().notifyDataSetChanged();
                    }
                    else {
                        mRecordList.set(i, event.getRecord());
                        getViewState().setData(mRecordList);
                        getViewState().notifyItemChanged(i);
                    }
                }

                break;
            }
        }

        if (!found && event.getRecord().isInFavorite() && mType==RecordListFragment.TYPE_FAVORITE) {

            boolean inserted=false;
            for (int i = 0; i< mRecordList.size(); i++) {
                if (mRecordList.get(i).getFavoriteTime()<event.getRecord().getFavoriteTime()) {
                    mRecordList.add(i, event.getRecord());
                    inserted=true;
                    break;
                }
            }

            if (!inserted) {
                mRecordList.add(event.getRecord());
            }

            getViewState().setData(mRecordList);
            getViewState().notifyDataSetChanged();

        }

        if (!found && event.getRecord().isInHistory() && mType==RecordListFragment.TYPE_HISTORY) {

            boolean inserted=false;
            for (int i = 0; i< mRecordList.size(); i++) {
                if (mRecordList.get(i).getHistoryTime()<event.getRecord().getHistoryTime()) {
                    mRecordList.add(i, event.getRecord());
                    getViewState().setData(mRecordList);
                    getViewState().notifyDataSetChanged();
                    inserted=true;
                    break;
                }
            }

            if (!inserted) {
                mRecordList.add(event.getRecord());
            }

            getViewState().setData(mRecordList);
            getViewState().notifyDataSetChanged();

        }

        getViewState().showEmpty(mRecordList.size()==0);

    }


    public void update() {

        if (mIsSearch) {
            loadAll();
            getViewState().clearSearchText();
            return;
        }

        if (mType==RecordListFragment.TYPE_HISTORY) return;

        if (mRecordList ==null) return;

        for (int i = 0; i< mRecordList.size(); i++) {
            if (!mRecordList.get(i).isInFavorite()) {
                mRecordList.remove(i);
                i--;
            }
        }

        getViewState().setData(mRecordList);
        getViewState().notifyDataSetChanged();
        getViewState().showEmpty(mRecordList.size()==0);
    }

    public void singleClick(int i) {
        EventBus.getDefault().post(new SelectRecordEvent(mRecordList.get(i)));
    }

    public void longClick(int num) {


        /*AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Важное сообщение!")
                .setMessage("Покормите кота!")
                .setCancelable(false)
                .setNegativeButton("ОК, иду на кухню",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();*/

        getViewState().showOptionsDialog(num);
    }

    private void removeItem(int i) {

        Record record=mRecordList.get(i);
        mRecordList.remove(i);
        getViewState().setData(mRecordList);
        getViewState().notifyItemRemoved(i);

        if (mType==RecordListFragment.TYPE_FAVORITE) record.setInFavorite(false);
        else if (mType==RecordListFragment.TYPE_HISTORY) record.setInHistory(false);

        if (record.isInFavorite() || record.isInHistory())
            getDaoSession().getRecordDao().insertOrReplace(record);
        else getDaoSession().getRecordDao().delete(record);

        EventBus.getDefault().post(new RecordChangedEvent(record,
                mType==RecordListFragment.TYPE_HISTORY?RecordChangedEvent.SENDER_HISTORY:RecordChangedEvent.SENDER_FAVORITE));

        getViewState().showEmpty(mRecordList.size()==0);

        getViewState().updateClearButtonState();
    }

    public void removeClicked(int num) {
        removeItem(num);
    }

    public void clearClicked() {
        getViewState().showAlertDelete();
    }


    public void clearFirstStep() {

        Log.d(APP_TAG, "clearFirstStep start");

        AsyncSession asyncSession = getDaoSession().startAsyncSession();

        asyncSession.setListenerMainThread(new AsyncOperationListener() {
            @Override
            public void onAsyncOperationCompleted(AsyncOperation operation) {

                Log.d(APP_TAG, "half_first_step");

                AsyncSession asyncSession2 = getDaoSession().startAsyncSession();

                asyncSession2.setListenerMainThread(new AsyncOperationListener() {
                    @Override
                    public void onAsyncOperationCompleted(AsyncOperation operation) {

                        Log.d(APP_TAG, "first_step finished");

                        clearSecondStep();

                    }
                });

                asyncSession2.deleteInTx(Record.class, (List<Record>) operation.getResult());

            }
        });

        asyncSession.queryList(getDaoSession().getRecordDao().queryBuilder().where((mType==RecordListFragment.TYPE_FAVORITE)?
                RecordDao.Properties.InHistory.eq(false)
                :RecordDao.Properties.InFavorite.eq(false)).build());

    }

    private void clearSecondStep() {

        AsyncSession asyncSession = getDaoSession().startAsyncSession();

        asyncSession.setListenerMainThread(new AsyncOperationListener() {
            @Override
            public void onAsyncOperationCompleted(AsyncOperation operation) {

                List<Record> records = (List<Record>) operation.getResult();

                if (mType==RecordListFragment.TYPE_FAVORITE) {
                    for (Record record : records) {
                        record.setInFavorite(false);
                        record.setFavoriteTime(0);
                    }
                }
                else if (mType==RecordListFragment.TYPE_HISTORY) {
                    for (Record record : records) {
                        record.setInHistory(false);
                        record.setHistoryTime(0);
                    }
                }

                AsyncSession asyncSession2 = getDaoSession().startAsyncSession();

                asyncSession2.setListenerMainThread(new AsyncOperationListener() {
                    @Override
                    public void onAsyncOperationCompleted(AsyncOperation operation) {

                        mBusy=false;
                        getViewState().showLoading(false);
                        getViewState().showEmpty(true);

                        mRecordList=new ArrayList<>();
                        getViewState().setData(mRecordList);
                        getViewState().notifyDataSetChanged();

                        EventBus.getDefault().post(new FullReloadNeededEvent(
                                mType==RecordListFragment.TYPE_HISTORY?RecordChangedEvent.SENDER_HISTORY
                                        :RecordChangedEvent.SENDER_FAVORITE));

                        getViewState().updateClearButtonState();

                    }
                });

                asyncSession2.insertOrReplaceInTx(Record.class, records);

            }
        });

        asyncSession.queryList(getDaoSession().getRecordDao().queryBuilder().build());

    }

    public void clearPositive() {

        if (mBusy) return;

        mBusy=true;

        getViewState().showLoading(true);

        clearFirstStep();

        getViewState().updateClearButtonState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFullReloadNeededEvent(FullReloadNeededEvent event) {

        if ((event.getSender()==RecordChangedEvent.SENDER_HISTORY && mType==RecordListFragment.TYPE_HISTORY) ||
                (event.getSender()==RecordChangedEvent.SENDER_FAVORITE && mType==RecordListFragment.TYPE_FAVORITE)) return;

        loadAll();
        getViewState().scrollToPosition(0);

    }


    public boolean getClearButtonState() {

        Log.d(APP_TAG, "asked for clear buttn state, type="+mType+" , return="+ (!mBusy && mRecordList!=null && mRecordList.size()>0));

        return (!mBusy && mRecordList!=null && mRecordList.size()>0);
    }


    public void searchTextChanged(String text) {
        getViewState().showSearchClearButton(text.length()>0);

        if (text.length()==0) loadAll();
        else {
            searchFor(text);
        }
    }

    private void searchFor(String text) {

        getViewState().showLoading(true);
        mBusy=true;

        AsyncSession asyncSession = getDaoSession().startAsyncSession();

        mReqNum++;
        final int curReqNum=mReqNum;

        asyncSession.setListenerMainThread(new AsyncOperationListener() {
            @Override
            public void onAsyncOperationCompleted(AsyncOperation operation) {

                if (curReqNum!=mReqNum) return;

                mIsSearch=true;

                getViewState().showLoading(false);

                mRecordList= (List<Record>) operation.getResult();

                getViewState().showEmpty(mRecordList.size()==0);

                getViewState().setData(mRecordList);
                getViewState().notifyDataSetChanged();

                mBusy=false;

                getViewState().updateClearButtonState();

            }
        });

        QueryBuilder qb = getDaoSession().getRecordDao().queryBuilder();

        asyncSession.queryList(qb.where(qb.and((mType==RecordListFragment.TYPE_HISTORY)?
                        RecordDao.Properties.InHistory.eq(true)
                        :RecordDao.Properties.InFavorite.eq(true),
                        new WhereCondition.PropertyCondition(RecordDao.Properties.LowText, " LIKE '%"+text.toLowerCase()+"%'"))).orderDesc(
                        (mType==RecordListFragment.TYPE_HISTORY)?RecordDao.Properties.HistoryTime:RecordDao.Properties.FavoriteTime)
                        .build());


    }

    public void clearSearchClicked() {

        getViewState().clearSearchText();
    }
}