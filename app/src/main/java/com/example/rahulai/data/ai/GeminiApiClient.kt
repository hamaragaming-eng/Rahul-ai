package com.example.rahulai.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionDetail: String? = null
)

enum class MessageSender {
    USER,
    RAHUL_AI
}

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateAiResponse(
        userPrompt: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineIntelligentResponse(userPrompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put(
                            "text",
                            "You are Rahul AI (राहुल एआई), a high-tech smart voice assistant and smart home system controller. " +
                                    "You natively understand Hindi, Hinglish, and English. " +
                                    "Your tone is polite, modern, confident, helpful, and concise (since responses are read aloud via voice). " +
                                    "Keep spoken answers under 2-3 short sentences unless the user explicitly asks for detailed explanations. " +
                                    "You can manage devices (lights, AC, locks, TV, fan, cameras) and device features (flashlight, battery, volume). " +
                                    "Always refer to yourself as Rahul AI when asked."
                        )
                    })
                })
            }

            val contentsArray = JSONArray()

            // Include last 4 turns for context
            val recentHistory = history.takeLast(4)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
            }

            // Current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userPrompt) })
                })
            })

            val payload = JSONObject().apply {
                put("systemInstruction", systemInstructionJson)
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.9)
                    put("maxOutputTokens", 250)
                })
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                return@withContext "AI response issue (${response.code}). Defaulting: ${getOfflineIntelligentResponse(userPrompt)}"
            }

            val responseBody = response.body?.string() ?: return@withContext getOfflineIntelligentResponse(userPrompt)
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                getOfflineIntelligentResponse(userPrompt)
            }
        } catch (e: Exception) {
            getOfflineIntelligentResponse(userPrompt)
        }
    }

    private fun getOfflineIntelligentResponse(prompt: String): String {
        val lower = prompt.lowercase().trim()
        return when {
            lower.contains("kaise ho") || lower.contains("how are you") ->
                "Main bilkul theek hoon! Rahul AI system control engine poori tarah se active hai. Aap batayein main aapki kya madad kar sakta hoon?"
            lower.contains("kya kar sakte ho") || lower.contains("what can you do") || lower.contains("features") ->
                "Main aapke smart home devices (Lights, AC, Door Locks, TV, Fans) ko voice command se control kar sakta hoon, phone ki Flashlight, Battery aur Volume manage kar sakta hoon, aur kisi bhi vishay par aapse baat kar sakta hoon!"
            lower.contains("joke") || lower.contains("chutkula") ->
                "Ek baar ek light bulb ne smart switch se bola: 'Jabse Rahul AI aaya hai, humara connection aur bhi bright ho gaya hai!'"
            lower.contains("namaste") || lower.contains("hello") || lower.contains("hi") ->
                "Namaste! Main Rahul AI hoon. Aapka personal smart assistant. Aap voice ya text se koi bhi aadesh de sakte hain."
            lower.contains("weather") || lower.contains("mausam") ->
                "Smart sensor ke mutabik ghar ka taapmaan anukool hai aur mausam suhavna bana hua hai."
            lower.contains("shukriya") || lower.contains("thank") || lower.contains("dhanyawad") ->
                "Aapka swagat hai! Main hamesha aapki seva ke liye tatpar hoon."
            else ->
                "Ji, maine aapki baat samajh li hai. Rahul AI engine aapke nirdesh ko process kar raha hai."
        }
    }
}
