package ru.dedoxyribose.yandexschooltest.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;

/**
 * Created by Ryan on 14.04.2017.
 */

/**
 * Класс, подготавливающий объект Gson, регистрирующий адаптеры преобразования
 */
public class GsonHelper {

    private GsonHelper(){}

    private static Gson sGson;

    public static Gson getGson() {
        if (sGson==null) {
            sGson = new GsonBuilder()
                    .registerTypeAdapter(Word.class, new Word.WordConverter())
                    .registerTypeAdapter(Def.class, new Def.DefConverter())
                    .registerTypeAdapter(Example.class, new Example.ExampleConverter())
                    .registerTypeAdapter(Record.class, new Record.RecordConverter())
                    .registerTypeAdapter(Translation.class, new Translation.TranslationConverter())
                    .registerTypeAdapter(SupportedLangs.class, new SupportedLangs.SupportedLangsConverter())
                    .create();
        }
        return sGson;
    }
}
