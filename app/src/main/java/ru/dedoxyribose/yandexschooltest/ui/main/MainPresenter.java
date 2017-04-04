package ru.dedoxyribose.yandexschooltest.ui.main;


import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.SimpleCountingIdlingResource;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class MainPresenter extends StandardMvpPresenter<MainView>{

    private SimpleCountingIdlingResource mIdlingResource = new SimpleCountingIdlingResource("main");

    @Override
    public void attachView(MainView view) {
        super.attachView(view);

        Log.d(TAG, "attachView");
    }

    public SimpleCountingIdlingResource getIdlingResource() {
        return mIdlingResource;
    }

    public void incrementIdlingResource() {
        mIdlingResource.increment();
    }

    public void decrementIdlingResource() {
        mIdlingResource.decrement();
    }
}