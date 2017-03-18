package ru.dedoxyribose.yandexschooltest.model.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ryan on 17.03.2017.
 */
public class SupportedLangs {
    private List<Lang> langs = new ArrayList<>();

    public SupportedLangs() {

    }

    public List<Lang> getLangs() {
        return langs;
    }

    public void setLangs(List<Lang> langs) {
        this.langs = langs;
    }

    public SupportedLangs(List<Lang> langs) {
        this.langs = langs;
    }

    public static class SupportedLangsConverter implements JsonDeserializer<SupportedLangs> {
        @Override
        public SupportedLangs deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JSONObject response = null;
            try {
                response = new JSONObject(json.getAsJsonObject().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            SupportedLangs supportedLangs=new SupportedLangs();

            if (response.optJSONObject("langs")!=null) {
                List<Lang> langs=new ArrayList<>();
                JSONObject jsonObject=response.optJSONObject("langs");

                Iterator<?> keys = jsonObject.keys();

                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    String value = jsonObject.optString(key);

                    Lang lang=new Lang();
                    lang.setCode(key);
                    lang.setName(value);
                    langs.add(lang);
                }
                supportedLangs.setLangs(langs);
            }

            return supportedLangs;
        }
    }
}
