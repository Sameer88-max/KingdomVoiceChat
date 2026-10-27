
package com.kingdomvoicechat

import android.app.Activity
import android.animation.ValueAnimator
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.animation.LinearInterpolator
import android.widget.*
import android.text.InputType

class AuthActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("kingdom_auth_demo", MODE_PRIVATE)
    }

    private val bg = Color.rgb(10, 12, 32)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 198, 225)
    private val cyan = Color.rgb(64, 235, 240)
    private val purple = Color.rgb(155, 91, 255)
    private val pink = Color.rgb(255, 78, 180)

    private lateinit var page: LinearLayout
    private lateinit var form: LinearLayout
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var heading: TextView
    private lateinit var subtitle: TextView
    private lateinit var action: TextView
    private lateinit var switchText: TextView
    private var registering = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        registering = !prefs.getBoolean("registered", false)
        showScreen()
        animateColours()
    }

    private fun showScreen() {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.isFillViewport = true

        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(35), dp(24), dp(30))
        }
        scroll.addView(page)

        val crown = label("♛", 52f, cyan, true)
        crown.gravity = Gravity.CENTER
        page.addView(crown, LinearLayout.LayoutParams(-1, dp(70)))

        val brand = label("KINGDOM", 30f, white, true)
        brand.gravity = Gravity.CENTER
        page.addView(brand)

        val tagline = label(
            "VOICE  •  CHAT  •  CONNECT", 11f, cyan, true
        )
        tagline.gravity = Gravity.CENTER
        page.addView(tagline)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(22))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(39, 35, 87),
                    Color.rgb(16, 36, 65)
                )
            ).apply {
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), Color.rgb(100, 100, 190))
            }
        }

        val cardParams = LinearLayout.LayoutParams(-1, -2)
        cardParams.topMargin = dp(28)
        page.addView(card, cardParams)

        heading = label("", 24f, white, true)
        subtitle = label("", 13f, muted, false)
        subtitle.setPadding(0, dp(8), 0, dp(22))

        card.addView(heading)
        card.addView(subtitle)

        form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        card.addView(form)

        email = field(
            "Email address",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        password = field(
            "Password",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        confirm = field(
            "Confirm password",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        action = label("", 14f, white, true).apply {
            gravity = Gravity.CENTER
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(purple, pink, cyan)
            ).apply {
                cornerRadius = dp(15).toFloat()
            }
        }

        val actionParams = LinearLayout.LayoutParams(-1, dp(54))
        actionParams.topMargin = dp(10)
        card.addView(action, actionParams)
        action.setOnClickListener { submit() }

        switchText = label("", 13f, cyan, true)
        switchText.gravity = Gravity.CENTER
        switchText.setPadding(0, dp(22), 0, dp(4))
        card.addView(switchText)

        switchText.setOnClickListener {
            registering = !registering
            showForm()
        }

        val footer = label(
            "EMAIL LOGIN  •  NO OTP REQUIRED",
            11f, muted, true
        )
        footer.gravity = Gravity.CENTER
        footer.setPadding(0, dp(25), 0, 0)
        page.addView(footer)

        setContentView(scroll)
        showForm()
    }

    private fun showForm() {
        heading.text = if (registering)
            "Create Account" else "Welcome Back"

        subtitle.text = if (registering)
            "Register once with your email and password."
        else
            "Log in with your registered account."

        form.removeAllViews()
        form.addView(email)
        form.addView(password)

        if (registering) form.addView(confirm)

        action.text = if (registering)
            "CREATE ACCOUNT" else "LOG IN"

        switchText.text = if (registering)
            "Already registered?  Log in"
        else
            "New here?  Create account"
    }

    private fun submit() {
        val e = email.text.toString().trim().lowercase()
        val p = password.text.toString()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            toast("Valid email address enter karo.")
            return
        }

        if (p.length < 8) {
            toast("Password at least 8 characters ka hona chahiye.")
            return
        }

        if (registering) {
            if (p != confirm.text.toString()) {
                toast("Passwords match nahi karte.")
                return
            }

            // Temporary UI demo only.
            // Real registration and secure password storage
            // must be implemented by the backend.
            prefs.edit()
                .putBoolean("registered", true)
                .putString("registered_email", e)
                .apply()

            registering = false
            email.setText(e)
            password.setText("")
            showForm()
            toast("Registration demo complete. Ab login karo.")
        } else {
            val savedEmail = prefs.getString("registered_email", null)

            if (savedEmail == null || e != savedEmail) {
                toast("Pehle isi device par registration karo.")
                return
            }

            toast("Login verification backend connect hone ke baad chalegi.")
        }
    }

    private fun field(hintText: String, type: Int): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 15f
            inputType = type
            setSingleLine(true)
            setPadding(dp(14), dp(8), dp(14), dp(8))

            background = GradientDrawable().apply {
                setColor(Color.rgb(17, 21, 48))
                cornerRadius = dp(13).toFloat()
                setStroke(dp(1), Color.rgb(75, 83, 135))
            }

            layoutParams = LinearLayout.LayoutParams(-1, dp(54)).apply {
                bottomMargin = dp(12)
            }
        }
    }

    private fun animateColours() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()

            addUpdateListener {
                val value = it.animatedValue as Float
                val colour = blend(purple, cyan, value)

                window.statusBarColor = blend(bg, colour, 0.4f)
                window.navigationBarColor = blend(bg, pink, value * 0.3f)
            }

            start()
        }
    }

    private fun blend(a: Int, b: Int, ratio: Float): Int {
        val r = ratio.coerceIn(0f, 1f)

        return Color.rgb(
            (Color.red(a) * (1-r) + Color.red(b) * r).toInt(),
            (Color.green(a) * (1-r) + Color.green(b) * r).toInt(),
            (Color.blue(a) * (1-r) + Color.blue(b) * r).toInt()
        )
    }

    private fun label(
        value: String,
        size: Float,
        colour: Int,
        bold: Boolean
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(colour)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
