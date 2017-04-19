package ru.dedoxyribose.yandexschooltest.event;

import ru.dedoxyribose.yandexschooltest.model.entity.Record;

/**
 * Created by Ryan on 21.03.2017.
 */

public class RecordChangedEvent {

    //константы, означающие, кто собсно послал этот event
    public static final int SENDER_TRANSLATION=0, SENDER_FAVORITE=1, SENDER_HISTORY=2;

    Record record;
    int sender;

    public RecordChangedEvent(Record record, int sender) {
        this.record = record;
        this.sender = sender;
    }

    public Record getRecord() {
        return record;
    }

    public int getSender() {
        return sender;
    }
}
