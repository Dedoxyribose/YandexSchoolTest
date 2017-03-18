package ru.dedoxyribose.yandexschooltest.model.entity;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Ryan on 16.03.2017.
 */
@Entity
public class Record {

    public static final int TYPE_WORD=0;
    public static final int TYPE_SENTENSE=1;

    @Id
    long id;

    int type;

    String text, translation;
    String lowText, lowTranslation;

    String direction;

    String jsonDefStr;

    long requestTime;

    @Transient
    List<Def> defs;

    @Generated(hash = 918183919)
    public Record(long id, int type, String text, String translation, String lowText, String lowTranslation, String direction,
            String jsonDefStr, long requestTime) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.translation = translation;
        this.lowText = lowText;
        this.lowTranslation = lowTranslation;
        this.direction = direction;
        this.jsonDefStr = jsonDefStr;
        this.requestTime = requestTime;
    }

    @Generated(hash = 477726293)
    public Record() {
    }

    public static class RecordConverter implements JsonDeserializer<Record> {
        @Override
        public Record deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JSONObject response = null;
            try {
                response = new JSONObject(json.getAsJsonObject().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            Record record=new Record();


            if (response.has("def")) {
                record.type=TYPE_WORD;

                List<Def> defs=new ArrayList<>();

                for (int i=0; i<response.optJSONArray("def").length(); i++) {
                    Def def=context.deserialize(json.getAsJsonObject().get("def").getAsJsonArray().get(i), Def.class);
                    defs.add(def);
                }
                record.defs=defs;

                if (defs.size()>0) {

                    record.text=defs.get(0).getWord().getText();    //TODO всегда ли непуст?
                    record.lowText=record.text.toLowerCase();

                    if (defs.get(0).getTranslations().size()>0) {

                        record.translation=record.getDefs().get(0).getTranslations().get(0).getWord().getText(); //TODO всегда ли непуст?
                        record.lowTranslation=record.translation.toLowerCase();

                    }
                }

                record.jsonDefStr=response.optJSONArray("def").toString();
            }
            else {
                record.type=TYPE_SENTENSE;
                record.direction=response.optString("lang");

                if (response.optJSONArray("text")!=null && response.optJSONArray("text").length()>0) {
                    record.translation=response.optJSONArray("text").optString(0);
                    record.lowTranslation=record.translation.toLowerCase();
                }
            }

            return record;
        }
    }

    public void inflateDefs(Gson gson) {

        if (type==TYPE_WORD){

            defs.clear();
            try
            {
                JSONArray jsonArray=new JSONArray(jsonDefStr);
                for (int i=0; i<jsonArray.length(); i++) {
                    Def def=gson.fromJson(jsonArray.optJSONObject(i).toString(), Def.class);
                    defs.add(def);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getTranslation() {
        return translation;
    }

    public String getLowText() {
        return lowText;
    }

    public String getLowTranslation() {
        return lowTranslation;
    }

    public String getDirection() {
        return direction;
    }

    public String getJsonDefStr() {
        return jsonDefStr;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public List<Def> getDefs() {
        return defs;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setText(String text) {
        this.text = text;
        this.lowText=text.toLowerCase();
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public void setLowText(String lowText) {
        this.lowText = lowText;
    }

    public void setLowTranslation(String lowTranslation) {
        this.lowTranslation = lowTranslation;
    }

    public void setJsonDefStr(String jsonDefStr) {
        this.jsonDefStr = jsonDefStr;
    }
}
