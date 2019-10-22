package com.guide.media.camera;

public interface ICameraStatusCallBack {
    void cameraOpened();

    void cameraPreviewed();

    void cameraStopped();

    void onError(int errorId, String errorMsg);
}