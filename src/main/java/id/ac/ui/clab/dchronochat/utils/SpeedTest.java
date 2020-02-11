package id.ac.ui.clab.dchronochat.utils;

import android.util.Log;

public class SpeedTest {

    private static int length;
    private static long time;
    private static float totalTime;

    public static void initialize(int len) {
        length = len/1024;
    }

    public static void start() {
        time = System.currentTimeMillis();
    }

    public static void end() {
        time = System.currentTimeMillis() - time;
        totalTime = time / 1000f;//转成秒
    }

    public static float getTotalTime() {
        return totalTime;
    }

    public static float getSpeed() {
        return length / totalTime;
    }
}
