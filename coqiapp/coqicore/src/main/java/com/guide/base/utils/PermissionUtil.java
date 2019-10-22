package com.guide.base.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.guide.permissionhelper.ApiUtil;
import com.guide.permissionhelper.app.ActivityCompat;

/**
 * @author yuanlongfei01 适配 Android 6.0 权限
 * jar为tbadkcore/libs/permissionHelper.jar
 */
public class PermissionUtil {

    private static final String TAG = "PermissionUtil";

    /**
     * 是否具有{@link android.Manifest.permission.ACCESS_FINE_LOCATION} 或
     * {@link android.Manifest.permission.ACCESS_COARSE_LOCATION} 权限
     * 目前LocationManager的api只要有其中之一权限就可以运行
     *
     * @return
     */
    public static boolean checkLocation(Context context) {
        return checkLocationForGoogle(context);
    }

    public static boolean checkLocationForBaiduLocation(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }
        // 百度location需要read_phone_state权限
        boolean isPermissionPhoneGranted = false;
        boolean isPermissionLocation = false;
        try {
            isPermissionPhoneGranted = ActivityCompat.checkPermissionGranted(context, Manifest.permission.READ_PHONE_STATE);
            isPermissionLocation = checkLocationForGoogle(context);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return isPermissionLocation && isPermissionPhoneGranted;
    }

    public static boolean checkLocationForGoogle(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }

        try {
            return ActivityCompat.checkPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.checkPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    public static boolean checkFineLocation(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        boolean isPermissionLocationGranted = false;
        try {
            isPermissionLocationGranted =
                    ActivityCompat.checkPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            || ActivityCompat.checkPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return isPermissionLocationGranted;
    }

    /**
     * 是否具有{@link android.Manifest.permission.CAMERA } 权限
     *
     * @return
     */
    public static boolean checkCamera(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }

        try {
            return ActivityCompat.checkPermissionGranted(context, Manifest.permission.CAMERA);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    /**
     * 是否具有{@link android.Manifest.permission.RECORD_AUDIO} 权限
     *
     * @return
     */
    public static boolean checkRecodeAudio(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }

        try {
            return ActivityCompat.checkPermissionGranted(context, Manifest.permission.RECORD_AUDIO);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    /**
     * 是否具有 {@link android.Manifest.permission.READ_PHONE_STATE} 权限
     *
     * @return
     */
    public static boolean checkReadPhoneState(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }
        try {
            return ActivityCompat.checkPermissionGranted(context, Manifest.permission.READ_PHONE_STATE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    /**
     * 是否具有 {@link android.Manifest.permission.SEND_SMS} 权限
     *
     * @return
     */
    public static boolean checkSendSms(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }

        return ActivityCompat.checkPermissionGranted(context, Manifest.permission.SEND_SMS);
    }

    /**
     * 是否具有 {@link android.Manifest.permission.WRITE_EXTERNAL_STORAGE} 权限
     *
     * @return
     */
    public static boolean checkWriteExternalStorage(Context context) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }
        try {
            return ActivityCompat.checkPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }

    /**
     * 请求定位权限
     *
     * @return
     */
    public static void reuqestLocation(Activity activity, int requestCode) {
        try {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void requestWriteExternalStorage(Activity activity, int requestCode) {
        try {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 读取手机状态
     *
     * @param activity
     * @param requestCode
     */
    public static void reuqestReadPhoneState(Activity activity, int requestCode) {
        try {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 请求Camera 权限
     *
     * @param activity
     * @param requestCode
     */
    public static void reuqestCamera(Activity activity, int requestCode) {
        try {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * @param permissions
     * @param grantResults
     * @return 转换权限授权获取结果为 map<权限,授权结果>
     */
    public static Map<String, Boolean> transformPermissionResult(String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length == 0 || grantResults == null || grantResults.length == 0) {
            return null;
        }

        Map result = new HashMap<String, Boolean>(permissions.length);
        for (int i = 0; i < permissions.length; i++) {
            if (i >= grantResults.length) {
                break;
            }

            result.put(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
        }

        return result;
    }

    public static boolean requestWriteExternalStorgeAndCameraPermission(Activity activity, int requestCode) {
        ArrayList<String> permissionList = new ArrayList<String>(2);
        if (!checkWriteExternalStorage(activity.getApplicationContext())) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!checkCamera(activity.getApplicationContext())) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (permissionList.size() == 0) {
            return false;
        }

        String[] permissions = new String[permissionList.size()];
        permissions = permissionList.toArray(permissions);
        try {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    /**
     * 请求SD卡写权限&麦克风权限
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean requestWriteExternalStorgeAndAudioPermission(Activity activity, int requestCode) {
        ArrayList<String> permissionList = new ArrayList<String>(2);
        if (!checkWriteExternalStorage(activity.getApplicationContext())) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!checkRecodeAudio(activity.getApplicationContext())) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (permissionList.size() == 0) {
            return false;
        }

        String[] permissions = new String[permissionList.size()];
        permissions = permissionList.toArray(permissions);
        try {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    /**
     * 请求录音权限
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean requestRecordAudioPermission(Activity activity, int requestCode) {
        ArrayList<String> permissionList = new ArrayList<String>();
        if (!checkRecodeAudio(activity.getApplicationContext())) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (permissionList.size() == 0) {
            return false;
        }

        String[] permissions = new String[permissionList.size()];
        permissions = permissionList.toArray(permissions);
        try {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    public static boolean checkPermission(Context context, String permission) {
        if (!ApiUtil.shouldCheckPermission()) {
            return true;
        }

        if (context == null) {
            return false;
        }

        try {
            return ActivityCompat.checkPermissionGranted(context, permission);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return false;
    }
}
