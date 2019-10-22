package com.guide.permissionhelper.context;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.Build.VERSION;

public class ContextCompat {
    private static final String TAG = "ContextCompat";

    public ContextCompat() {
    }

    public static final Drawable getDrawable(Context context, int id) {
        int version = VERSION.SDK_INT;
        return version >= 21 ? ContextCompatApi21.getDrawable(context, id) : context.getResources().getDrawable(id);
    }

    public static final ColorStateList getColorStateList(Context context, int id) {
        int version = VERSION.SDK_INT;
        return version >= 23 ? ContextCompatApi23.getColorStateList(context, id) : context.getResources().getColorStateList(id);
    }

    public static final int getColor(Context context, int id) {
        int version = VERSION.SDK_INT;
        return version >= 23 ? ContextCompatApi23.getColor(context, id) : context.getResources().getColor(id);
    }

    public static int checkSelfPermission(Context context, String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        } else {
            return context.checkPermission(permission, Process.myPid(), Process.myUid());
        }
    }

    public static boolean checkPermissionGranted(Context context, String permission) {
        return checkSelfPermission(context, permission) == 0;
    }

    public static boolean checkPermissionDenied(Context context, String permission) {
        return checkSelfPermission(context, permission) == -1;
    }

    public static boolean verifyPermissions(int[] grantResults) {
        if (grantResults != null && grantResults.length >= 1) {
            int[] var4 = grantResults;
            int var3 = grantResults.length;

            for(int var2 = 0; var2 < var3; ++var2) {
                int result = var4[var2];
                if (result != 0) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}