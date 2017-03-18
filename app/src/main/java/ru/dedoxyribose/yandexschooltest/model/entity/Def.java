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
public class Def {

    private Word word;

    private List<Translation> translations = new ArrayList<>();

    public static class DefConverter implements JsonDeserializer<Def> {
        @Override
        public Def deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JSONObject response = null;
            try {
                response = new JSONObject(json.getAsJsonObject().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            Def def=new Def();

            Word word=context.deserialize(json.getAsJsonObject(), Word.class);
            def.setWord(word);

            if (response.optJSONArray("tr")!=null) {
                List<Translation> translations=context.deserialize(json.getAsJsonObject().get("tr").getAsJsonArray(), new TypeToken<List<Translation>>(){}.getType());
                def.setTranslations(translations);
            }

            return def;
        }
    }

    public Word getWord() {
        return word;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }
}
