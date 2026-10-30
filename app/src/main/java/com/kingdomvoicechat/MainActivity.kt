private fun openRooms() {
    val options = arrayOf("Create New Room", "Join Existing Room")

    AlertDialog.Builder(this)
        .setTitle("KINGDOM VOICE ROOMS")
        .setItems(options) { _, which ->
            if (which == 0) {
                val roomID = "KVC_" +
                    System.currentTimeMillis().toString().takeLast(8)

                AlertDialog.Builder(this)
                    .setTitle("Your Room ID")
                    .setMessage(
                        "Room ID: $roomID\nShare this ID with others."
                    )
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("CREATE ROOM") { _, _ ->
                        RoomManager.createRoom(this, roomID)
                    }
                    .show()

            } else {
                val input = android.widget.EditText(this).apply {
                    hint = "Enter Room ID"
                    setSingleLine(true)
                }

                AlertDialog.Builder(this)
                    .setTitle("Join Existing Room")
                    .setView(input)
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("JOIN") { _, _ ->
                        RoomManager.joinRoom(
                            this,
                            input.text.toString()
                        )
                    }
                    .show()
            }
        }
        .setNegativeButton("CANCEL", null)
        .show()
}
