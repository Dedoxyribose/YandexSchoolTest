package ru.dedoxyribose.yandexschooltest.ui.chooselang;


import android.app.Activity;
import android.content.Intent;

import com.arellomobile.mvp.InjectViewState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.Singletone;
import ru.dedoxyribose.yandexschooltest.util.Utils;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class ChooseLangPresenter extends StandardMvpPresenter<ChooseLangView>{

    private boolean mIsLangFrom =true;
    private String mCurLangCode="ru";

    public void setIntent(Intent intent) {
        if (intent.getIntExtra(ChooseLangActivity.ARG_LANG_POSITION, 0)==ChooseLangActivity.LANG_POSITION_FROM)
            mIsLangFrom =true;
        else mIsLangFrom =false;

        mCurLangCode=intent.getStringExtra(ChooseLangActivity.ARG_CUR_LANG);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        getViewState().setTitle(getContext().getString(mIsLangFrom ?R.string.LangOfText:R.string.LangOfTrsl));

        List<Lang> allLangs=Singletone.getInstance().getLangs();
        List<Lang> sortedLangs = new ArrayList<>(allLangs);

        Collections.sort(sortedLangs, new Comparator<Lang>() {
            @Override
            public int compare(Lang l1, Lang l2) {
                if (l1.getAskedTime()==l2.getAskedTime()) return 0;
                else return l1.getAskedTime()<l2.getAskedTime()?1:-1;
            }
        });

        List<Lang> recentLangs=new ArrayList<>();

        for (int i=0; i<5; i++)
        {
            //TODO вернуть if (sortedLangs.get(i).getAskedTime()>0)
                recentLangs.add(sortedLangs.get(i));
        }

        int curLangPos=0;
        for (Lang lang : allLangs){
            if (lang.getCode().equals(mCurLangCode))
                break;
            curLangPos++;
        }

        getViewState().setData(mIsLangFrom, recentLangs, allLangs, curLangPos);

    }

    public void backClicked() {
        getViewState().finishWithIntent(Activity.RESULT_CANCELED, null);
    }

    public void langClicked(Lang lang) {
        String code=(lang==null)?"00":lang.getCode();
        Intent intent=new Intent();
        intent.putExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE, code);
        getViewState().finishWithIntent(Activity.RESULT_OK, intent);
    }
}