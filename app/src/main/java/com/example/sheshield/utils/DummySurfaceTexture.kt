package com.example.sheshield.utils

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.view.Surface

class DummySurfaceTexture : SurfaceTexture(0) {
    init {
        setDefaultBufferSize(640, 480)
    }
}