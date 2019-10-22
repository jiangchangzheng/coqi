package com.guide.permissionhelper.app;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.guide.permissionhelper.ApiUtil;
import com.guide.permissionhelper.context.ContextCompat;

public class ActivityCompat extends ContextCompat {
    public ActivityCompat() {
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (ApiUtil.shouldCheckPermission()) {
            ActivityCompatApi23.requestPermissions(activity, permissions, requestCode);
        } else if (activity instanceof ActivityCompat.OnRequestPermissionsResultCallback) {
            requestPermissions(activity, permissions, requestCode, (ActivityCompat.OnRequestPermissionsResultCallback)activity);
        }

    }

    public static void requestPermissions(final Activity activity, final String[] permissions,
                                          final int requestCode, final ActivityCompat.OnRequestPermissionsResultCallback callback) {
        if (ApiUtil.shouldCheckPermission()) {
            ActivityCompatApi23.requestPermissions(activity, permissions, requestCode);
        } else if (!activity.isFinishing() && callback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    int[] grantResults = new int[permissions.length];
                    PackageManager packageManager = activity.getPackageManager();
                    String packageName = activity.getPackageName();
                    int permissionCount = permissions.length;

                    for(int i = 0; i < permissionCount; ++i) {
                        grantResults[i] = packageManager.checkPermission(permissions[i], packageName);
                    }

                    callback.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            });
        }

    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ApiUtil.shouldCheckPermission() ? ActivityCompatApi23.shouldShowRequestPermissionRationale(activity, permission) : false;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        if (ApiUtil.shouldCheckPermission() && permissions != null && permissions.length != 0) {
            for(int i = permissions.length - 1; i >= 0; --i) {
                if (!shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public interface OnRequestPermissionsResultCallback {
        void onRequestPermissionsResult(int var1, String[] var2, int[] var3);
    }
}
