package id.co.edtslib.uikit.utils

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.view.Choreographer
import kotlin.text.contains

fun isLowEndDevice(): Boolean {
    val cores = Runtime.getRuntime().availableProcessors()
    return cores <= 4
}

fun isLowRamDevice(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo.totalMem / (1024 * 1024) <= 2000 // Less than 2GB RAM
}

fun isLowGPUDevice(): Boolean {
    val glVersion = GLES20.glGetString(GLES20.GL_VERSION)
    return glVersion.contains("OpenGL ES 2.0") // Older GPUs
}

fun isLowEndDevice(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.isLowRamDevice // True for low-end devices
}

fun isDeviceStruggling(callback: (Boolean) -> Unit) {
    Choreographer.getInstance().postFrameCallback { frameTimeNanos ->
        val startTime = System.nanoTime()

        Choreographer.getInstance().postFrameCallback { nextFrameTimeNanos ->
            val elapsedTimeMs = (nextFrameTimeNanos - frameTimeNanos) / 1_000_000
            callback(elapsedTimeMs > 20) // More than 20ms means jank
        }
    }
}


fun isLowPerformanceDevice(context: Context): Boolean {
    return isLowEndDevice() ||
            isLowRamDevice(context) ||
            isLowGPUDevice()
}




