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

/**
 * Сущность перевод. Содержит слово (переведённое), и списки синонимов, их переводов и примеров
 */
public class Translation {

    private Word word;

    private List<Word> syns = new ArrayList<>();
    private List<Word> means = new ArrayList<>();
    private List<Example> examples = new ArrayList<>();

    public static class TranslationConverter implements JsonDeserializer<Translation> {
        @Override
        public Translation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JSONObject response = null;
            try {
                response = new JSONObject(json.getAsJsonObject().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            Translation translation=new Translation();

            Word word=context.deserialize(json.getAsJsonObject(), Word.class);
            translation.setWord(word);

            if (response.optJSONArray("syn")!=null) {
                List<Word> syns=context.deserialize(json.getAsJsonObject().get("syn").getAsJsonArray(), new TypeToken<List<Word>>(){}.getType());
                translation.setSyns(syns);
            }

            if (response.optJSONArray("mean")!=null) {
                List<Word> means=context.deserialize(json.getAsJsonObject().get("mean").getAsJsonArray(), new TypeToken<List<Word>>(){}.getType());
                translation.setMeans(means);
            }

            if (response.optJSONArray("ex")!=null) {
                List<Example> exs=context.deserialize(json.getAsJsonObject().get("ex").getAsJsonArray(), new TypeToken<List<Example>>(){}.getType());
                translation.setExamples(exs);
            }

            return translation;
        }
    }


    public Word getWord() {
        return word;
    }

    public List<Word> getSyns() {
        return syns;
    }

    public List<Word> getMeans() {
        return means;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public void setSyns(List<Word> syns) {
        this.syns = syns;
    }

    public void setMeans(List<Word> means) {
        this.means = means;
    }

    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }
}
