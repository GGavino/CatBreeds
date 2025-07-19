package com.example.catbreeds.util

import android.content.Context
import java.io.File
import java.util.Properties

object LocalPropertiesReader {

    fun getApiKey(context: Context): String {
        return try {
            val properties = Properties()
            val localPropertiesFile = File(context.filesDir.parentFile?.parentFile?.parentFile, "local.properties")

            if (localPropertiesFile.exists()) {
                properties.load(localPropertiesFile.inputStream())
                properties.getProperty("CAT_API_KEY") ?: ""
            } else {
                // Fallback: try reading from assets if local.properties is copied there
                val inputStream = context.assets.open("local.properties")
                properties.load(inputStream)
                properties.getProperty("CAT_API_KEY") ?: ""
            }
        } catch (e: Exception) {
            // If reading fails, return empty string or handle as needed
            ""
        }
    }
}
