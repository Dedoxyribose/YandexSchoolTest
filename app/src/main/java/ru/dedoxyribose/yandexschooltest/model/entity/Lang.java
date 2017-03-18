package ru.dedoxyribose.yandexschooltest.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Ryan on 17.03.2017.
 */
@Entity
public class Lang {
    @Id
    private String code;
    private String name;

    private long askedTime;

    @Generated(hash = 2111248664)
    public Lang(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Generated(hash = 1197397665)
    public Lang() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAskedTime() {
        return askedTime;
    }

    public void setAskedTime(long askedTime) {
        this.askedTime = askedTime;
    }

}
