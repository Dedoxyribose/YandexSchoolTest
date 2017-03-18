package ru.dedoxyribose.yandexschooltest.ui.chooselang;

import android.content.Intent;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;

/**
 * Created by Ryan on 27.02.2017.
 */
public interface ChooseLangView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setTitle(String text);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void finishWithIntent(int code, Intent intent);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setData(boolean showDetermineLang, List<Lang> recentLangs, List<Lang> allLangs, int curLangPos);

}
