package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
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
    private lateinit var statusText: TextView

    private var selectedSeat = -1
    private var micOn = false
    private var speakerOn = true

    private val gifts = arrayOf(
        "🌹 Rose — 10 coins",
        "❤️ Heart — 50 coins",
        "💐 Flowers — 100 coins",
        "🎂 Birthday Cake — 500 coins",
        "💎 Diamond — 1,000 coins",
        "👑 Royal Crown — 5,000 coins"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showRoom()
    }

    override fun onBackPressed() {
        showRoomSwitchMenu()
    }

    private fun showRoom() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(24))
        }

        scroll.addView(root)

        // Header with top-right switch button
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = makeText("‹", 32f, cyan, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { showRoomSwitchMenu() }
        }

        header.addView(back, LinearLayout.LayoutParams(dp(40), dp(48)))

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(makeText("KINGDOM VOICE ROOM", 16f, white, true))
            addView(makeText("Room ID: KVC-1001", 11f, cyan, false))
        }

        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))

        val switchButton = makeText("⇄", 25f, white, true).apply {
            gravity = Gravity.CENTER
            background = gradient(intArrayOf(purple, Color.rgb(48, 42, 105)), 14)
            setOnClickListener { showRoomSwitchMenu() }
        }

        val switchParams = LinearLayout.LayoutParams(dp(54), dp(48))
        header.addView(switchButton, switchParams)
        root.addView(header)

        // Room banner
        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(18), dp(17), dp(18))
            background = gradient(
                intArrayOf(Color.rgb(56, 35, 115), Color.rgb(16, 91, 120)),
                22
            )
        }

        banner.addView(
            makeText("🎙 Welcome to Kingdom Voice Chat", 19f, white, true)
        )

        val bannerInfo = makeText(
            "Choose a mic seat, open the gift box and select a gift.",
            12f, white, false
        )
        bannerInfo.setPadding(0, dp(8), 0, dp(12))
        banner.addView(bannerInfo)
        banner.addView(makeText("👥 16 MIC SEATS", 12f, cyan, true))

        val bannerParams = LinearLayout.LayoutParams(-1, -2)
        bannerParams.topMargin = dp(16)
        root.addView(banner, bannerParams)

        statusText = makeText(
            "Select a seat before sending a gift.",
            12f, gold, false
        )
        statusText.setPadding(dp(3), dp(16), dp(3), dp(12))
        root.addView(statusText)

        // Seats
        val seatHeading = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(
                makeText("MIC SEATS", 18f, white, true),
                LinearLayout.LayoutParams(0, -2, 1f)
            )
            addView(makeText("01–16", 11f, cyan, true))
        }
        root.addView(seatHeading)

        seatsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        refreshSeats()
        root.addView(seatsLayout)

        // Gift box
        val giftBox = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = gradient(
                intArrayOf(Color.rgb(89, 35, 112), Color.rgb(38, 31, 78)),
                20
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { showGiftBox() }
        }

        val giftIcon = makeText("🎁", 36f, pink, true)
        giftBox.addView(giftIcon, LinearLayout.LayoutParams(dp(54), dp(54)))

        val giftInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(makeText("GIFT BOX", 17f, white, true))
            addView(makeText("Select a gift to send", 12f, muted, false))
        }

        giftBox.addView(giftInfo, LinearLayout.LayoutParams(0, -2, 1f))
        giftBox.addView(makeText("›", 30f, cyan, true))

        val giftParams = LinearLayout.LayoutParams(-1, -2)
        giftParams.topMargin = dp(18)
        root.addView(giftBox, giftParams)

        // Mic and speaker controls
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val mic = controlButton("🎙", "Mic ${if (micOn) "ON" else "OFF"}", purple) {
            micOn = !micOn
            statusText.text = if (micOn) {
                "Mic enabled locally. Live voice service is not connected."
            } else {
                "Mic disabled locally."
            }
            showRoom()
        }

        val speaker = controlButton(
            if (speakerOn) "🔊" else "🔇",
            if (speakerOn) "Speaker ON" else "Speaker OFF",
            cyan
        ) {
            speakerOn = !speakerOn
            showRoom()
        }

        val micParams = LinearLayout.LayoutParams(0, dp(72), 1f)
        micParams.setMargins(0, dp(14), dp(6), 0)
        controls.addView(mic, micParams)

        val speakerParams = LinearLayout.LayoutParams(0, dp(72), 1f)
        speakerParams.setMargins(dp(6), dp(14), 0, 0)
        controls.addView(speaker, speakerParams)

        root.addView(controls)

        val footer = makeText(
            "KINGDOM VOICE CHAT",
            10f, muted, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, 0)
        }
        root.addView(footer)

        setContentView(scroll)
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
                val selected = number == selectedSeat

                val seat = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(3), dp(8), dp(3), dp(8))
                    background = gradient(
                        if (selected) intArrayOf(
                            Color.rgb(42, 78, 116), purple
                        ) else intArrayOf(card, Color.rgb(20, 23, 49)),
                        15
                    )
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        selectedSeat = if (selectedSeat == number) -1 else number
                        statusText.text = if (selectedSeat == -1) {
                            "No seat selected. Select a seat before sending a gift."
                        } else {
                            "Seat $selectedSeat selected. Open the gift box to choose a gift."
                        }
                        refreshSeats()
                    }
                }

                val icon = makeText(
                    if (selected) "👤" else "🎙",
                    23f, if (selected) cyan else purple, true
                ).apply { gravity = Gravity.CENTER }

                seat.addView(icon)

                seat.addView(
                    makeText(
                        if (selected) "You" else "Seat $number",
                        10f, white, true
                    ).apply { gravity = Gravity.CENTER }
                )

                seat.addView(
                    makeText(
                        if (selected) "Selected" else "Empty",
                        9f, if (selected) cyan else muted, false
                    ).apply {
                        gravity = Gravity.CENTER
                        setPadding(0, dp(3), 0, 0)
                    }
                )

                val params = LinearLayout.LayoutParams(0, dp(100), 1f)
                params.setMargins(dp(3), dp(4), dp(3), dp(4))
                row.addView(seat, params)
            }

            seatsLayout.addView(row)
        }
    }

    private fun showGiftBox() {
        if (selectedSeat == -1) {
            AlertDialog.Builder(this)
                .setTitle("Select a seat")
                .setMessage("Gift bhejne se pehle 16 mic seats mein se ek seat select karein.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("🎁 Choose a Gift for Seat $selectedSeat")
            .setItems(gifts) { _, which ->
                confirmGift(gifts[which], selectedSeat)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun confirmGift(gift: String, seatNumber: Int) {
        AlertDialog.Builder(this)
            .setTitle("Send Gift?")
            .setMessage("Gift: $gift\nRecipient: Seat $seatNumber\n\nThis is a demo. No coins will be deducted.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SEND DEMO GIFT") { _, _ ->
                Toast.makeText(
                    this,
                    "$gift selected for Seat $seatNumber. Backend pending.",
                    Toast.LENGTH_LONG
                ).show()

                statusText.text =
                    "Demo gift selected for Seat $seatNumber. No coins deducted."
            }
            .show()
    }

    private fun showRoomSwitchMenu() {
        val options = arrayOf(
            "Minimize Room",
            "Exit Room",
            "Cancel"
        )

        AlertDialog.Builder(this)
            .setTitle("Room Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> moveTaskToBack(true)
                    1 -> confirmExit()
                    2 -> Unit
                }
            }
            .show()
    }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("Exit Room?")
            .setMessage("Room screen se bahar jana chahte hain?")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("EXIT") { _, _ ->
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
        return makeText("$icon\n$label", 13f, white, true).apply {
            gravity = Gravity.CENTER
            background = gradient(intArrayOf(card, bg), 17)
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
