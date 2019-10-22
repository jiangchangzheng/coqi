package com.guide.media.player;

public class IGuidePlayer {
    public native void initPlayer();

    public native void prepare(String url);

    public native void start();

    public native void resume();

    public native void pause();

    public native void stop();

    public native void release();
}
