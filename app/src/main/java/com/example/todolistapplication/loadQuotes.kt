package com.example.todolistapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


suspend fun loadQuote(): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.breakingbadquotes.xyz/v1/quotes")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val inputStream = connection.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(inputStream)
            val quote = jsonArray.getJSONObject(0).getString("quote")

            connection.disconnect()
            quote
        } catch (e: Exception) {
            e.printStackTrace()
            "未知错误: ${e.message}"
           // "未知错误"
        }
    }
}

