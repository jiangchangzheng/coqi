package com.guide.media.recorder.util;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class OpenGLUtil {

    private static final String TAG = "OpenGLUtil";

    private static final int SIZEOF_FLOAT = 4;
    private static final int SIZEOF_SHORT = 2;

    /**
     * 创建program
     *
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            GLES20.glDeleteShader(vertexShader);
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, " " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(pixelShader);

        return program;
    }

    /**
     * 加载Shader
     *
     * @param shaderType
     * @param source
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * 创建OES 类型的Texture
     *
     * @return
     */
    public static int createOESTexture() {
        return createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    /**
     * 创建Texture对象
     * 1,GenTexture
     * 2,BindTexture
     * 3,glTexParameterf
     * <p>
     * 注意：GLES20.GL_TEXTURE_MIN_FILTER 选择了：GLES20.GL_NEAREST
     * 以及 GLES20.GL_TEXTURE_MAG_FILTER 选择了：GLES20.GL_LINEAR
     * GLES20.GL_NEAREST跟GLES20.GL_LINEAR的区别
     *
     * @param textureType
     * @return
     */
    public static int createTexture(int textureType) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        OpenGLUtil.checkGlError("glGenTextures");

        int textureId = textures[0];
        GLES20.glBindTexture(textureType, textureId);
        OpenGLUtil.checkGlError("glBindTexture " + textureId);

        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(textureType, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        OpenGLUtil.checkGlError("glTexParameter");
        return textureId;
    }

    /**
     * 删除纹理
     *
     * @param texture
     */
    public static void deleteTexture(int texture) {
        int[] textures = new int[1];
        textures[0] = texture;
        GLES20.glDeleteTextures(1, textures, 0);
    }

    /**
     * 检查是否出错
     *
     * @param op
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * 创建FloatBuffer
     *
     * @param coords
     * @return
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     * 创建FloatBuffer
     *
     * @param data
     * @return
     */
    public static FloatBuffer createFloatBuffer(ArrayList<Float> data) {
        float[] coords = new float[data.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = data.get(i);
        }
        return createFloatBuffer(coords);
    }

    /**
     * 创建ShortBuffer
     *
     * @param coords
     * @return
     */
    public static ShortBuffer createShortBuffer(short[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_SHORT);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(coords);
        sb.position(0);
        return sb;
    }

    /**
     * 创建ShortBuffer
     *
     * @param data
     * @return
     */
    public static ShortBuffer createShortBuffer(ArrayList<Short> data) {
        short[] coords = new short[data.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = data.get(i);
        }
        return createShortBuffer(coords);
    }

    /**
     * 创建IntBuffer
     *
     * @param coords
     * @return
     */
    public static IntBuffer createIntBuffer(int[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_SHORT);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer sb = bb.asIntBuffer();
        sb.put(coords);
        sb.position(0);
        return sb;
    }

    /**
     * 创建IntBuffer
     *
     * @param data
     * @return
     */
    public static IntBuffer createIntBuffer(ArrayList<Integer> data) {
        int[] coords = new int[data.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = data.get(i);
        }
        return createIntBuffer(coords);
    }
}
