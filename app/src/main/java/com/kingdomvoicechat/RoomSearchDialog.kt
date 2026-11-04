package com.kingdomvoicechat

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

class RoomSearchDialog(
    private val context: Context,
    private val onSearch: (String) -> Unit
) {
    fun show() {
        val input = EditText(context).apply {
            hint = "Enter User ID or Room ID"
            setSingleLine(true)
        }

        AlertDialog.Builder(context)
            .setTitle("Search")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Search") { _, _ ->
                val query = input.text.toString().trim()
                if (query.isNotBlank()) {
                    onSearch(query)
                }
            }
            .show()
    }
}
