package com.guide.permissionhelper;

import android.os.Build.VERSION;

public class ApiUtil {
    public ApiUtil() {
    }

    public static boolean shouldCheckPermission() {
        return VERSION.SDK_INT >= 23;
    }
}
