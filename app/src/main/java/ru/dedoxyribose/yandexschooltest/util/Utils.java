package ru.dedoxyribose.yandexschooltest.util;

import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
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

    Record deserializeRecord(boolean dict, JSONObject jsonObject, String initialText, String direction) {
        Record record = new Record();

        record=Singletone.getGson().fromJson(jsonObject.toString(), Record.class);

        record.setDirection(direction);
        if (!dict) {
            record.setText(initialText);
        }

        return record;
    }

    List<ListItem> generateViewModelList(Record record) {

        ArrayList<ListItem> list = new ArrayList<>();

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
                for (Word word : translation.getMeans()) words.add(word);

                TrItem trItem = new TrItem(def.getTranslations().size()>1?String.valueOf(num):"", words, means);
                num++;

                for (Example example: translation.getExamples()) {
                    Word word=new Word();
                    if (example.getWord()!=null) word=example.getWord();
                    String text=word+" - ";

                    if (example.getTranslations()!=null) {

                        int trNum=0;
                        for (Word exTr:example.getTranslations()) {

                            if (trNum>0) text+=", ";
                            text+=exTr;
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

}
