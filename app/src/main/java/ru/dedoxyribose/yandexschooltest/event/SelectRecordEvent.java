package ru.dedoxyribose.yandexschooltest.event;

import ru.dedoxyribose.yandexschooltest.model.entity.Record;

/**
 * Created by Ryan on 23.03.2017.
 */
public class SelectRecordEvent {
    Record record;

    public SelectRecordEvent(Record record) {
        this.record = record;
    }

    public Record getRecord() {
        return record;
    }
}
