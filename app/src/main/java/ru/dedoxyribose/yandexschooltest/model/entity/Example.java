package ru.dedoxyribose.yandexschooltest.model.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 16.03.2017.
 */
public class Example {

    private Word word;

    private List<Word> translations = new ArrayList<>();


    public static class ExampleConverter implements JsonDeserializer<Example> {
        @Override
        public Example deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JSONObject response = null;
            try {
                response = new JSONObject(json.getAsJsonObject().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            Example example=new Example();

            Word word=context.deserialize(json.getAsJsonObject(), Word.class);
            example.setWord(word);

            if (response.optJSONArray("tr")!=null) {
                List<Word> translations=context.deserialize(json.getAsJsonObject().get("tr").getAsJsonArray(), new TypeToken<List<Word>>(){}.getType());
                example.setTranslations(translations);
            }

            return example;
        }
    }

    public Word getWord() {
        return word;
    }

    public List<Word> getTranslations() {
        return translations;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public void setTranslations(List<Word> translations) {
        this.translations = translations;
    }
}
