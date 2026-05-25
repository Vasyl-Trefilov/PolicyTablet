package org.rusting.policytablet.client;

public class ClientCaptureData {
    public static String cellLabel = "";
    public static int progress = 0;
    public static final int MAX_PROGRESS = 200;

    public static void reset() {
        cellLabel = "";
        progress = 0;
    }
}
