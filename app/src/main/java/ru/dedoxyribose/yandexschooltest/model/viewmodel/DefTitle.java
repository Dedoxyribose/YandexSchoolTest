package ru.dedoxyribose.yandexschooltest.model.viewmodel;

import ru.dedoxyribose.yandexschooltest.model.entity.Word;

/**
 * Created by Ryan on 16.03.2017.
 */
//Заголовок словарной статьи, выводимый в списке словаря на главном экране. Используется слово и часть речи
public class DefTitle extends ListItem {

    Word word;

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public DefTitle(Word word) {
        this.word = word;
    }
}
