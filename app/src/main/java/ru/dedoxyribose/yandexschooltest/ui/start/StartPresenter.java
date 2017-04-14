package ru.dedoxyribose.yandexschooltest.ui.start;


import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.AppSession;
import ru.dedoxyribose.yandexschooltest.util.RetrofitHelper;
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

        List<Lang> langs= getAppSession().getLangs();

        if (langs.size()==0 || !getAppSession().getLocale().equals(Locale.getDefault().getLanguage())){
            refreshLangs();
        }
        else {

            Collections.sort(langs, new Comparator<Lang>() {
                @Override
                public int compare(Lang lang, Lang t1) {
                    return lang.getName().compareTo(t1.getName());
                }
            });

            getAppSession().setLangs(langs);
            getViewState().proceedToMain();
        }
    }

    private void loadLangsForLocale(final String ui) {

        getViewState().showLoading(true);
        getViewState().showError(false, null);
        getServerApi().getLangs(getContext().getString(R.string.trans_key), ui).enqueue(new Callback<SupportedLangs>() {
            @Override
            public void onResponse(Call<SupportedLangs> call, Response<SupportedLangs> response) {
                if (response.isSuccessful()){

                    List<Lang> langs=new ArrayList<>();

                    if (response.body().getLangs()==null || response.body().getLangs().size()==0) {
                        loadLangsForLocale("en");
                        return;
                    }

                    for (Lang lang: response.body().getLangs()) {
                        langs.add(lang);
                    }

                    getDaoSession().getLangDao().deleteAll();
                    getDaoSession().getLangDao().insertOrReplaceInTx(langs);

                    Collections.sort(langs, new Comparator<Lang>() {
                        @Override
                        public int compare(Lang lang, Lang t1) {
                            return lang.getName().compareTo(t1.getName());
                        }
                    });

                    getAppSession().setLangs(langs);

                    getAppSession().setLocale(ui);
                    getAppSession().saveSettings();

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

    private void refreshLangs() {

        String ui=Locale.getDefault().getLanguage();

        loadLangsForLocale(ui);

    }
}