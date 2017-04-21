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

    /**
     * Максимальный объём БД истории и избранного
     */
    public static final int MAX_CAPACITY=10000;

    private List<Record> mRecordList=new ArrayList<>();

    private int mType;  //тип фрагмента - история или избранное.

    private boolean mIsSearch=false;
    private boolean mBusy=false;  //"занятость фрагмента"; если true, значит выполняется какая-то операция и все события игнорируем

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


        //загружаем асинхронно все записи для избранного или для истории

        getViewState().showLoading(true);
        mBusy=true;

        AsyncSession asyncSession = getDaoSession().startAsyncSession();

        mReqNum++;
        final int curReqNum=mReqNum;

        asyncSession.setListenerMainThread(new AsyncOperationListener() {
            @Override
            public void onAsyncOperationCompleted(AsyncOperation operation) {

                Log.d(APP_TAG, TAG+" loadAll async completed");

                //если запрос устарел, бросаем его
                if (curReqNum!=mReqNum) return;

                mIsSearch=false;

                getViewState().showLoading(false);

                List<Record> list=(List<Record>) operation.getResult();


                //в случае переполнения истории необходимо создать два списка записей
                //поскольку записи избранного и истории пересекаются;
                //если, например, элемент удаляется из истории и не был в избранном, то удаляем его совсем
                //если элемент был и там и там, то просто сбрасываем один из его флагов
                List<Record> toChange=new ArrayList<>();
                List<Record> toDelete=new ArrayList<>();

                mRecordList.clear();

                for (int i=0; i<list.size()-MAX_CAPACITY; i++) {
                    if (list.get(i).isInFavorite()) {
                        list.get(i).setInHistory(false);
                        toChange.add(list.get(i));
                    }
                    else {
                        toDelete.add(list.get(i));
                    }
                }

                //удаляем и меняем

                getDaoSession().getRecordDao().insertOrReplaceInTx(toChange);
                getDaoSession().getRecordDao().deleteInTx(toDelete);


                //копируем в поле презентера всё, что осталось
                //при этом переворачиваем список, в целях повышения производительности
                //(теперь в дальнейшем элементы будут добавляться в конец)
                for (int i=(list.size()-MAX_CAPACITY>-1)?(list.size()-MAX_CAPACITY):0; i<list.size(); i++) {
                    mRecordList.add(list.get(i));
                }

                getViewState().showEmpty(mRecordList.size()==0);

                getViewState().setData(mRecordList);
                getViewState().notifyDataSetChanged();

                mBusy=false;

                getViewState().updateClearButtonState();

            }
        });

        QueryBuilder qb = getDaoSession().getRecordDao().queryBuilder();

        asyncSession.queryList(qb.where(
                (mType==RecordListFragment.TYPE_HISTORY)?
                        RecordDao.Properties.InHistory.eq(true)
                        :RecordDao.Properties.InFavorite.eq(true))
                .orderAsc((mType==RecordListFragment.TYPE_HISTORY)?RecordDao.Properties.HistoryTime:RecordDao.Properties.FavoriteTime)
                .build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void iconClicked(int i) {

        //кликнули по иконке закладки (избранного, то есть)

        Record record = mRecordList.get(i);

        //меняем статус записи на противоположный
        if (!record.isInFavorite()) {
            record.setInFavorite(true);
            record.setFavoriteTime(System.currentTimeMillis());
        }
        else {
            record.setInFavorite(false);
        }

        //обновляем БД
        if (record.isInFavorite() || record.isInHistory())
            getDaoSession().insertOrReplace(record);
        else getDaoSession().delete(record);

        getViewState().setData(mRecordList);
        getViewState().notifyItemChanged(mRecordList.size()-i-1);

        //уведомляем по шине, что запись поменялась;
        EventBus.getDefault().post(new RecordChangedEvent(record,
                mType==RecordListFragment.TYPE_HISTORY?RecordChangedEvent.SENDER_HISTORY:RecordChangedEvent.SENDER_FAVORITE));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordChangedEvent(RecordChangedEvent event) {

        Log.d(APP_TAG, TAG+"onRecordChangedEvent");

        //Event о том, что где-то поменяли какую-то запись

        if (mBusy) return;

        if (mIsSearch) return;

        //если данный фрагмент его и отправлял, то игнорируем
        if ((event.getSender()==RecordChangedEvent.SENDER_HISTORY && mType==RecordListFragment.TYPE_HISTORY) ||
                (event.getSender()==RecordChangedEvent.SENDER_FAVORITE && mType==RecordListFragment.TYPE_FAVORITE)) return;

        boolean found=false;

        //ищем, встречается эта запись в списке записей этого фрагмента
        for (int i = 0; i< mRecordList.size(); i++) {
            Record record= mRecordList.get(i);

            if (record.getId().equals(event.getRecord().getId())) {

                /*Log.d(APP_TAG, "record.ht="+record.getHistoryTime());
                Log.d(APP_TAG, "event.record.ht="+event.getRecord().getHistoryTime());*/

                //если обновилось время записи в истории, её надо сначала удалить, а потом воткнуть на нужное место; удаляем
                if (record.getHistoryTime()!=event.getRecord().getHistoryTime() && mType==RecordListFragment.TYPE_HISTORY) {

                    /*Log.d(APP_TAG, "gonna replace");*/

                    mRecordList.remove(i);
                    getViewState().notifyItemRemoved(mRecordList.size()-i);
                    getViewState().setData(mRecordList);
                    break;
                }

                found=true;

                //обновляем данные записи
                if (mType==RecordListFragment.TYPE_HISTORY) {
                    if (!event.getRecord().isInHistory()) {
                        mRecordList.remove(i);
                        getViewState().setData(mRecordList);
                        getViewState().notifyDataSetChanged();
                    }
                    else {
                        mRecordList.set(i, event.getRecord());
                        getViewState().setData(mRecordList);
                        getViewState().notifyItemChanged(mRecordList.size()-i-1);
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
                        getViewState().notifyItemChanged(mRecordList.size()-i-1);
                    }
                }

                break;
            }
        }

        //если элемент не был найден (либо был удалён из-за устаревания даты), ищем куда его воткнуть;
        //код дублируется для разных типов фрагмента (избранное/история), т.к. так проще и наглядней

        if (!found && event.getRecord().isInFavorite() && mType==RecordListFragment.TYPE_FAVORITE) {

            boolean inserted=false;
            for (int i = mRecordList.size()-1; i>=0; i--) {
                if (mRecordList.get(i).getFavoriteTime()<event.getRecord().getFavoriteTime()) {
                    mRecordList.add(i+1, event.getRecord());
                    inserted=true;
                    break;
                }
            }

            if (!inserted) {
                mRecordList.add(0, event.getRecord());
            }

            getViewState().setData(mRecordList);
            getViewState().notifyDataSetChanged();

        }

        if (!found && event.getRecord().isInHistory() && mType==RecordListFragment.TYPE_HISTORY) {

            boolean inserted=false;
            for (int i = mRecordList.size()-1; i>=0; i--) {
                if (mRecordList.get(i).getHistoryTime()<event.getRecord().getHistoryTime()) {
                    mRecordList.add(i+1, event.getRecord());
                    inserted=true;
                    break;
                }
            }

            if (!inserted) {
                mRecordList.add(0, event.getRecord());
            }

            getViewState().setData(mRecordList);
            getViewState().notifyDataSetChanged();

        }

        getViewState().showEmpty(mRecordList.size()==0);

    }


    public void update() {

        Log.d(APP_TAG, TAG+"update()");

        //обновить список

        //если был поиск, убираем его
        if (mIsSearch) {
            loadAll();
            getViewState().clearSearchText();
            return;
        }

        //обновить, удалить элементы, которых уже не должно быть в списке
        //например элементы избранного, у которых уже сняли "закладку" вручную

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
        //клик по записи, уведомляем главный экран, что нужно перейти к данной записи
        EventBus.getDefault().post(new SelectRecordEvent(mRecordList.get(i)));
    }

    public void longClick(int num) {
        //длинный клик, показываем диалог с единственной опцией "Удалить"
        getViewState().showOptionsDialog(num);
    }

    private void removeItem(int i) {

        Log.d(APP_TAG, "removeItem " + i);

        //удалить элемент и запись из БД.
        //либо удаляем полностью, либо сбрасываем один из флагов, если запись ещё нужна в
        //соседнем фрагменте (т.к. одна запись используется и для истории и для избранного)

        Record record=mRecordList.get(i);
        mRecordList.remove(i);
        getViewState().notifyItemRemoved(mRecordList.size()-i);
        getViewState().setData(mRecordList);

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

        //тут получаем асинхронно все записи, которые находятся ТОЛЬКО в избранном или ТОЛЬКО в истории (в зависимости от типа фрагмента)

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

                //удаляем их

                asyncSession2.deleteInTx(Record.class, (List<Record>) operation.getResult());

            }
        });

        asyncSession.queryList(getDaoSession().getRecordDao().queryBuilder().where((mType==RecordListFragment.TYPE_FAVORITE)?
                RecordDao.Properties.InHistory.eq(false)
                :RecordDao.Properties.InFavorite.eq(false)).build());

    }

    private void clearSecondStep() {

        AsyncSession asyncSession = getDaoSession().startAsyncSession();

        //тут получаем остальные записи для текущего типа (т.е. те, которые и в избранном и в истории)

        asyncSession.setListenerMainThread(new AsyncOperationListener() {
            @Override
            public void onAsyncOperationCompleted(AsyncOperation operation) {

                List<Record> records = (List<Record>) operation.getResult();

                //и сбрасываем у них один из флагов (флаг избранного или флаг истории)

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

                        //завершаем процесс очистки

                        mBusy=false;
                        getViewState().showLoading(false);
                        getViewState().showEmpty(true);

                        mRecordList=new ArrayList<>();
                        getViewState().setData(mRecordList);
                        getViewState().notifyDataSetChanged();

                        //уведомляем слушателей, что все данные надо обновить
                        EventBus.getDefault().post(new FullReloadNeededEvent(
                                mType==RecordListFragment.TYPE_HISTORY?RecordChangedEvent.SENDER_HISTORY
                                        :RecordChangedEvent.SENDER_FAVORITE));

                        getViewState().updateClearButtonState();

                    }
                });

                //обновляем БД
                asyncSession2.insertOrReplaceInTx(Record.class, records);

            }
        });

        asyncSession.queryList(getDaoSession().getRecordDao().queryBuilder().build());

    }

    public void clearPositive() {

        if (mBusy) return;

        mBusy=true;

        //кликнули на очистку. Очистка выполняется в несколько этапов (clearFirstStep, clearSecondStep)

        getViewState().showLoading(true);

        clearFirstStep();

        getViewState().updateClearButtonState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFullReloadNeededEvent(FullReloadNeededEvent event) {

        if ((event.getSender()==RecordChangedEvent.SENDER_HISTORY && mType==RecordListFragment.TYPE_HISTORY) ||
                (event.getSender()==RecordChangedEvent.SENDER_FAVORITE && mType==RecordListFragment.TYPE_FAVORITE)) return;

        Log.d(APP_TAG, TAG+"onFullReloadNeededEvent()");
        loadAll();
        getViewState().scrollToPosition(0);

    }


    public boolean getClearButtonState() {

        Log.d(APP_TAG, "asked for clear buttn state, type="+mType+" , return="+ (!mBusy && mRecordList!=null && mRecordList.size()>0));

        return (!mBusy && mRecordList!=null && mRecordList.size()>0);
    }


    public void searchTextChanged(String text) {

        Log.d(APP_TAG, TAG+"searchTextChanged()");

        getViewState().showSearchClearButton(text.length()>0);

        if (text.length()==0) loadAll();
        else {
            searchFor(text);
        }
    }

    private void searchFor(String text) {

        //поиск

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