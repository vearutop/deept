package com.example.deept

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.security.MessageDigest
import android.content.ClipboardManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DeepLinkContent(intent?.data)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setContent {
            DeepLinkContent(intent?.data)
        }
    }
}

@Composable
fun DeepLinkContent(uri: Uri?) {
    val context = LocalContext.current
    val sha256Fingerprint = getSHA256CertificateFingerprint(context)


    Column {
        if (uri != null) {
            Text(text = "App opened with:")
            Text(text = uri.toString() ?: "No deep link")
            Button(onClick = { copyToClipboard(context, uri.toString()) }) {
                Text("Copy deeplink to Clipboard")
            }
        } else {
            Text(text = "No deep link")
        }

        Text(text = "SHA256 Fingerprint:")
        if (sha256Fingerprint != null) {
            Text(text = sha256Fingerprint)
            Button(onClick = { copyToClipboard(context, sha256Fingerprint) }) {
                Text("Copy SHA256 to Clipboard")
            }
        }
    }
}


fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("SHA256 Fingerprint", text)
    clipboardManager.setPrimaryClip(clipData)
}

fun getSHA256CertificateFingerprint(context: Context): String? {
    return try {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)

        val signingInfo = packageInfo.signingInfo // Create a local copy
        if (signingInfo != null && signingInfo.signingCertificateHistory != null) {
            val signatures = signingInfo.signingCertificateHistory
            if (signatures.isNotEmpty()) {
                val certificate = signatures[0]
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(certificate.toByteArray())
                bytesToHex(digest).uppercase()
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun bytesToHex(bytes: ByteArray): String {
    val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    val hexChars = CharArray(bytes.size * 3)
    var j = 0
    for (i in bytes.indices) {
        val v = bytes[i].toInt() and 0xFF
        hexChars[j++] = hexArray[v ushr 4]
        hexChars[j++] = hexArray[v and 0x0F]
        hexChars[j++] = ':'
    }
    return String(hexChars)
}