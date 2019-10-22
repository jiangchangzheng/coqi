package com.guide.media;

import android.content.Context;
import android.media.AudioFormat;

public class StreamConfig {
    public static class Audio {
        /**
         * 音频采样率
         */
        public static final int AUDIO_FREQUENCY = 44100;

        /**
         * 音频采样率
         */
        public static final int AUDIO_RTC_FREQUENCY_48K = 48000;

        /**
         * 音频采样率
         */
        public static final int AUDIO_RTC_FREQUENCY_32K = 32000;

        /**
         * 音频采样率
         */
        public static final int AUDIO_RTC_FREQUENCY_16K = 16000;

        /**
         * 音频数据格式
         */
        public static final int AUDIO_FORMAT_BIT = AudioFormat.ENCODING_PCM_16BIT;
        /**
         * 音频播放声道
         */
        public static final int AUDIO_FORMAT_CHANNEL_FOR_PLAY = AudioFormat.CHANNEL_OUT_MONO;
        /**
         * 音频录制声道
         */
        public static final int AUDIO_FORMAT_CHANNEL_FOR_RECORD = AudioFormat.CHANNEL_IN_MONO;

        public static final int AUDIO_FORMAT_CHANNELS_NB1 = 1;
        public static final int AUDIO_FORMAT_CHANNELS_NB2 = 2;

        public static final int AUDIO_FORMAT_PCM_SHORT16 = 16;
    }

    // 获取所有配置信息
    public static int initConfig(Context context) {
        // 当前机器是否支持opensles
        // 当前机器是够支持opengl
        return 0;
    }
}
