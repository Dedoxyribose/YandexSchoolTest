package ru.dedoxyribose.yandexschooltest.ui.recordlist;

import android.content.Intent;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;

/**
 * Created by Ryan on 27.02.2017.
 */
public interface RecordListView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setData(List<Record> list);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void notifyDataSetChanged();

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void notifyItemChanged(int i);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void notifyItemRemoved(int i);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void notifyItemInserted(int i);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void showLoading(boolean show);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showOptionsDialog(int i);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAlertDelete();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showEmpty(boolean show);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void scrollToPosition(int i);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void updateClearButtonState();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSearchClearButton(boolean show);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void clearSearchText();
}


