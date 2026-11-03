package com.kingdomvoicechat

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class CreateRoomDialog(
    private val context: Context,
    private val onCreate: (String, String) -> Unit
) {
    fun show() {
        val nameInput = EditText(context).apply {
            hint = "Enter Room Name"
        }

        val idInput = EditText(context).apply {
            hint = "Enter Room ID"
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 10, 40, 0)
            addView(nameInput)
            addView(idInput)
        }

        AlertDialog.Builder(context)
            .setTitle("Create Voice Room")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text.toString().trim()
                val id = idInput.text.toString().trim()

                if (name.isBlank() || id.isBlank()) {
                    Toast.makeText(
                        context,
                        "Room name aur ID zaroori hain",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onCreate(name, id)
                }
            }
            .show()
    }
}
