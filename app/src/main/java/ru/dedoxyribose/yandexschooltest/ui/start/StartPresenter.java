package ru.dedoxyribose.yandexschooltest.ui.start;


import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.ui.main.MainView;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.dedoxyribose.yandexschooltest.util.Utils;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class StartPresenter extends StandardMvpPresenter<StartView>{

    @Override
    public void attachView(StartView view) {
        super.attachView(view);

        Log.d(TAG, "attachView");
    }

    public void repeatClicked() {
        refreshLangs();
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        List<Lang> langs=getDaoSession().getLangDao().loadAll();

        if (langs.size()==0){
            refreshLangs();
        }
        else {

            Collections.sort(langs, new Comparator<Lang>() {
                @Override
                public int compare(Lang lang, Lang t1) {
                    return lang.getName().compareTo(t1.getName());
                }
            });

            Singletone.getInstance().setLangs(langs);
            getViewState().proceedToMain();
        }
    }

    private void refreshLangs() {

        getViewState().showLoading(true);
        getViewState().showError(false, null);
        RetrofitHelper.getServerApi().getLangs(getContext().getString(R.string.trans_key), "ru").enqueue(new Callback<SupportedLangs>() {
            @Override
            public void onResponse(Call<SupportedLangs> call, Response<SupportedLangs> response) {
                if (response.isSuccessful()){

                    List<Lang> langs=new ArrayList<>();

                    for (Lang lang: response.body().getLangs()) {
                        getDaoSession().getLangDao().insertOrReplace(lang);
                        langs.add(lang);
                    }

                    Collections.sort(langs, new Comparator<Lang>() {
                        @Override
                        public int compare(Lang lang, Lang t1) {
                            return lang.getName().compareTo(t1.getName());
                        }
                    });

                    Singletone.getInstance().setLangs(langs);
                    getViewState().proceedToMain();
                }
                else {
                    getViewState().showLoading(false);
                    getViewState().showError(true, Utils.getErrorTextForCode(getContext(), Utils.extractErrorCode(response)));
                }
            }

            @Override
            public void onFailure(Call<SupportedLangs> call, Throwable t) {
                getViewState().showLoading(false);

                t.printStackTrace();

                if (t instanceof IOException)
                    getViewState().showError(true, getContext().getString(R.string.ConnectionError));
                else getViewState().showError(true, getContext().getString(R.string.UnknownError));
            }
        });

    }
}