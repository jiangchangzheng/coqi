package com.guide.media.player;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.guide.demo.R;

/**
 * 方式1，自带MediaPlayer MediaController + VideoView 实现视频播放的功能
 * 方式2，基于ijkPlayer。ijkplayer是一个基于FFmpeg的轻量级Android/iOS视频播放器。
 *
 */
public class PlayerActivity extends Activity {
    private static final String TAG = "PlayerActivity";
    private GuideIjkplayer mGuideijkplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);


//        String path = Environment.getExternalStorageDirectory().getPath()+"/20190909.mp4";
        String path = "https://haokan.baidu.com/v?vid=14551686948667289047&pd=bjh&fr=bjhauthor&type=video";
//        File file = new File(path);
//        Log.e(TAG, "path =" + path + "|is exists=" + file.exists());

        //
        useVideoView(path);

        //

    }

    // 方式MediaController+VideoView实现方式
    //这种方式是最简单的实现方式。VideoView继承了SurfaceView
    // 同时实现了MediaPlayerControl接口，
    // MediaController则是安卓封装的辅助控制器，
    // 带有暂停，播放，停止，进度条等控件。
    // 通过VideoView+MediaController可以很轻松的实现视频播放、停止、快进、快退等功能。
    public void useVideoView(String path) {
        Uri uri = Uri.parse(path);
        VideoView videoView = new VideoView(this);
        videoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        setContentView(videoView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        videoView.start();
        videoView.requestFocus();
    }

    /**
     * void start()：开始播放
     * void stopPlayback()：停止播放
     * void pause()：暂停
     * void resume()：重新播放
     * void seekTo(int msec)：从第几毫秒开始播放
     * int getCurrentPosition()：获取当前播放的位置
     * int getDuration()：获取当前播放视频的总长度
     * boolean isPlaying()：当前VideoView是否在播放视频
     * void setVideoPath(String path)：以文件路径的方式设置VideoView播放的视频源。
     * void setVideoURI(Uri uri)：以Uri的方式设置视频源，可以是网络Uri或本地Uri。
     * setMediaController(MediaController controller)：设置MediaController控制器。
     * setOnCompletionListener(MediaPlayer.onCompletionListener l)：监听播放完成的事件。
     * setOnErrorListener(MediaPlayer.OnErrorListener l)：监听播放发生错误时候的事件。
     * setOnPreparedListener(MediaPlayer.OnPreparedListener l)：：监听视频装载完成的事件。
     *
     */

    public void userIjkPlayer() {

    }

}
