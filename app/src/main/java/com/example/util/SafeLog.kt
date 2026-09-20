package com.example.util

import android.util.Log
import com.example.BuildConfig

object SafeLog {
    private const val TAG = "PhoneCheckPro"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            try {
                Log.d(TAG, message)
            } catch (_: Throwable) {}
        }
    }

    fun i(message: String) {
        try {
            Log.i(TAG, message)
        } catch (_: Throwable) {}
    }

    fun w(message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                Log.w(TAG, message, throwable)
            } else {
                Log.w(TAG, message)
            }
        } catch (_: Throwable) {}
    }

    fun e(message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        } catch (_: Throwable) {}
    }
}
