package com.guide.media.camera.test;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.guide.media.R;
import com.guide.media.camera.CameraManager;
import com.guide.media.camera.ICameraStatusCallBack;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";

    private FrameLayout mContentLayout;
    private TextureView mTextureView;

    private SurfaceTexture mPreviewSurface;
    private CameraManager mCameraManager;

    private boolean mIsCameraStopped = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏无状态栏，并竖屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_camera);
        mContentLayout = findViewById(R.id.content_view);

        // 初始化 Camera管理生命周期
        mCameraManager = new CameraManager(mCameraStatusCallBack);

        mTextureView = new TextureView(this);
        mContentLayout.addView(mTextureView);
        mTextureView.setSurfaceTextureListener(mListener);
    }

    private TextureView.SurfaceTextureListener mListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.e(TAG, "onSurfaceTextureAvailable() called");
            mPreviewSurface = surfaceTexture;
            startCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            Log.e(TAG, "onSurfaceTextureSizeChanged() called");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.e(TAG, "onSurfaceTextureDestroyed() called");
            mPreviewSurface = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraManager != null) {
            mCameraManager.closeCamera();
        }
    }

    public void startCamera() {
        if (!mIsCameraStopped) {
            return;
        }
        if (mPreviewSurface != null) {
            Log.e(TAG, "startCamera() called");
            mIsCameraStopped = false;
            mCameraManager.openCamera();
        }
    }

    private ICameraStatusCallBack mCameraStatusCallBack = new ICameraStatusCallBack() {
        @Override
        public void cameraOpened() {
            if (mCameraManager != null) {
                mCameraManager.startPreview(mPreviewSurface);
            }
        }

        @Override
        public void cameraPreviewed() {
            // 如果开启预览成功可以做其他事情。
        }

        @Override
        public void cameraStopped() {
            mIsCameraStopped = true;
        }

        @Override
        public void onError(int errorId, String errorMsg) {
            // 相机的各种异常情况的处理以及日志收集
            Log.e(TAG, "camera error code = " + errorId + "error msg=" + errorMsg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mIsCameraStopped = true;
        // 先暂停预览以及stop
        if (mCameraManager != null) {
            mCameraManager.release();
        }
    }
}
