package ru.dedoxyribose.yandexschooltest.model.viewmodel;

/**
 * Created by Ryan on 16.03.2017.
 */
public class ExItem  extends ListItem {

    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ExItem(String text) {
        this.text = text;
    }
}
