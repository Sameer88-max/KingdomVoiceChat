
package com.kingdomvoicechat

import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

data class WalletBalance(
    val coins: Long = 0,
    val pearls: Long = 0
)

data class GiftItem(
    val id: String,
    val name: String,
    val price: Long,
    val bonusCoins: Long = 0,
    val imageUrl: String = "",
    val animationUrl: String = ""
)

object WalletApi {
    private const val BASE_URL =
        "https://kingdom-wallet-api.tzindahai15.workers.dev"

    private val executor = Executors.newSingleThreadExecutor()

    interface Callback<T> {
        fun onSuccess(value: T)
        fun onError(message: String)
    }

    fun getWallet(callback: Callback<WalletBalance>) {
        request("GET", "/wallet", null,
            object : Callback<JSONObject> {
                override fun onSuccess(value: JSONObject) {
                    callback.onSuccess(
                        WalletBalance(
                            value.optLong("coins", 0),
                            value.optLong("pearls", 0)
                        )
                    )
                }

                override fun onError(message: String) {
                    callback.onError(message)
                }
            })
    }

    fun getGifts(callback: Callback<List<GiftItem>>) {
        request("GET", "/gifts", null,
            object : Callback<JSONObject> {
                override fun onSuccess(value: JSONObject) {
                    val array = value.optJSONArray("gifts") ?: JSONArray()
                    val gifts = mutableListOf<GiftItem>()

                    for (i in 0 until array.length()) {
                        val item = array.optJSONObject(i) ?: continue
                        gifts.add(
                            GiftItem(
                                id = item.optString("id"),
                                name = item.optString("name"),
                                price = item.optLong("price"),
                                bonusCoins = item.optLong("bonusCoins"),
                                imageUrl = item.optString("imageUrl"),
                                animationUrl = item.optString("animationUrl")
                            )
                        )
                    }
                    callback.onSuccess(gifts)
                }

                override fun onError(message: String) {
                    callback.onError(message)
                }
            })
    }

    fun sendGift(
        receiverUid: String,
        giftId: String,
        roomId: String,
        callback: Callback<JSONObject>
    ) {
        if (receiverUid.isBlank() || giftId.isBlank() || roomId.isBlank()) {
            callback.onError("Receiver, gift and room are required")
            return
        }

        val body = JSONObject()
            .put("receiverUid", receiverUid)
            .put("giftId", giftId)
            .put("roomId", roomId)
            .put("requestId", UUID.randomUUID().toString())

        request("POST", "/gift", body, callback)
    }

    private fun request(
        method: String,
        path: String,
        body: JSONObject?,
        callback: Callback<JSONObject>
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            callback.onError("Please sign in first")
            return
        }

        user.getIdToken(true)
            .addOnSuccessListener { result ->
                val token = result.token
                if (token.isNullOrBlank()) {
                    callback.onError("Firebase token unavailable")
                    return@addOnSuccessListener
                }

                executor.execute {
                    var connection: HttpURLConnection? = null
                    try {
                        connection = (URL(BASE_URL + path).openConnection()
                            as HttpURLConnection).apply {
                            requestMethod = method
                            connectTimeout = 15000
                            readTimeout = 20000
                            setRequestProperty("Accept", "application/json")
                            setRequestProperty("Authorization", "Bearer $token")

                            if (body != null) {
                                doOutput = true
                                setRequestProperty(
                                    "Content-Type",
                                    "application/json; charset=utf-8"
                                )
                                outputStream.use {
                                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                                }
                            }
                        }

                        val status = connection.responseCode
                        val stream = if (status in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        }

                        val response = stream?.bufferedReader()?.use {
                            it.readText()
                        }.orEmpty()

                        val json = if (response.isBlank()) JSONObject()
                            else JSONObject(response)

                        if (status in 200..299 &&
                            json.optBoolean("success", true)
                        ) {
                            callback.onSuccess(json)
                        } else {
                            callback.onError(
                                json.optString("error", "Request failed: $status")
                            )
                        }
                    } catch (e: Exception) {
                        callback.onError(e.localizedMessage ?: "Network error")
                    } finally {
                        connection?.disconnect()
                    }
                }
            }
            .addOnFailureListener {
                callback.onError(it.localizedMessage ?: "Firebase token error")
            }
    }
}
