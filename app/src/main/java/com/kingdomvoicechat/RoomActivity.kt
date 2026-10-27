
package com.kingdomvoicechat

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class RoomActivity : Activity() {

    private val bg = Color.rgb(4, 10, 34)
    private val panel = Color.rgb(10, 22, 57)
    private val cyan = Color.rgb(35, 226, 255)
    private val purple = Color.rgb(153, 69, 255)
    private var muted = false
    private var roomLocked = false

    private lateinit var room: LinearLayout
    private lateinit var muteButton: Button
    private lateinit var lockButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        room = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 24)
        }

        scroll.addView(room)
        setContentView(scroll)

        addText("♛ KINGDOM VOICE CHAT", 25, cyan, true, Gravity.CENTER)
        addText("ROYAL LIVE ROOM  •  16 MIC SEATS", 13,
            Color.LTGRAY, false, Gravity.CENTER)

        addSpace(18)
        addText("KINGDOM VIBES 👑", 23, Color.WHITE, true)
        addText("Room ID: 45872  •  Good Vibes • Family",
            14, cyan, false)

        addSpace(14)
        addCard {
            addText("ROOM INFORMATION", 14, cyan, true)
            addText("Host: King 👑", 17, Color.WHITE, true)
            addText("Room: Personal room", 14, Color.LTGRAY)
            addText("Mic Seats: 16", 14, Color.LTGRAY)
            addText("Cover: Room owner can customize", 14, Color.LTGRAY)
            addText("Background: Owner-approved options", 14, Color.LTGRAY)
        }

        addSpace(14)
        addText("MIC STAGE", 19, Color.WHITE, true)
        addText("16 seats • Tap a seat to view its status",
            13, Color.LTGRAY)

        val names = listOf(
            "King 👑", "Queen ❤️", "Zain", "Ayesha",
            "Rana", "Ali", "Sara", "Usman",
            "Hina", "Zoya", "Fahad", "Laiba",
            "Tayyab", "Mona", "Kashif", "Sana"
        )

        val roles = listOf(
            "HOST", "CO-HOST", "MOD", "MOD",
            "USER", "USER", "USER", "USER",
            "USER", "USER", "USER", "USER",
            "USER", "USER", "USER", "USER"
        )

        for (rowIndex in 0 until 4) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            for (column in 0 until 4) {
                val index = rowIndex * 4 + column

                val seat = TextView(this).apply {
                    text = "◉\n${index + 1}\n${names[index]}\n${roles[index]}"
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    setPadding(3, 12, 3, 12)
                    background = shape(
                        panel, if (index < 4) purple else cyan, 18
                    )
                    setOnClickListener {
                        Toast.makeText(
                            this@RoomActivity,
                            "${names[index]} • ${roles[index]}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                val params = LinearLayout.LayoutParams(
                    0, 112, 1f
                ).apply {
                    setMargins(3, 5, 3, 5)
                }

                row.addView(seat, params)
            }

            room.addView(row)
        }

        addSpace(12)
        addCard {
            addText("ROOM CONTROLS", 15, cyan, true)

            muteButton = makeButton("🎙 Mute")
            muteButton.setOnClickListener {
                muted = !muted
                muteButton.text = if (muted) "🔇 Unmute" else "🎙 Mute"
                Toast.makeText(
                    this@RoomActivity,
                    if (muted) "Local mute UI enabled"
                    else "Local mute UI disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            addView(muteButton, matchParams())

            val speaker = makeButton("🔊 Speaker")
            speaker.setOnClickListener {
                Toast.makeText(
                    this@RoomActivity,
                    "Audio routing integration pending",
                    Toast.LENGTH_SHORT
                ).show()
            }
            addView(speaker, matchParams())

            val gift = makeButton("🎁 SEND GIFT")
            gift.setOnClickListener {
                Toast.makeText(
                    this@RoomActivity,
                    "Gift Box UI — next integration step",
                    Toast.LENGTH_SHORT
                ).show()
            }
            addView(gift, matchParams())

            lockButton = makeButton("🔓 Room Lock")
            lockButton.setOnClickListener {
                roomLocked = !roomLocked
                lockButton.text =
                    if (roomLocked) "🔒 Room Locked" else "🔓 Room Lock"

                Toast.makeText(
                    this@RoomActivity,
                    "Demo control only; server permission required",
                    Toast.LENGTH_SHORT
                ).show()
            }
            addView(lockButton, matchParams())
        }

        addSpace(14)
        addCard {
            addText("GIFT BOX 🎁", 19, cyan, true)
            addText("Normal Gifts (100+)", 15, Color.WHITE, true)
            addText("Rose • 10 coins", 14, Color.LTGRAY)
            addText("Heart • 50 coins", 14, Color.LTGRAY)
            addText("Diamond • 1,000 coins", 14, Color.LTGRAY)
            addText("Castle • 50,000 coins", 14, Color.LTGRAY)
            addText("Lucky Gifts (50)", 15, Color.WHITE, true)
            addText("Lucky gift range: 1,000–1,000,000 coins",
                14, Color.LTGRAY)
            addText("Normal gift reward: 50% in Pearls",
                14, cyan)
            addText("Lucky gift reward: 5% in Pearls",
                14, cyan)
            addText("Combo: 0/50  •  Jackpot: Owner-configured",
                14, Color.YELLOW)
        }

        addSpace(14)
        addCard {
            addText("CHAT  •  MEMBERS  •  GIFT HISTORY",
                14, cyan, true)
            addText("Welcome to Kingdom Voice Chat! ❤️",
                14, Color.WHITE)
            addText("Chat service integration pending",
                13, Color.LTGRAY)
        }

        addSpace(16)
        val back = makeButton("← BACK TO HOME")
        back.setOnClickListener { finish() }
        room.addView(back, matchParams())
    }

    private fun addCard(content: LinearLayout.() -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 14)
            background = shape(panel, purple, 18)
        }
        card.content()
        room.addView(card, matchParams())
    }

    private fun addText(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean = false,
        gravity: Int = Gravity.START
    ) {
        val text = TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            this.gravity = gravity
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(4, 5, 4, 5)
        }
        room.addView(
            text,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addSpace(height: Int) {
        room.addView(
            View(this),
            LinearLayout.LayoutParams(1, height)
        )
    }

    private fun makeButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            background = shape(
                Color.rgb(39, 28, 105), cyan, 28
            )
        }
    }

    private fun shape(
        color: Int,
        stroke: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            setStroke(2, stroke)
        }
    }

    private fun matchParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 5, 0, 5)
        }
    }
}
