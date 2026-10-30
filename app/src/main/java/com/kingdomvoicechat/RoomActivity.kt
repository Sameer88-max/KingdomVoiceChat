package com.kingdomvoicechat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomConfig
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomFragment

class RoomActivity : AppCompatActivity() {

    companion object {
        private const val APP_ID = 976076264L
        private val appSign: String
    get() = BuildConfig.ZEGO_APP_SIGN
        private const val MIC_PERMISSION = 1001
    }

    private var roomStarted = false
    private lateinit var roomContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomContainer = FrameLayout(this).apply {
            id = View.generateViewId()
        }

        setContentView(roomContainer)

        if (APP_SIGN == "PASTE_YOUR_ZEGO_APPSIGN_HERE") {
            Toast.makeText(
                this,
                "ZEGOCLOUD AppSign is required",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        checkMicrophonePermission()
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            joinVoiceRoom()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == MIC_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                joinVoiceRoom()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun joinVoiceRoom() {
        if (roomStarted) return
        roomStarted = true

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val userID = (
            firebaseUser?.uid ?: "guest_${System.currentTimeMillis()}"
        ).replace(Regex("[^A-Za-z0-9_]"), "_")

        val userName = (
            firebaseUser?.displayName ?: "Kingdom_User"
        ).replace(Regex("[^A-Za-z0-9_]"), "_")

        val roomID = (
            intent.getStringExtra("roomID") ?: "KVC_1001"
        ).replace(Regex("[^A-Za-z0-9_]"), "_")

        val isHost = intent.getBooleanExtra("host", false)

        val config = if (isHost) {
            ZegoUIKitPrebuiltLiveAudioRoomConfig.host()
        } else {
            ZegoUIKitPrebuiltLiveAudioRoomConfig.audience()
        }

        val fragment =
            ZegoUIKitPrebuiltLiveAudioRoomFragment.newInstance(
                APP_ID,
                APP_SIGN,
                userID,
                userName,
                roomID,
                config
            )

        supportFragmentManager.beginTransaction()
            .replace(roomContainer.id, fragment)
            .commit()
    }
}
