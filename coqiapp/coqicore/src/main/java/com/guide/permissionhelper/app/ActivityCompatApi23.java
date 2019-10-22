package com.guide.permissionhelper.app;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;

@TargetApi(23)
@SuppressLint({"NewApi"})
public class ActivityCompatApi23 {
    public ActivityCompatApi23() {
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (activity instanceof ActivityCompatApi23.RequestPermissionsRequestCodeValidator) {
            ((ActivityCompatApi23.RequestPermissionsRequestCodeValidator)activity).validateRequestPermissionsRequestCode(requestCode);
        }

        activity.requestPermissions(permissions, requestCode);
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return activity.shouldShowRequestPermissionRationale(permission);
    }

    public interface RequestPermissionsRequestCodeValidator {
        void validateRequestPermissionsRequestCode(int var1);
    }
}
