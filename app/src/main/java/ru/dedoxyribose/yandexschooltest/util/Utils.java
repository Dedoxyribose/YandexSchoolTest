package ru.dedoxyribose.yandexschooltest.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

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

/**
 * Static-методы и константы, используемые в приложении
 */
public class Utils {

    /**
     * Языки, поддерживаемые речевыми технологиями Яндекса
     */
    public static String [] speakableLangs = {
            "ru", "en", "uk", "tr"
    };

    /**
     * Полные коды языков, поддерживаемых речевыми технологиями Яндекса
     */
    public static String [] speakableLangsCodes = {
            "ru-RU", "en-EN", "uk-UA", "tr-TR",
    };

    /**
     * Преобразовать json-ответ в готовый объект Record.
     * @param dict к словарю ли был запрос (или к переводчику)
     * @param jsonObject объект ответа
     * @param initialText текст, отправленный серверу в кач-ве запроса
     * @param direction направление перевода
     * @return готовая запись для сохранения в БД
     */
    //не используется в текущей версии приложения
    public static Record deserializeRecord(boolean dict, JSONObject jsonObject, String initialText, String direction) {
        Record record = new Record();

        record= GsonHelper.getGson().fromJson(jsonObject.toString(), Record.class);

        record.setDirection(direction);
        if (!dict) {
            record.setText(initialText);
        }

        return record;
    }

    /**
     *  Функция, конвертирующая словарную статью в формате List<Def> в модель представления -
     *  линейный список объектов, наследующих model.viewmodel.ListItem
     */
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

    /**
     * Добавить спан-цвет к тексту
     */

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

    /**
     * извлечь код ошибки из body из Response
     * @param response ответ сервера
     * @return код ошибки
     */
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

    /**
     * Получить нужный текст ошибки для кода ошибки
     * @param context контекст
     * @param errorCode код ошибки
     * @return текст для вывода на экран
     */
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

    /**
     * Получить объект языка из списка языков по его коду
     * @param code код языка
     * @param langs список объектов
     * @return объект языка
     */
    public static Lang getLangByCode(String code, List<Lang> langs){
        for (Lang lang:langs){
            if (lang.getCode().equals(code))
                return lang;
        }
        return null;
    }

    /**
     * Является ли строка пустой или null
     */
    public static boolean isEmpty(String text){
        if (text==null) return true;
        if (text.length()==0) return true;
        return false;
    }

    /**
     * Получить полный код языка для двухсимвольного кода
     * @param code двухсимвольный код
     * @return полный код
     */
    public static String getSpeechCodeForLang(String code) {
        for (int i=0; i<speakableLangs.length; i++) {
            if (speakableLangs[i].equals(code))
                return speakableLangsCodes[i];
        }
        return null;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static String getAppTag() {
        return "YSTAPP";
    }
}
