
package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserSearchActivity : Activity() {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 181, 215)
    private val cyan = Color.rgb(69, 226, 235)
    private val purple = Color.rgb(150, 102, 255)

    private lateinit var searchInput: EditText
    private lateinit var results: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(18))
            setBackgroundColor(bg)
        }

        root.addView(label("FIND PEOPLE", 25f, white, true))
        root.addView(
            label("Search users by their KingdomVoiceChat ID", 13f, muted)
        )

        searchInput = EditText(this).apply {
            hint = "Enter User ID"
            textSize = 15f
            setTextColor(white)
            setHintTextColor(muted)
            setSingleLine(true)
            setPadding(dp(14), 0, dp(14), 0)
            background = rounded(card, 12, Color.rgb(60, 68, 110))
        }

        root.addView(
            searchInput,
            LinearLayout.LayoutParams(-1, dp(54)).apply {
                topMargin = dp(20)
            }
        )

        root.addView(
            button("SEARCH USER", cyan, bg) {
                searchUser(searchInput.text.toString().trim())
            },
            LinearLayout.LayoutParams(-1, dp(50)).apply {
                topMargin = dp(10)
            }
        )

        val scroll = ScrollView(this)
        results = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, dp(15))
        }
        scroll.addView(results)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f).apply {
                topMargin = dp(8)
            }
        )

        setContentView(root)
    }

    private fun searchUser(query: String) {
        if (query.isBlank()) {
            toast("Please enter a User ID")
            return
        }

        val myUid = auth.currentUser?.uid
        if (myUid == null) {
            toast("Please sign in first")
            return
        }

        results.removeAllViews()
        results.addView(label("Searching...", muted))

        // First check whether the entered value is a Firebase UID.
        db.collection("publicProfiles")
            .document(query)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    showUserResult(document.id, document.data.orEmpty())
                } else {
                    searchNumericId(query)
                }
            }
            .addOnFailureListener {
                searchNumericId(query)
            }
    }

    private fun searchNumericId(number: String) {
        db.collection("publicProfiles")
            .whereEqualTo("numericId", number)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val document = snapshot.documents.firstOrNull()

                if (document != null) {
                    showUserResult(document.id, document.data.orEmpty())
                    return@addOnSuccessListener
                }

                val numericValue = number.toLongOrNull()
                if (numericValue == null) {
                    showNotFound()
                    return@addOnSuccessListener
                }

                db.collection("publicProfiles")
                    .whereEqualTo("numericId", numericValue)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { numericSnapshot ->
                        val found = numericSnapshot.documents.firstOrNull()
                        if (found == null) {
                            showNotFound()
                        } else {
                            showUserResult(found.id, found.data.orEmpty())
                        }
                    }
                    .addOnFailureListener {
                        showError(it.localizedMessage)
                    }
            }
            .addOnFailureListener {
                showError(it.localizedMessage)
            }
    }

    private fun showUserResult(
        uid: String,
        data: Map<String, Any>
    ) {
        if (uid == auth.currentUser?.uid) {
            results.removeAllViews()
            results.addView(label("This is your own profile.", muted))
            return
        }

        results.removeAllViews()

        val name = data["name"]?.toString()
            ?.takeIf { it.isNotBlank() } ?: "Kingdom User"

        val publicId = data["numericId"]?.toString() ?: uid

        val profileCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(17), dp(17), dp(17))
            background = rounded(card, 18, Color.rgb(55, 62, 105))
            isClickable = true
            isFocusable = true
        }

        profileCard.addView(label("👤  $name", 19f, white, true))
        profileCard.addView(
            label("ID: $publicId", 13f, muted).apply {
                setPadding(0, dp(7), 0, dp(5))
            }
        )
        profileCard.addView(
            label("VIEW PROFILE  ›", 12f, cyan, true)
        )

        profileCard.setOnClickListener {
            openProfile(uid, name, publicId, data)
        }

        results.addView(
            profileCard,
            LinearLayout.LayoutParams(-1, -2)
        )
    }

    private fun openProfile(
        uid: String,
        name: String,
        publicId: String,
        publicData: Map<String, Any>
    ) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val country = document.getString("country")
                    ?: publicData["country"]?.toString().orEmpty()

                val age = document.getLong("age")?.toString().orEmpty()
                val gender = document.getString("gender").orEmpty()

                val profileDetails = buildString {
                    append("Name: $name\n")
                    append("User ID: $publicId\n")
                    if (country.isNotBlank()) append("Country: $country\n")
                    if (age.isNotBlank()) append("Age: $age\n")
                    if (gender.isNotBlank()) append("Gender: $gender\n")
                }

                checkFollowStatus(uid, name, profileDetails)
            }
            .addOnFailureListener {
                toast("Profile could not be loaded: ${it.localizedMessage}")
            }
    }

    private fun checkFollowStatus(
        targetUid: String,
        name: String,
        details: String
    ) {
        val myUid = auth.currentUser?.uid ?: return

        val followRef = db.collection("follows")
            .document(myUid)
            .collection("following")
            .document(targetUid)

        followRef.get()
            .addOnSuccessListener { document ->
                val following = document.exists()

                AlertDialog.Builder(this)
                    .setTitle(name)
                    .setMessage(details)
                    .setPositiveButton(
                        if (following) "UNFOLLOW" else "FOLLOW"
                    ) { _, _ ->
                        if (following) {
                            followRef.delete()
                                .addOnSuccessListener {
                                    toast("You unfollowed $name")
                                }
                                .addOnFailureListener {
                                    toast("Unfollow failed: ${it.localizedMessage}")
                                }
                        } else {
                            followRef.set(
                                mapOf(
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                            ).addOnSuccessListener {
                                toast("You are now following $name")
                            }.addOnFailureListener {
                                toast("Follow failed: ${it.localizedMessage}")
                            }
                        }
                    }
                    .setNegativeButton("CLOSE", null)
                    .show()
            }
            .addOnFailureListener {
                toast("Could not check follow status")
            }
    }

    private fun showNotFound() {
        results.removeAllViews()
        results.addView(label("No user found with this ID.", 14f, muted))
    }

    private fun showError(message: String?) {
        results.removeAllViews()
        results.addView(
            label("Search failed: ${message ?: "Unknown error"}", 14f, muted)
        )
    }

    private fun label(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun button(
        value: String,
        color: Int,
        textColor: Int,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = 13f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            background = rounded(color, 12)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun rounded(
        color: Int,
        radius: Int,
        stroke: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
