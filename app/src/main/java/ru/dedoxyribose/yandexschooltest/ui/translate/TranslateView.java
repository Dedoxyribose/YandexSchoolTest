package ru.dedoxyribose.yandexschooltest.ui.translate;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;

/**
 * Created by Ryan on 27.02.2017.
 */
public interface TranslateView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setDefData(List<ListItem> list);

}
