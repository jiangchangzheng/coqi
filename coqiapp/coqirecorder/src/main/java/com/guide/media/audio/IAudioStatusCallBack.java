package com.guide.media.audio;

public interface IAudioStatusCallBack {
    void onAudioStarted();

    void onError(int errorCode, String errorMsg);
}
