package ru.dedoxyribose.yandexschooltest.ui.translate;

import android.content.Intent;

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

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void setText(String text);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void showLoading(boolean show);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setMainText(String text);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void showError(boolean showError, String title, String text, boolean showRepeat);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void openChooseLang(Intent intent);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void showLangs(String from, String to, boolean determined);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void hideSoftKeyboard();

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void clearTextFocus();

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setRecognitionEnabled(boolean enabled);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void startRecognition(Intent intent);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setTextSpeechStatus(boolean visible, boolean enabled, boolean loading);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setTranslateSpeechStatus(boolean enabled, boolean loading);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void startFullscreen(Intent intent);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void showToast(String text);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void share(String text);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setTranslationButtonsEnabled(boolean enabled);

    @StateStrategyType(AddToEndSingleStrategy.class)
    public void setFavoriteOn(boolean on);

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void toFailActivity();

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void incrementIdling();

    @StateStrategyType(OneExecutionStateStrategy.class)
    public void decrementIdling();

}
