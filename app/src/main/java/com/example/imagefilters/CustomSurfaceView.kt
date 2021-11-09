package com.example.imagefilters

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import org.opencv.android.JavaCameraView

class CustomSufaceView(context: Context?, attrs: AttributeSet?) :
    JavaCameraView(context, attrs) {
    val effectList: List<String>
        get() = mCamera.parameters.supportedColorEffects
    val isEffectSupported: Boolean
        get() = mCamera.parameters.colorEffect != null
    var effect: String?
        get() = mCamera.parameters.colorEffect
        set(effect) {
            val params = mCamera.parameters
            params.colorEffect = effect
            mCamera.parameters = params
        }
    fun getParameters(): Camera.Parameters? {
        return mCamera.parameters
    }
    val resolutionList: List<Camera.Size>
        get() = mCamera.parameters.supportedPreviewSizes
    var resolution: Camera.Size
        get() = mCamera.parameters.previewSize
        set(resolution) {
            disconnectCamera()
            mMaxHeight = resolution.height
            mMaxWidth = resolution.width
            connectCamera(width, height)
        }

        @JvmName("getResolutionList1")
    fun getResolutionList(): List<Camera.Size?>? {
        return mCamera.parameters.supportedPreviewSizes
    }

    @JvmName("getResolution1")
    fun getResolution(): Camera.Size? {
        return mCamera.parameters.previewSize
    }

    @JvmName("setParameters1")
    fun setParameters(params: Camera.Parameters?) {
        mCamera.parameters = params
    }


    companion object {
        private const val TAG = "OpenCustomSufaceView"
    }
}