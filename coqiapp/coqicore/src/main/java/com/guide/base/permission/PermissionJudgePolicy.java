package com.guide.base.permission;

import java.util.ArrayList;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.guide.permissionhelper.ApiUtil;
import com.guide.permissionhelper.app.ActivityCompat;

/**
 * 动态权限管理判断类，处理权限逻辑
 *
 * 兼容6.0系统的权限设置。6.0之前与6.0之后有什么区别
 *
 * <p>
 *
 * demo：
 *
 * public boolean requestPermission() {
 *         if (mCurrentPermissionJudgePolicy == null) {
 *             mCurrentPermissionJudgePolicy = new PermissionJudgePolicy();
 *             mCurrentPermissionJudgePolicy.clearRequestPermissionList();
 *             mCurrentPermissionJudgePolicy.appendRequestPermission(this, Manifest.permission.CAMERA);
 *             mCurrentPermissionJudgePolicy.appendRequestPermission(this, Manifest.permission.RECORD_AUDIO);
 *             mCurrentPermissionJudgePolicy.appendRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
 *             mCurrentPermissionJudgePolicy.setOnPermissionsGrantedListener(new PermissionJudgePolicy.OnPermissionsGrantedListener() {
 *                 @Override
 *                 public void onPermissionsGranted() {
 *
 *                 }
 *             });
 *         }
 *         return mCurrentPermissionJudgePolicy.startRequestPermission(this);
 *     }
 *
 *     @Override
 *     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 *         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 *         if (grantResults != null && grantResults.length > 0 && permissions != null && permissions.length > 0) {
 *             //判断是否勾选"禁止后不再询问",如果是这样，则弹窗引导去 设置页面开启
 *             if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
 *                 Intent intent = new Intent();
 *                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 *                 intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
 *                 intent.setData(Uri.fromParts("package", this.getPackageName(), null));
 *                 this.startActivity(intent);
 *                 return;
 *             }
 *             //检查是否所有权限已获取到
 *             boolean allPermissionsGranted = true;
 *             for (int i = 0; i < permissions.length; i++) {
 *                 if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
 *                     allPermissionsGranted = false;
 *                     break;
 *                 }
 *             }
 *             if (allPermissionsGranted) {
 *                 if (mCurrentPermissionJudgePolicy != null) {
 *                     mCurrentPermissionJudgePolicy.onPermissionsGranted();
 *                 }
 *             }
 *         }
 *     }
 */

public class PermissionJudgePolicy {
	private static final String TAG = "PermissionJudgePolicy";
	public static final int REQUEST_PERMISSION_JUDGEMENT = 1;

	private ArrayList<String> requestPermissionList;
	//所有权限获取到的回调
	private OnPermissionsGrantedListener mOnPermissionsGrantedListener;

	public PermissionJudgePolicy() {
		requestPermissionList = new ArrayList<>();
	}

	/**
	 * 清理要申请的权限列表
	 */
	public void clearRequestPermissionList() {
		if (requestPermissionList != null) {
			requestPermissionList.clear();
		}
	}

	/**
	 * 添加要申请的权限
	 *
	 * @param requestPermission
	 */
	public void appendRequestPermission(Activity context, String requestPermission) {
		if (TextUtils.isEmpty(requestPermission)) {
			return;
		}
		if (checkPermissionGranted(context, requestPermission)) {
			return;
		}
		requestPermissionList.add(requestPermission);
	}

	/**
	 * 开始申请权限
	 *
	 * @param context
	 * @return
	 */
	public boolean startRequestPermission(Activity context) {
		//	如果不需要申请权限，则返回
		if (!ApiUtil.shouldCheckPermission()) {
			onPermissionsGranted();
			return false;
		}
		//	如果没有要申请的权限，则返回
		if (requestPermissionList == null || requestPermissionList.isEmpty()) {
			onPermissionsGranted();
			return false;
		}
		//	开始申请需要的权限
		startRequestPermissionInternal(context);
		return true;
	}

	/**
	 * 检查该权限是否已经获取到
	 *
	 * @param context
	 * @return
	 */
	private boolean checkPermissionGranted(Activity context, String permission) {
		if (context == null) {
			return false;
		}
		return ActivityCompat.checkPermissionGranted(context, permission);
	}

	/**
	 * 开始真正申请权限
	 *
	 * @param activity
	 */
	private void startRequestPermissionInternal(Activity activity) {
		if (activity == null) {
			return;
		}
		String[] permissions = new String[requestPermissionList.size()];
		permissions = requestPermissionList.toArray(permissions);
		try {
			ActivityCompat.requestPermissions(activity, permissions,
					REQUEST_PERMISSION_JUDGEMENT);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * 所有权限获取到的回调
	 */
	public interface OnPermissionsGrantedListener {
		void onPermissionsGranted();
	}

	public void setOnPermissionsGrantedListener(OnPermissionsGrantedListener listener) {
		mOnPermissionsGrantedListener = listener;
	}

	public void onPermissionsGranted() {
		if (mOnPermissionsGrantedListener != null) {
			mOnPermissionsGrantedListener.onPermissionsGranted();
		}
	}
}