package ru.dedoxyribose.yandexschooltest;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.entity.Example;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.SupportedLangs;
import ru.dedoxyribose.yandexschooltest.model.entity.Translation;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;

import static junit.framework.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GsonTests {

    Gson gson;

    @Before
    public void before() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Word.class, new Word.WordConverter())
                .registerTypeAdapter(Def.class, new Def.DefConverter())
                .registerTypeAdapter(Example.class, new Example.ExampleConverter())
                .registerTypeAdapter(Record.class, new Record.RecordConverter())
                .registerTypeAdapter(Translation.class, new Translation.TranslationConverter())
                .registerTypeAdapter(SupportedLangs.class, new SupportedLangs.SupportedLangsConverter())
                .create();
    }

    @Test
    public void parseApple_isCorrect() throws Exception {

        String str="{\"head\":{},\"def\":[{\"text\":\"яблоко\",\"pos\":\"noun\",\"gen\":\"ср\",\"an" +
                "m\":\"неодуш\",\"tr\":[{\"text\":\"apple\",\"pos\":\"noun\",\"ex\":[{\"text\":\"зел" +
                "еное яблоко\",\"tr\":[{\"text\":\"green apple\"}]}]}]}]}";

        Record record=gson.fromJson(str, Record.class);

        assertEquals(record.getDefs().size(), 1);
        assertEquals(record.getDefs().get(0).getWord().getText(), "яблоко");
        assertEquals(record.getDefs().get(0).getWord().getPos(), "noun");
        assertEquals(record.getDefs().get(0).getWord().getGen(), "ср");
        assertEquals(record.getDefs().get(0).getTranslations().size(), 1);
        assertEquals(record.getDefs().get(0).getTranslations().get(0).getWord().getText(), "apple");
        assertEquals(record.getDefs().get(0).getTranslations().get(0).getWord().getPos(), "noun");
        assertEquals(record.getDefs().get(0).getTranslations().get(0).getExamples().size(), 1);
        assertEquals(record.getDefs().get(0).getTranslations().get(0).getExamples().get(0).getWord().getText(), "зеленое яблоко");
        assertEquals(record.getDefs().get(0).getTranslations().get(0).getExamples().get(0).getTranslations().size(), 1);
        assertEquals(record.getDefs().get(0).getTranslations().get(0).getExamples().get(0).getTranslations().get(0).getText(), "green apple");

    }
}