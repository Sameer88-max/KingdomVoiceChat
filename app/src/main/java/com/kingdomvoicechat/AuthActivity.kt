package com.kingdomvoicechat

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import android.text.InputType
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var form: LinearLayout
    private lateinit var heading: TextView
    private lateinit var subtitle: TextView
    private lateinit var action: TextView
    private lateinit var switchText: TextView

    private var registering = true
    private var busy = false

    private val bg = Color.rgb(10, 12, 32)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 198, 225)
    private val cyan = Color.rgb(64, 235, 240)
    private val purple = Color.rgb(155, 91, 255)
    private val pink = Color.rgb(255, 78, 180)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        window.statusBarColor = bg
        window.navigationBarColor = bg

        registering = auth.currentUser == null
        buildScreen()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(40), dp(24), dp(30))
        }
        scroll.addView(page)

        page.addView(label("♛", 52f, cyan, true).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, dp(70)))

        page.addView(label("KINGDOM", 30f, white, true).apply {
            gravity = Gravity.CENTER
        })

        page.addView(label(
            "VOICE  •  CHAT  •  CONNECT", 11f, cyan, true
        ).apply {
            gravity = Gravity.CENTER
        })

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

        page.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(28)
        })

        heading = label("", 24f, white, true)
        subtitle = label("", 13f, muted, false).apply {
            setPadding(0, dp(8), 0, dp(22))
        }

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

        confirmPassword = field(
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

        card.addView(action, LinearLayout.LayoutParams(-1, dp(54)).apply {
            topMargin = dp(10)
        })
        action.setOnClickListener { submit() }

        switchText = label("", 13f, cyan, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(4))
        }

        card.addView(switchText)
        switchText.setOnClickListener {
            if (!busy) {
                registering = !registering
                updateForm()
            }
        }

        page.addView(label(
            "SECURE EMAIL LOGIN • FIREBASE AUTH",
            11f, muted, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, 0)
        })

        setContentView(scroll)
        updateForm()
    }

    private fun updateForm() {
        heading.text = if (registering)
            "Create Account" else "Welcome Back"

        subtitle.text = if (registering)
            "Register with your email and password."
        else
            "Log in with your registered account."

        form.removeAllViews()
        form.addView(email)
        form.addView(password)

        if (registering) {
            form.addView(confirmPassword)
        }

        action.text = if (registering)
            "CREATE ACCOUNT" else "LOG IN"

        switchText.text = if (registering)
            "Already registered? Log in"
        else
            "New here? Create account"
    }

    private fun submit() {
        if (busy) return

        val e = email.text.toString().trim().lowercase()
        val p = password.text.toString()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            toast("Valid email address enter karein.")
            return
        }

        if (p.length < 8) {
            toast("Password kam az kam 8 characters ka hona chahiye.")
            return
        }

        if (registering && p != confirmPassword.text.toString()) {
            toast("Passwords match nahi karte.")
            return
        }

        setBusy(true)

        if (registering) {
            auth.createUserWithEmailAndPassword(e, p)
                .addOnCompleteListener(this) { task ->
                    setBusy(false)

                    if (task.isSuccessful) {
                        toast("Account successfully create ho gaya.")
                        finish()
                    } else {
                        toast(
                            task.exception?.localizedMessage
                                ?: "Registration fail ho gayi."
                        )
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(this) { task ->
                    setBusy(false)

                    if (task.isSuccessful) {
                        toast("Login successful.")
                        finish()
                    } else {
                        toast(
                            task.exception?.localizedMessage
                                ?: "Login fail ho gaya."
                        )
                    }
                }
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        action.isEnabled = !value
        action.alpha = if (value) 0.6f else 1f
        action.text = if (value) {
            "PLEASE WAIT..."
        } else if (registering) {
            "CREATE ACCOUNT"
        } else {
            "LOG IN"
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

    private fun label(
        value: String,
        size: Float,
        colour: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(colour)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
