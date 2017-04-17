package ru.dedoxyribose.yandexschooltest.model.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by Ryan on 16.03.2017.
 */
//Сущность слово. Содержит текст, число, часть речи, род и транскрипцию
public class Word {

    private String text, num, pos, gen, ts;

    public static class WordConverter implements JsonDeserializer<Word> {
        @Override
        public Word deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JSONObject response = null;
            try {
                response = new JSONObject(json.getAsJsonObject().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            Word word=new Word();

            word.setGen(response.optString("gen"));
            word.setNum(response.optString("num"));
            word.setPos(response.optString("pos"));
            word.setText(response.optString("text"));
            word.setTs(response.optString("ts"));

            return word;
        }
    }

    public String getText() {
        return text;
    }

    public String getNum() {
        return num;
    }

    public String getPos() {
        return pos;
    }

    public String getGen() {
        return gen;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public void setGen(String gen) {
        this.gen = gen;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}
