package ru.dedoxyribose.yandexschooltest.ui.chooselang;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardMvpPresenter;
import ru.dedoxyribose.yandexschooltest.util.AppSession;

/**
 * Created by Ryan on 27.02.2017.
 */
@InjectViewState
public class ChooseLangPresenter extends StandardMvpPresenter<ChooseLangView>{

    private boolean mIsLangFrom =true; //язык текста или язык перевода
    private String mCurLangCode; //текущий язык

    private boolean mFirstTime=true;

    public void setIntent(Intent intent) {
        if (intent.getIntExtra(ChooseLangActivity.ARG_LANG_POSITION, 0)==ChooseLangActivity.LANG_POSITION_FROM)
            mIsLangFrom =true;
        else mIsLangFrom =false;

        mCurLangCode=intent.getStringExtra(ChooseLangActivity.ARG_CUR_LANG);

        //загружаем данные только если это не пересозданное View, а самое первое.
        if (mFirstTime) {

            mFirstTime=false;

            getViewState().setTitle(getContext().getString(mIsLangFrom ?R.string.LangOfText:R.string.LangOfTrsl));

            List<Lang> allLangs= getAppSession().getLangs();
            List<Lang> sortedLangs = new ArrayList<>(allLangs);

            //сортируем языки по времени последнего вызова
            Collections.sort(sortedLangs, new Comparator<Lang>() {
                @Override
                public int compare(Lang l1, Lang l2) {
                    if (l1.getAskedTime()==l2.getAskedTime()) return 0;
                    else return l1.getAskedTime()<l2.getAskedTime()?1:-1;
                }
            });

            List<Lang> recentLangs=new ArrayList<>();

            //и берём 5 последних
            for (int i=0; i<5; i++)
            {
                if (sortedLangs.get(i).getAskedTime()>0)
                    recentLangs.add(sortedLangs.get(i));
            }

            //обозначаем текущий выбранный язык
            int curLangPos=0;
            for (Lang lang : allLangs){
                if (lang.getCode().equals(mCurLangCode))
                    break;
                curLangPos++;
            }

            //отправляем во View
            getViewState().setData(mIsLangFrom, recentLangs, allLangs, curLangPos);
        }
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();


        Log.d(APP_TAG, TAG+" onFirstViewAttach");


    }

    public void backClicked() {
        getViewState().finishWithIntent(Activity.RESULT_CANCELED, null);
    }

    public void langClicked(Lang lang) {

        //клик по языку, возвращаем ответ в главное активити

        String code=(lang==null)?"00":lang.getCode();
        Intent intent=new Intent();
        intent.putExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_CODE, code);
        intent.putExtra(ChooseLangActivity.RES_ARG_CHOSEN_LANG_POS,
                mIsLangFrom?ChooseLangActivity.LANG_POSITION_FROM:ChooseLangActivity.LANG_POSITION_TO);
        getViewState().finishWithIntent(Activity.RESULT_OK, intent);
    }
}