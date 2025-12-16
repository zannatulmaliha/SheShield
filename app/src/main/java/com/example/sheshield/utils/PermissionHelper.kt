//// Add this file: CallPermissionHelper.kt
//package com.example.sheshield.utils
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import androidx.core.content.ContextCompat
//import android.Manifest
//import android.content.pm.PackageManager
//
//object CallPermissionHelper {
//
//    fun makeCall(context: Context, phoneNumber: String) {
//        val intent = Intent(Intent.ACTION_CALL).apply {
//            data = Uri.parse("tel:$phoneNumber")
//        }
//
//        // Check if we have permission
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.CALL_PHONE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // We have permission - make direct call
//            context.startActivity(intent)
//        } else {
//            // No permission - open dialer instead (no permission needed)
//            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
//                data = Uri.parse("tel:$phoneNumber")
//            }
//            context.startActivity(dialIntent)
//        }
//    }
//}

package com.example.sheshield.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

// Simple permission checker
fun hasCallPermission(context: Context): Boolean {
    return android.content.pm.PackageManager.PERMISSION_GRANTED ==
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            )
}

// Direct call function
fun makeDirectCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}
