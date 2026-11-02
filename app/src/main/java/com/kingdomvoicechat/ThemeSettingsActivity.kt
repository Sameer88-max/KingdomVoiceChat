
package com.kingdomvoicechat

import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ThemeSettingsActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout

    private val themes = listOf(
        "Royal Blue",
        "Midnight Purple",
        "Ocean Cyan",
        "Dark Red",
        "Emerald"
    )

    private val cardStyles = listOf(
        "GLASS",
        "SOLID",
        "OUTLINED"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KingdomTheme.applyTo(this)
        createScreen()
    }

    private fun createScreen() {
        val scroll = ScrollView(this)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 20, 18, 30)
            setBackgroundColor(KingdomTheme.BACKGROUND)
        }

        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = KingdomTheme.heading(
            this,
            "Appearance Settings"
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val back = TextView(this).apply {
            text = "✕"
            textSize = 22f
            setTextColor(KingdomTheme.CYAN)
            setPadding(15, 10, 15, 10)
            setOnClickListener { finish() }
        }

        header.addView(back)
        root.addView(header)

        root.addView(
            KingdomTheme.text(
                this,
                "Personalize your Kingdom Voice Chat experience.",
                13f,
                KingdomTheme.MUTED
            )
        )

        addSectionTitle("Choose App Theme")

        themes.forEach { theme ->
            addOption(
                theme,
                KingdomTheme.selectedTheme(this) == theme
            ) {
                KingdomTheme.saveTheme(this, theme)
                refreshScreen()
                Toast.makeText(
                    this,
                    "$theme selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        addSectionTitle("Card Design Style")

        cardStyles.forEach { style ->
            addOption(
                style,
                KingdomTheme.selectedCardStyle(this) == style
            ) {
                KingdomTheme.saveCardStyle(this, style)
                refreshScreen()
                Toast.makeText(
                    this,
                    "$style card style selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        addSectionTitle("Live Preview")

        val preview = KingdomTheme.card(this)

        preview.addView(
            KingdomTheme.text(
                this,
                "KINGDOM VOICE CHAT",
                18f,
                KingdomTheme.CYAN,
                true
            )
        )

        preview.addView(
            KingdomTheme.text(
                this,
                "Your selected theme preview.",
                13f,
                KingdomTheme.MUTED
            )
        )

        root.addView(
            preview,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = 10
            }
        )

        val done = KingdomTheme.button(
            this,
            "DONE"
        )

        done.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        root.addView(
            done,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = 24
            }
        )
    }

    private fun addSectionTitle(value: String) {
        root.addView(
            KingdomTheme.heading(this, value)
        )
    }

    private fun addOption(
        label: String,
        selected: Boolean,
        action: () -> Unit
    ) {
        val option = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 15, 16, 15)
            background = KingdomTheme.background(
                KingdomTheme.CARD,
                18f,
                if (selected) KingdomTheme.CYAN
                else KingdomTheme.CARD_LIGHT
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        val name = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(KingdomTheme.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        option.addView(
            name,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val mark = TextView(this).apply {
            text = if (selected) "✓" else "○"
            textSize = 20f
            setTextColor(KingdomTheme.CYAN)
        }

        option.addView(mark)

        root.addView(
            option,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = 8
            }
        )
    }

    private fun refreshScreen() {
        KingdomTheme.applyTo(this)
        createScreen()
    }

    override fun onResume() {
        super.onResume()
        KingdomTheme.applyTo(this)
    }
}
