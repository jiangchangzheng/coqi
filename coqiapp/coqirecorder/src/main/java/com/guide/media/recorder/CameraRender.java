package com.guide.media.recorder;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.guide.filter.GLImageOESInputFilter;
import com.guide.gl.gles.EglCore;
import com.guide.gl.gles.WindowSurface;
import com.guide.media.recorder.util.OpenGLUtil;
import com.guide.media.recorder.util.TextureRotationUtils;

import java.nio.FloatBuffer;

public class CameraRender {
    private static final String TAG = "CameraRender";
    private static final int MSG_INIT = 1;
    private static final int MSG_RENDER = 2;
    private static final int MSG_RELEASE = 3;

    private EglCore mEglCore;

    private WindowSurface mWindowSurface;
    private int mInputTexture; // OES
    private SurfaceTexture mCameraTexture;

    private GLImageOESInputFilter oesInputFilter;
    private float[] transformMatrix = new float[16];

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private SurfaceTexture mSurface;

    private boolean isFinishing = false;

    public CameraRender() {

    }

    private Handler.Callback mCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT: {
                    doInit();
                    break;
                }
                case MSG_RENDER: {
                    doDrawFrame();
                    break;
                }
                case MSG_RELEASE: {
                    doRelease();
                    break;
                }
                default:
                    break;
            }
            return false;
        }
    };

    public void initResource(SurfaceTexture surface) {
        isFinishing = false;
        mSurface = surface;
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("RendererThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper(), mCallBack);
        }
        mHandler.obtainMessage(MSG_INIT).sendToTarget();
    }

    private void doInit() {
        Log.e(TAG, "init() called");
        initEglResource();
        initWindowSurface(mSurface);
        initCameraSurface();
        initCameraFilter();
    }

    private void initEglResource() {
        try {
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        } catch (Exception e) {
            // 初始化环境失败的处理
            e.printStackTrace();
        }
    }

    private void releaseEglResource() {
        if (mEglCore != null) {
            mEglCore.makeNothingCurrent();
            mEglCore.release();
            mEglCore = null;
        }
    }

    private void initWindowSurface(SurfaceTexture surface) {
        if (surface != null) {
            // 可视窗口
            if (mWindowSurface == null) {
                mWindowSurface = new WindowSurface(mEglCore, surface);
                mWindowSurface.makeCurrent();
            }
        } else {
            // 输出错误
            Log.e(TAG, "surface is not valid");
        }
    }

    private void releaseWindowSurface() {
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
    }

    private void initCameraSurface() {
        if (mCameraTexture == null) {
            mInputTexture = OpenGLUtil.createOESTexture();
            mCameraTexture = new SurfaceTexture(mInputTexture);
            mCameraTexture.setOnFrameAvailableListener(mFrameAvailableListener);
            // This Surface was established on a previous run, so no
            // surfaceChanged()
            // message is forthcoming. Finish the surface setup now.
            //
            // We could also just call this unconditionally, and perhaps do an
            // unnecessary
            // bit of reallocating if a surface-changed message arrives.
        }
    }

    private SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener =
            new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    if (isFinishing) {
                        return;
                    }
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_RENDER);
                    }
                }
            };

    private void releaseCameraSurface() {
        if (mCameraTexture != null) {
            mCameraTexture.release();
            mCameraTexture = null;
        }
        if (mInputTexture >= 0) {
            OpenGLUtil.deleteTexture(mInputTexture);
            mInputTexture = -1;
        }
    }

    private void initCameraFilter() {
        mVertexBuffer = OpenGLUtil.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
        oesInputFilter = new GLImageOESInputFilter();
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return mCameraTexture;
    }

    private void doDrawFrame() {
        try {
            // 如果存在新的帧，则更新帧
            if (mCameraTexture != null) {
                mCameraTexture.updateTexImage();
                mCameraTexture.getTransformMatrix(transformMatrix);
            }
            if (mWindowSurface == null) {
                return;
            }

            //设置视口
            GLES20.glViewport(0, 0, 1080, 2265);
            //清楚屏幕
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClearColor(1f, 1f, 0f, 0f);

            oesInputFilter.setTextureTransformMatrix(transformMatrix);
            oesInputFilter.drawFrame(mInputTexture, mVertexBuffer, mTextureBuffer);
            // 这里可以增加其他的filter进行图像处理，美颜美白

            // 显示到屏幕
            mWindowSurface.swapBuffers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        Log.e(TAG, "release() called");
        isFinishing = true;

        // 在对应的线程调用egl api：解决call to opengl es api with no current context.
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.obtainMessage(MSG_RELEASE);
        }
    }

    private void doRelease() {
        releaseWindowSurface();
        releaseCameraSurface();
        releaseEglResource();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        mHandler = null;
        mHandlerThread = null;
    }
}