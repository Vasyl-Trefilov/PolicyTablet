package org.rusting.policytablet.client;

public class Message {
    private static String text = "";
    private static int timer = 0;

    public static void send(String message, int durationFrames) {
        text = message;
        timer = durationFrames;
    }

    public static String getText() {
        return text;
    }

    public static int getTimer() {
        return timer;
    }

    public static void tick() {
        if (timer <= 0) return;
        timer--;
        if (timer == 0) text = "";
    }
}
