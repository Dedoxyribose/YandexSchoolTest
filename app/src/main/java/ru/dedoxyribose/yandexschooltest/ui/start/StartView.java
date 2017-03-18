package ru.dedoxyribose.yandexschooltest.ui.start;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

/**
 * Created by Ryan on 27.02.2017.
 */
public interface StartView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void showError(boolean show, String text);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void showLoading(boolean show);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void proceedToMain();
}
