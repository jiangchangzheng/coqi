package com.guide.media;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.guide.base.permission.PermissionJudgePolicy;
import com.guide.media.camera.test.CameraActivity;
import com.guide.media.editor.PlayerActivity;
import com.guide.media.recorder.RecordActivity;

/**
 * 目标：
 *
 * 支持直播和点播。
 * 支持调整显示比例:默认、原始大小、16:9、4:3、铺满屏幕、居中裁剪。
 * 支持滑动调节播放进度、声音、亮度；双击播放、暂停；保存播放进度。
 * 支持边播边缓存，使用了AndroidVideoCache。
 * 支持弹幕，使用了DanmakuFlameMaster。
 * 支持 Https，rtsp，concat 协议。
 * 支持播放本地视频以及 raw 和 assets 视频。
 * 支持重力感应自动进入/退出全屏以及手动进入/退出全屏，全屏状态下可锁定。
 * 完美实现列表播放（RecyclerView 和 ListView），列表自动播放。
 * 支持列表小窗全局悬浮播放，Android 8.0 画中画功能。
 * 支持连续播放一个列表的视频。
 * 支持广告播放。
 * 支持清晰度切换。
 * 支持扩展自定义播放内核，MediaPlayer、ExoPlayer、vitamio 等。
 * 支持完全自定义控制层。
 * 支持多路播放器同时播放，没有任何控制 UI 的纯播放
 *
 * 支持秒开
 * 支持页面播放起平滑转换
 */
public class MainActivity extends Activity {
    private PermissionJudgePolicy mCurrentPermissionJudgePolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.startLive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission("startLive");
            }
        });

        findViewById(R.id.startCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission("startCamera");
            }
        });

        findViewById(R.id.startPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission("startPlay");
            }
        });
    }

    /**
     * 开始检测
     *
     * @return
     */
    public void requestPermission(final String from) {
        if (mCurrentPermissionJudgePolicy == null) {
            mCurrentPermissionJudgePolicy = new PermissionJudgePolicy();
            mCurrentPermissionJudgePolicy.clearRequestPermissionList();
            mCurrentPermissionJudgePolicy.appendRequestPermission(this, Manifest.permission.CAMERA);
            mCurrentPermissionJudgePolicy.appendRequestPermission(this, Manifest.permission.RECORD_AUDIO);
            mCurrentPermissionJudgePolicy.appendRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            mCurrentPermissionJudgePolicy.setOnPermissionsGrantedListener(new PermissionJudgePolicy.OnPermissionsGrantedListener() {
                @Override
                public void onPermissionsGranted() {
                    if ("startCamera".equals(from)) {
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        startActivity(intent);
                    } else if ("startLive".equals(from)) {
                        Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                        startActivity(intent);
                    } else if ("startPlay".equals(from)) {
                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
        mCurrentPermissionJudgePolicy.startRequestPermission(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length > 0 && permissions != null && permissions.length > 0) {
            //判断是否勾选"禁止后不再询问",如果是这样，则弹窗引导去 设置页面开启
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                popPermissionDialog(this);
                return;
            }

            //检查是否所有权限已获取到
            boolean allPermissionsGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                if (mCurrentPermissionJudgePolicy != null) {
                    mCurrentPermissionJudgePolicy.onPermissionsGranted();
                }
            }
        }
    }

    public void popPermissionDialog(final Activity context) {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("权限");
        dialog.setMessage("开启日志");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "打开", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(intent);
                context.finish();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
                context.finish();
            }
        });
        dialog.show();
    }
}
