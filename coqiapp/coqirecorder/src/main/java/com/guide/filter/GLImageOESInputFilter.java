package com.guide.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.guide.media.recorder.util.OpenGLUtil;

import java.nio.FloatBuffer;

public class GLImageOESInputFilter {
    public static final String OES_VERTEX_SHADER = "\n" +
            "// GL_OES_EGL_image_external 格式纹理输入滤镜\n" +
            "uniform mat4 transformMatrix;" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    textureCoordinate = (transformMatrix * aTextureCoord).xy;\n" +
            "}\n";
    public static final String OES_FRAGMENT_SHADER = "\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES inputTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(inputTexture, textureCoordinate);\n" +
            "}";

    // 纹理字符串
    protected String mVertexShader;
    protected String mFragmentShader;
    // 句柄
    protected int mProgramHandle;

    private int aPositionLoc;
    private int aTextureCoordLoc;
    private int inputTextureLoc;

    private int mTransformMatrixLoc;
    private float[] mTransformMatrix;

    public GLImageOESInputFilter() {
        // 记录shader数据
        mVertexShader = OES_VERTEX_SHADER;
        mFragmentShader = OES_FRAGMENT_SHADER;
        // 初始化程序句柄
        initProgramHandle();
    }

    private void initProgramHandle() {
        mProgramHandle = OpenGLUtil.createProgram(mVertexShader, mFragmentShader);

        aPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        aTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        inputTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "inputTexture");
        mTransformMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "transformMatrix");
    }

    /**
     * 设置SurfaceTexture的变换矩阵
     *
     * @param transformMatrix
     */
    public void setTextureTransformMatrix(float[] transformMatrix) {
        mTransformMatrix = transformMatrix;
    }

    public void drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(inputTextureLoc, 0);
        GLES20.glUniformMatrix4fv(mTransformMatrixLoc, 1, false, mTransformMatrix, 0);

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(aPositionLoc, 2,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(aTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, 0, textureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
    }
}
