package com.guide.media.recorder;

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

/**
 * 目标：
 * 支持美颜
 * 支持滤镜
 * 支持贴纸
 *
 * 支持直播以及视频录制
 * 支持视频编辑
 *
 */
public class RecordActivity extends Activity {
    private static final String TAG = "CameraActivity";

    private FrameLayout mContentLayout;
    private TextureView mTextureView;

    private SurfaceTexture mPreviewSurface; // 不直接设置设置给Camera
    private CameraRender mCameraRender;
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
            Log.e(TAG, "surfaceCreated() called");
            mPreviewSurface = surfaceTexture;
            startRecord();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.e(TAG, "surfaceDestroyed() called");
            mPreviewSurface = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    public void startRecord() {
        if (!mIsCameraStopped) {
            return;
        }
        if (mPreviewSurface != null) {
            Log.e(TAG, "startRecord() called");
            mIsCameraStopped = false;
            if (mCameraRender == null) {
                mCameraRender = new CameraRender();
                mCameraRender.initResource(mPreviewSurface);
            }
            mCameraManager.openCamera();
        }
    }

    private ICameraStatusCallBack mCameraStatusCallBack = new ICameraStatusCallBack() {
        @Override
        public void cameraOpened() {
            if (mCameraManager != null && mCameraRender != null) {
                mCameraManager.startPreview(mCameraRender.getCameraSurfaceTexture());
            }
        }

        @Override
        public void cameraPreviewed() {
            // 如果开启预览成功可以做其他事情。
        }

        @Override
        public void cameraStopped() {
            mIsCameraStopped = true;
            // 在camera关闭之后释放，surface资源，解决：uery: BufferQueue has been abandoned
            if (mCameraRender != null) {
                mCameraRender.release();
            }
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
