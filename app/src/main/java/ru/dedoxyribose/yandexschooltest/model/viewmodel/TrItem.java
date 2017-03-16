package ru.dedoxyribose.yandexschooltest.model.viewmodel;

import java.util.ArrayList;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.model.entity.Word;

/**
 * Created by Ryan on 16.03.2017.
 */
public class TrItem  extends ListItem {

    String num;
    List<Word> words = new ArrayList<>();
    List<Word> means = new ArrayList<>();

    public List<Word> getWords() {
        return words;
    }

    public List<Word> getMeans() {
        return means;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public void setMeans(List<Word> means) {
        this.means = means;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public TrItem(String num, List<Word> words, List<Word> means) {
        this.num = num;
        this.words = words;
        this.means = means;
    }
}
