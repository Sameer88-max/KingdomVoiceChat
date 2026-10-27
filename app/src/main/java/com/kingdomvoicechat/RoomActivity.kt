package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class RoomActivity : Activity() {

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val cyan = Color.rgb(69, 226, 235)
    private val purple = Color.rgb(150, 102, 255)
    private val pink = Color.rgb(255, 105, 175)
    private val gold = Color.rgb(255, 190, 86)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 181, 215)

    private lateinit var root: LinearLayout
    private lateinit var seatsLayout: LinearLayout
    private lateinit var micButton: TextView
    private lateinit var speakerButton: TextView
    private lateinit var statusText: TextView

    private var micOn = false
    private var speakerOn = true
    private var selectedSeat = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        showRoom()
    }

    override fun onBackPressed() {
        leaveRoom()
    }

    private fun showRoom() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(24))
        }

        scroll.addView(root)

        // Room header
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = makeText("‹", 34f, cyan, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { leaveRoom() }
        }

        header.addView(back, LinearLayout.LayoutParams(dp(45), dp(50)))

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        titleBox.addView(
            makeText("KINGDOM VOICE ROOM", 17f, white, true)
        )
        titleBox.addView(
            makeText("Room ID: KVC-1001", 11f, cyan, false)
        )

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val menu = makeText("⋮", 28f, white, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { showRoomMenu() }
        }

        header.addView(menu, LinearLayout.LayoutParams(dp(35), dp(50)))
        root.addView(header)

        // Room information
        val roomCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = gradient(
                intArrayOf(Color.rgb(56, 35, 115), Color.rgb(16, 91, 120)),
                22
            )
        }

        roomCard.addView(
            makeText("🎙  Welcome to Kingdom Voice Chat", 19f, white, true)
        )

        val roomDescription = makeText(
            "Meet friends, choose a seat and enjoy the room.",
            13f, white, false
        )
        roomDescription.setPadding(0, dp(9), 0, dp(12))
        roomCard.addView(roomDescription)

        roomCard.addView(
            makeText("👥  1 local user  •  16 seats", 12f, cyan, true)
        )

        val roomParams = LinearLayout.LayoutParams(-1, -2)
        roomParams.topMargin = dp(18)
        root.addView(roomCard, roomParams)

        // Status
        statusText = makeText(
            "Choose a seat to select it. Live voice is not connected yet.",
            12f, gold, false
        )
        statusText.setPadding(dp(4), dp(16), dp(4), dp(8))
        root.addView(statusText)

        // Seat section heading
        val seatHeading = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        seatHeading.addView(
            makeText("MIC SEATS", 18f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        seatHeading.addView(
            makeText("01–16", 11f, cyan, true)
        )

        val headingParams = LinearLayout.LayoutParams(-1, -2)
        headingParams.topMargin = dp(10)
        headingParams.bottomMargin = dp(14)
        root.addView(seatHeading, headingParams)

        // 4 rows, 4 seats per row
        seatsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        for (rowIndex in 0 until 4) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            for (columnIndex in 0 until 4) {
                val seatNumber = rowIndex * 4 + columnIndex + 1
                val seat = createSeat(seatNumber)

                val params = LinearLayout.LayoutParams(0, dp(105), 1f)
                params.setMargins(dp(4), dp(4), dp(4), dp(4))
                row.addView(seat, params)
            }

            seatsLayout.addView(
                row,
                LinearLayout.LayoutParams(-1, -2)
            )
        }

        root.addView(seatsLayout)

        // Current user indicator
        val userCard = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = gradient(intArrayOf(card, bg), 18)
        }

        val userIcon = makeText("👤", 25f, cyan, true)
        userCard.addView(
            userIcon,
            LinearLayout.LayoutParams(dp(45), dp(45))
        )

        val userInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(makeText("You", 15f, white, true))
            addView(makeText("Local device", 11f, muted, false))
        }

        userCard.addView(
            userInfo,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        userCard.addView(
            makeText("MEMBER", 10f, cyan, true)
        )

        val userParams = LinearLayout.LayoutParams(-1, -2)
        userParams.topMargin = dp(16)
        root.addView(userCard, userParams)

        // Controls
        val controlsTitle = makeText("ROOM CONTROLS", 16f, white, true)
        controlsTitle.setPadding(dp(2), dp(22), 0, dp(12))
        root.addView(controlsTitle)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        micButton = controlButton("🎙", "Mic OFF", purple) {
            micOn = !micOn
            updateMicButton()
        }

        speakerButton = controlButton("🔊", "Speaker ON", cyan) {
            speakerOn = !speakerOn
            updateSpeakerButton()
        }

        val micParams = LinearLayout.LayoutParams(0, dp(85), 1f)
        micParams.setMargins(dp(4), 0, dp(6), 0)
        controls.addView(micButton, micParams)

        val speakerParams = LinearLayout.LayoutParams(0, dp(85), 1f)
        speakerParams.setMargins(dp(6), 0, dp(4), 0)
        controls.addView(speakerButton, speakerParams)

        root.addView(controls)

        val leave = controlButton("🚪", "Leave Room", pink) {
            leaveRoom()
        }

        val leaveParams = LinearLayout.LayoutParams(-1, dp(65))
        leaveParams.topMargin = dp(14)
        root.addView(leave, leaveParams)

        val footer = makeText(
            "KINGDOM VOICE CHAT • ROOM DEMO",
            10f, muted, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(4))
        }

        root.addView(footer)

        setContentView(scroll)
    }

    private fun createSeat(number: Int): LinearLayout {
        val occupied = number == selectedSeat

        val seat = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(8), dp(4), dp(8))
            background = gradient(
                if (occupied) {
                    intArrayOf(
                        Color.rgb(44, 78, 115),
                        Color.rgb(32, 38, 78)
                    )
                } else {
                    intArrayOf(card, Color.rgb(20, 23, 49))
                },
                16
            )
            isClickable = true
            isFocusable = true
        }

        val icon = makeText(
            if (occupied) "👤" else "🎙",
            25f,
            if (occupied) cyan else purple,
            true
        )
        icon.gravity = Gravity.CENTER
        seat.addView(icon)

        val seatLabel = makeText(
            if (occupied) "You" else "Seat $number",
            11f,
            white,
            true
        )
        seatLabel.gravity = Gravity.CENTER
        seatLabel.setPadding(0, dp(7), 0, 0)
        seat.addView(seatLabel)

        val state = makeText(
            if (occupied) "Selected" else "Empty",
            9f,
            if (occupied) cyan else muted,
            false
        )
        state.gravity = Gravity.CENTER
        state.setPadding(0, dp(3), 0, 0)
        seat.addView(state)

        seat.setOnClickListener {
            if (selectedSeat == number) {
                selectedSeat = -1
                statusText.text =
                    "Seat released. Choose a seat to select it."
            } else {
                selectedSeat = number
                statusText.text =
                    "Seat $number selected on this device."
            }

            refreshSeats()
        }

        return seat
    }

    private fun refreshSeats() {
        seatsLayout.removeAllViews()

        for (rowIndex in 0 until 4) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            for (columnIndex in 0 until 4) {
                val number = rowIndex * 4 + columnIndex + 1
                val params = LinearLayout.LayoutParams(0, dp(105), 1f)
                params.setMargins(dp(4), dp(4), dp(4), dp(4))
                row.addView(createSeat(number), params)
            }

            seatsLayout.addView(
                row,
                LinearLayout.LayoutParams(-1, -2)
            )
        }
    }

    private fun updateMicButton() {
        micButton.text = if (micOn) {
            "🎙\nMic ON"
        } else {
            "🎙\nMic OFF"
        }

        statusText.text = if (micOn) {
            "Mic button enabled locally. Live audio is not connected."
        } else {
            "Mic is off locally. Live audio is not connected."
        }

        micButton.background = gradient(
            if (micOn) intArrayOf(pink, purple)
            else intArrayOf(card, bg),
            18
        )
    }

    private fun updateSpeakerButton() {
        speakerButton.text = if (speakerOn) {
            "🔊\nSpeaker ON"
        } else {
            "🔇\nSpeaker OFF"
        }

        statusText.text = if (speakerOn) {
            "Speaker control selected. Live audio is not connected."
        } else {
            "Speaker control disabled locally. Live audio is not connected."
        }

        speakerButton.background = gradient(
            if (speakerOn) intArrayOf(Color.rgb(18, 95, 112), card)
            else intArrayOf(card, bg),
            18
        )
    }

    private fun showRoomMenu() {
        val options = arrayOf(
            "Room Information",
            "Refresh Room Screen",
            "Leave Room"
        )

        AlertDialog.Builder(this)
            .setTitle("Room Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> AlertDialog.Builder(this)
                        .setTitle("Room Information")
                        .setMessage(
                            "Kingdom Voice Chat\nRoom ID: KVC-1001\n16 demo seats\nLive voice service not connected."
                        )
                        .setPositiveButton("OK", null)
                        .show()

                    1 -> showRoom()

                    2 -> leaveRoom()
                }
            }
            .show()
    }

    private fun leaveRoom() {
        AlertDialog.Builder(this)
            .setTitle("Leave Room?")
            .setMessage("Do you want to return to the Home Screen?")
            .setNegativeButton("Stay", null)
            .setPositiveButton("Leave") { _, _ ->
                finish()
            }
            .show()
    }

    private fun controlButton(
        icon: String,
        label: String,
        accent: Int,
        action: () -> Unit
    ): TextView {
        return makeText("$icon\n$label", 14f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), dp(10))
            background = gradient(intArrayOf(card, bg), 18)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun gradient(
        colors: IntArray,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            colors
        ).apply {
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
