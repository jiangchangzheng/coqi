package com.guide.media.camera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.guide.media.camera.utils.CameraUtil;

/**
 * Camera 关键流程
 * 1，获取CameraId
 * 2，openCamera
 * 3，setParameters
 * 4，startPreview
 * 5，stopPreview
 * 6，release
 *
 * a，不同模式喜下前后台切换，Camera如何处理比较合适？？
 * <p>
 * 1，拍照模式：切换到后台，关闭当前App的Camera。切换到前台后，再次打开Camera
 * 2，录制模式
 * 3，直播模式
 * <p>
 * b，什么样的预览比例是比较合适的？
 */
public class CameraManager {
    private static final String TAG = "CameraManager";

    private static final int PREVIEW_FORMAT = ImageFormat.NV21;
    private static final int REQ_CAMERA_FPS = 30;

    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_START_PREVIEW = 2;
    private static final int MSG_STOP_PREVIEW = 3;
    private static final int MSG_CLOSE_CAMERA = 4;

    private HandlerThread mCameraThread = null;
    private Handler mCameraHandler = null;

    private Camera mCamera;
    private int mCameraId = -1;
    private Camera.CameraInfo mCameraInfo;

    private ICameraStatusCallBack mCameraStatusCallBack;
    private SurfaceTexture mSurface;

    private int previewWidth = 720;
    private int previewHeight = 1280;
    private int mPreviewBufferSize = 0;

    public CameraManager(ICameraStatusCallBack statusCallback) {
        mCameraStatusCallBack = statusCallback;
        startCameraThread();
        initCameraInfo();
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(Looper.getMainLooper(), mHandlerCallBack);
    }

    private void stopCameraThread() {
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacksAndMessages(null);
        }
        if (mCameraThread != null) {
            mCameraThread.quit();
        }
        mCameraThread = null;
        mCameraHandler = null;
    }

    /**
     * 初始化摄像头信息。
     */
    private void initCameraInfo() {
        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // 前置摄像头信息
                mCameraId = cameraId;
                mCameraInfo = cameraInfo;
            }
        }
    }

    //
    public void openCamera() {
        if (mCameraHandler != null) {
            mCameraHandler.obtainMessage(MSG_OPEN_CAMERA).sendToTarget();
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        mSurface = surfaceTexture;
        if (mCameraHandler != null) {
            mCameraHandler.obtainMessage(MSG_START_PREVIEW).sendToTarget();
        }
    }

    //
    public void stopPreview() {
        if (mCameraHandler != null) {
            mCameraHandler.removeMessages(MSG_START_PREVIEW);
            mCameraHandler.obtainMessage(MSG_STOP_PREVIEW).sendToTarget();
        }
    }

    /**
     * onPause 暂时关闭相机
     *
     */
    public void closeCamera() {
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacksAndMessages(null);
            mCameraHandler.obtainMessage(MSG_CLOSE_CAMERA, false).sendToTarget();
        }
    }

    /**
     * 页面退出或者以后也不再使用相机的时候调用
     */
    public void release() {
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacksAndMessages(null);
            mCameraHandler.obtainMessage(MSG_CLOSE_CAMERA, true).sendToTarget();
        }
    }

    private Handler.Callback mHandlerCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_CAMERA: {
                    doOpenCamera();
                    break;
                }
                case MSG_START_PREVIEW: {
                    doStartPreview();
                    break;
                }
                case MSG_STOP_PREVIEW: {
                    doStopPreview();
                    break;
                }
                case MSG_CLOSE_CAMERA: {
                    Boolean isCloseAll = (Boolean) msg.obj;
                    doCloseCamera(isCloseAll);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Illegal message: " + msg.what);
            }
            return false;
        }
    };

    /**
     * 开启指定摄像头
     */
    private void doOpenCamera() {
        Camera camera = mCamera;
        if (camera != null) {
            throw new RuntimeException("You must close previous camera before open a new one.");
        }
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
            if (mCameraStatusCallBack != null) {
                mCameraStatusCallBack.onError(
                        CameraErrors.ERROR_CAMERA_OPEN_FAILED,
                        "摄像头开启失败，出现异常，异常信息是" + e.getMessage());
            }
            return;
        }

        if (mCamera == null) {
            if (mCameraStatusCallBack != null) {
                mCameraStatusCallBack.onError(CameraErrors.ERROR_CAMERA_OPEN_FAILED,
                        "摄像头开启失败，出现异常，mCamera is null");
            }
            return;
        }

        mCamera.setErrorCallback(mCameraErrorCallback);

        doSetCameraParam();

        Log.e(TAG, "Camera[" + mCameraId + "] has been opened.");
        if (mCameraStatusCallBack != null) {
            mCameraStatusCallBack.cameraOpened();
        }
    }

    private void doSetCameraParam() {
        if (mCamera == null) {
            throw new RuntimeException("You must open a camera before set parameters.");
        }
        Camera.Parameters params = mCamera.getParameters();
        // 设置图像格式
        boolean formatNV21AVValid = CameraUtil.choosePreviewFormat(params, PREVIEW_FORMAT);
        if (formatNV21AVValid) {
            //
        }
        // 设置预览大小
        Camera.Size previewSize = CameraUtil.choosePreviewSize(params, previewWidth, previewHeight);
        if (previewSize != null) {
            Log.e(TAG, "previewSize width,height =" + previewSize.width + "," + previewSize.height);

            // 解决onPreviewFrame 频繁GC问题
            int frameWidth = previewSize.width;
            int frameHeight = previewSize.height;
            int previewFormat = params.getPreviewFormat();
            PixelFormat pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(previewFormat, pixelFormat);
            mPreviewBufferSize = (frameWidth * frameHeight * pixelFormat.bitsPerPixel) / 8;
        }
//        // 设置帧率范围
//        CameraUtil.chooseFixedPreviewFps(params, REQ_CAMERA_FPS * 1000);
//        // 设置对焦模式
//        List<String> focusModes = params.getSupportedFocusModes();
//        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        }
//        params.setRecordingHint(true);

        mCamera.setDisplayOrientation(getCameraDisplayOrientation(mCameraInfo, 0));
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
            if (mCameraStatusCallBack != null) {
                mCameraStatusCallBack.onError(
                        CameraErrors.ERROR_CAMERA_SET_PARAMETER_FAILED,
                        "摄像头设置参数错误");
            }
        }
    }

    /**
     * 获取预览画面要校正的角度。
     */
    private int getCameraDisplayOrientation(Camera.CameraInfo cameraInfo, int rotation) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 开始预览。
     */
    private void doStartPreview() {
        Log.e(TAG, "doStartPreview() called");
        Camera camera = mCamera;
        try {
            if (camera != null) {
                // 解决使用setPreviewCallback 频繁GC问题
                if (mPreviewBufferSize != 0) {
                    camera.addCallbackBuffer(new byte[mPreviewBufferSize]);
                    camera.addCallbackBuffer(new byte[mPreviewBufferSize]);
                    camera.addCallbackBuffer(new byte[mPreviewBufferSize]);
                    camera.setPreviewCallbackWithBuffer(mPreviewCallback);
                }
                camera.setPreviewTexture(mSurface);
                camera.startPreview();
                if (mCameraStatusCallBack != null) {
                    mCameraStatusCallBack.cameraPreviewed();
                }
            } else {
                if (mCameraStatusCallBack != null) {
                    mCameraStatusCallBack.onError(
                            CameraErrors.ERROR_CAMERA_PREVIEW_FAILED,
                            "摄像头开启预览失败，原因camera is null，camera not inited");
                }
            }
        } catch (Exception e) {
            if (mCameraStatusCallBack != null) {
                mCameraStatusCallBack.onError(
                        CameraErrors.ERROR_CAMERA_PREVIEW_FAILED,
                        "摄像头开启预览失败，error msg=" + e.getMessage());
            }
        }
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            // 解决使用setPreviewCallback 频繁GC问题
            camera.addCallbackBuffer(bytes);
//            Log.e(TAG, "onPreviewFrame() called");
        }
    };

    /**
     * 停止预览。
     */
    private void doStopPreview() {
        Log.e(TAG, "doStopPreview() called");
        Camera camera = mCamera;
        if (camera != null) {
            camera.stopPreview();
        }
    }

    /**
     *
     * @param isCloseAll 是否释放所有资源
     */
    private void doCloseCamera(boolean isCloseAll) {
        Log.e(TAG, "doCloseCamera() called isCloseAll=" + isCloseAll);
        if (mCamera != null) {
            mCamera.stopPreview();
            // setPreviewCallback(null) 解决：Camera is being used after Camera.release() was called
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.release();
            mCamera = null;
        }
        if (isCloseAll) {
            stopCameraThread();
            mCameraId = -1;
            mCameraInfo = null;
        }
        if (mCameraStatusCallBack != null) {
            mCameraStatusCallBack.cameraStopped();
        }
    }

    private Camera.ErrorCallback mCameraErrorCallback = new Camera.ErrorCallback() {
        @Override
        public void onError(int i, Camera camera) {
            Log.e(TAG, "camera onError index=" + i);
        }
    };
}
