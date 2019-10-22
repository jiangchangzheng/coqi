package com.guide.media.camera.utils;

import android.hardware.Camera;

import java.util.List;

public class CameraUtil {
    public static boolean choosePreviewFormat(Camera.Parameters params, int format) {
        if (params == null) {
            return false;
        }
        List<Integer> supportedPreviewFormats = params.getSupportedPreviewFormats();
        if (supportedPreviewFormats != null && supportedPreviewFormats.contains(format)) {
            params.setPreviewFormat(format);
            return true;
        }
        return false;
    }

    /**
     * 根据Camera的支持状态，以及配置的目标预览宽高
     * 设置相机的预览宽高
     *
     * @param params
     * @param w
     * @param h
     * @return
     */
    public static Camera.Size choosePreviewSize(Camera.Parameters params, int w, int h) {
        if (params == null) {
            return null;
        }

        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        if (sizes == null) {
            return null;
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            double ratioDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                double diffRat = Math.abs(ratio - targetRatio);
                if (diffRat <= ratioDiff && Math.abs(size.height - targetHeight) < minDiff) {
                    ratioDiff = diffRat;
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        if (optimalSize != null) {
            params.setPreviewSize(optimalSize.width, optimalSize.height);
        }
        return optimalSize;
    }

    /**
     * 根据Camera的支持状态
     * 设置帧率范围
     * http://stackoverflow.com/questions/22639336/#22645327
     */
    public static void chooseFixedPreviewFps(Camera.Parameters parms, int desiredThousandFps) {
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
            }
        }
    }

    /**
     * 根据Camera的支持状态
     * 设置Camera打开闪光灯
     */
    public static void turnLightOn(Camera.Parameters params) {
        if (params == null) {
            return;
        }
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes == null) {
            return;
        }
        String flashMode = params.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
        }
    }

    /**
     * 根据Camera的支持状态
     * 设置Camera关闭闪光灯
     */
    public static void turnLightOff(Camera.Parameters params) {
        if (params == null) {
            return;
        }
        List<String> flashModes = params.getSupportedFlashModes();
        String flashMode = params.getFlashMode();
        if (flashModes == null) {
            return;
        }
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }
    }
}
