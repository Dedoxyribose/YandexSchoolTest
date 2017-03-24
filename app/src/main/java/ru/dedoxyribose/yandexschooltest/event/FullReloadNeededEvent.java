package ru.dedoxyribose.yandexschooltest.event;

/**
 * Created by Ryan on 24.03.2017.
 */
public class FullReloadNeededEvent {

    public static final int SENDER_TRANSLATION=0, SENDER_FAVORITE=1, SENDER_HISTORY=2;

    int sender;

    public FullReloadNeededEvent(int sender) {
        this.sender = sender;
    }

    public int getSender() {
        return sender;
    }
}
