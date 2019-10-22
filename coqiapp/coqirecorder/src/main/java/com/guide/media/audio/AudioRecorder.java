package com.guide.media.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.guide.media.StreamConfig;

import java.nio.ByteBuffer;

/**
 * 音频采集
 * 1，AudioRecorder 采集
 * 2，openSLES 采集
 */
public class AudioRecorder {
    private static final String TAG = AudioRecorder.class.getSimpleName();

    private static final int BUFFER_LENGTH = 2048;

    private IAudioStatusCallBack mCallBack;

    private AudioRecord mAudioRecord;
    private ByteBuffer mNativeBuffer;
    private byte[] mBuffer;
    private boolean mEnableACE = false;

    public AudioRecorder(IAudioStatusCallBack callBack) {
        this.mCallBack = callBack;
        if (mBuffer == null) {
            mBuffer = new byte[BUFFER_LENGTH];
        }

        if (mNativeBuffer == null) {
            mNativeBuffer = ByteBuffer.allocateDirect(BUFFER_LENGTH * 2);
        }
    }

    private void init(int sampleRate, int channels) {
        if (mAudioRecord != null) {
            return;
        }

        // 最小缓冲大小
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channels, StreamConfig.Audio.AUDIO_FORMAT_BIT);
        if (bufferSize < sampleRate) {
            bufferSize = sampleRate;
        }

        try {
            int ret = StreamConfig.initConfig(mContext);
            if (ret < 0) {
                Log.e(TAG, ".getMessage()StreamConfig.initConfig failed");
            }

            if (mEnableACE) {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate,
                        channels, StreamConfig.Audio.AUDIO_FORMAT_BIT, bufferSize);

                int ch = channels == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1;
//                AudioProcessModule.sharedInstance().createAudioProcessModule(
//                        StreamConfig.OUTPUT_SAMPLE_RATE,
//                        sampleRate, ch, ch,
//                        0, 1, 1);
//                AudioProcessModule.sharedInstance().setCaptureBuffer(mNativeBuffer, 1.3f);

            } else {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate, channels, StreamConfig.Audio.AUDIO_FORMAT_BIT, bufferSize);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            mAudioRecord = null;
            if (mCallBack != null) {
                mCallBack.onError(AudioErrors.ERROR_INIT_FAIL, "IllegalArgumentException " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            mAudioRecord = null;
            if (mCallBack != null) {
                mCallBack.onError(AudioErrors.ERROR_INIT_FAIL, "1 Exception " + e.getLocalizedMessage());
            }
        }
    }

    private void initSpeaker() {
        if (mAudioManager == null) {
            try {
                mAudioManager = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        // 监听耳机状态
        registerHeadset();

        // 根据耳机状态，初始化设置
        if (mAudioManager != null) {
            try {
                boolean enable = mAudioManager.isWiredHeadsetOn();
                mAudioManager.setSpeakerphoneOn(!enable);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void start() {

    }

    public byte[] read() {
        return null;
    }

    public void stop() {
        unregisterHeadset();

        if (mAudioRecord != null) {
            try {
                mAudioRecord.stop();
                mAudioRecord.release();

            } catch (Exception e) {
                Log.e(TAG,"audiorecorder catch Exception " + e);
                if (mCallBack != null) {
                    mCallBack.onError(AudioErrors.ERROR_STOP_FAILED, "stopAndRelease - Exception " + e.getLocalizedMessage());
                }
            }
            mAudioRecord = null;
        }
    }

    /**************************************************************************************/
    private Context mContext;
    private HeadSetReceiver mReceiver;
    private AudioManager mAudioManager;

    // 监听耳机状态
    private void registerHeadset() {
        try {
            if (mContext != null) {
                if (mReceiver == null) {
                    mReceiver = new HeadSetReceiver();
                }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
                try {
                    // 检测蓝牙
                    // 蓝牙一般有两种语音相关的模式是A2DP(高质量音乐播放，只进不出)和SCO(语音通话，有进有出）。
                    // 要实现语音从蓝牙进，那么得处于SCO模式下，也是通话模式下。
                    if (mAudioManager != null && mAudioManager.isBluetoothScoAvailableOffCall()) {
                        intentFilter.addAction(android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
                        mAudioManager.startBluetoothSco();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mContext.registerReceiver(mReceiver, intentFilter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterHeadset() {
        if (mReceiver != null && mContext != null) {
            try {
                if (mAudioManager != null && mAudioManager.isBluetoothScoOn()) {
                    mAudioManager.setBluetoothScoOn(false);
                    mAudioManager.stopBluetoothSco();
                }
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mContext.unregisterReceiver(mReceiver);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class HeadSetReceiver extends BroadcastReceiver {
        public HeadSetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (android.media.AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(android.media.AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                if (android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    if (mAudioManager != null) {
                        mAudioManager.setBluetoothScoOn(true);
                        mContext.unregisterReceiver(this);
                    }
                }
            } else if ((android.media.AudioManager.ACTION_HEADSET_PLUG.equals(action)) && (intent.hasExtra("state"))) {
                if (intent.getIntExtra("state", 0) == 0) {
                    setEnableSpeaker(context, true);
                } else if (intent.getIntExtra("state", 0) == 1) {
                    setEnableSpeaker(context, false);
                }
            }
        }
    }

    private void setEnableSpeaker(Context context, boolean enable) {
        if (mAudioManager == null) {
            try {
                mAudioManager = ((android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return;
            }
        }

        try {
            if (mAudioManager != null) {
                mAudioManager.setSpeakerphoneOn(enable);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
