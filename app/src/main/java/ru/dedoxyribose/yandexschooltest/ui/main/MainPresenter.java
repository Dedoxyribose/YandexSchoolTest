package ru.dedoxyribose.yandexschooltest.ui.main;


import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class MainPresenter extends StandardMvpPresenter<MainView>{

    @Override
    public void attachView(MainView view) {
        super.attachView(view);

        Log.d(TAG, "attachView");
    }
}