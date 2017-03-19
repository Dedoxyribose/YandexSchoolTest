package ru.dedoxyribose.yandexschooltest.util;

import android.content.Context;
import android.support.annotation.StringRes;

import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.DefTitle;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ExItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.TrItem;

/**
 * Created by Ryan on 16.03.2017.
 */
public class Utils {

    public static String [] speakableLangs = {
            "ru", "en", "ua", "tr"
    };

    public static String [] speakableLangsCodes = {
            "en-EN", "ru-RU", "tr-TR", "uk-UA"
    };


    public static Record deserializeRecord(boolean dict, JSONObject jsonObject, String initialText, String direction) {
        Record record = new Record();

        record=Singletone.getGson().fromJson(jsonObject.toString(), Record.class);

        record.setDirection(direction);
        if (!dict) {
            record.setText(initialText);
        }

        return record;
    }

    public static List<ListItem> generateViewModelList(Record record) {

        ArrayList<ListItem> list = new ArrayList<>();

        if (record.getDefs()==null) return list;

        for (Def def : record.getDefs()) {
            if (def.getWord()!=null) {
                DefTitle defTitle = new DefTitle(def.getWord());
                list.add(defTitle);
            }

            int num=1;
            for (Translation translation : def.getTranslations()){
                List<Word> words= new ArrayList<>();
                words.add(translation.getWord());
                for (Word word : translation.getSyns()) words.add(word);

                List<Word> means= new ArrayList<>();
                for (Word word : translation.getMeans()) means.add(word);

                TrItem trItem = new TrItem(def.getTranslations().size()>1?String.valueOf(num):"", words, means);
                list.add(trItem);
                num++;

                for (Example example: translation.getExamples()) {
                    Word word=new Word();
                    if (example.getWord()!=null) word=example.getWord();
                    String text=word.getText()+" - ";

                    if (example.getTranslations()!=null) {

                        int trNum=0;
                        for (Word exTr:example.getTranslations()) {

                            if (trNum>0) text+=", ";
                            text+=exTr.getText();
                            trNum++;

                        }
                    }

                    ExItem exItem = new ExItem(text);
                    list.add(exItem);

                }
            }


        }

        return list;

    }

    public static String getColoredSpanned(String text, String color) {
        String input = "<font color='" + color + "'>" + text + "</font>";
        return input;
    }


    /*public static String getPosWord(Context context, String what) {
        int res=0;
        switch (what) {
            case "noun": res=R.string.noun; break;
            case "adjective": res=R.string.adjective; break;
            case "verb": res=R.string.verb; break;
            case "adverb": res=R.string.adverb; break;
            case "participle": res=R.string.participle; break;
            case "particle": res=R.string.particle; break;
            case "conjunction": res=R.string.conjunction; break;
            case "interjection": res=R.string.interjection; break;
            case "preposition": res=R.string.preposition; break;
            case "pronoun": res=R.string.pronoun; break;
            case "numeral": res=R.string.numeral; break;
        }

        if (res==0) return null;
        else return context.getString(res);
    }*/

    public static int extractErrorCode(Response response) {
        if (response.isSuccessful()) return 200;
        else{
            try {
                String text=response.errorBody().string();
                JSONObject jsonObject=new JSONObject(text);
                if (jsonObject.has("code"))
                    return jsonObject.optInt("code");
                else return -1;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    public static String getErrorTextForCode(Context context, int errorCode) {

        switch (errorCode){
            case 401: return context.getString(R.string.BadKey);
            case 402: return context.getString(R.string.BlockedKey);
            case 403: return context.getString(R.string.TooManyReqs);
            case 404: return context.getString(R.string.TooBigText);
            case 422: return context.getString(R.string.TextCantBeTranslated);
            case 501: return context.getString(R.string.DirectionNotSupported);
        }
        return context.getString(R.string.UnknownError);

    }

    public static Lang getLangByCode(String code, List<Lang> langs){
        for (Lang lang:langs){
            if (lang.getCode().equals(code))
                return lang;
        }
        return null;
    }

    public static boolean isEmpty(String text){
        if (text==null) return true;
        if (text.length()==0) return true;
        return false;
    }

    public static String getSpeechCodeForLang(String code) {
        for (int i=0; i<speakableLangs.length; i++) {
            if (speakableLangs[i].equals(code))
                return speakableLangsCodes[i];
        }
        return null;
    }
}
