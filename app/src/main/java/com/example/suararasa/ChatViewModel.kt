package com.example.suararasa

import android.R.attr.content
import android.R.id.content
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suararasa.BuildConfig.apiKey
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content // Import Content
import com.google.ai.client.generativeai.type.content // Untuk content builder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.plus
import kotlin.collections.toTypedArray
import kotlin.let
import kotlin.run
import kotlin.text.isNotBlank

// Data class untuk mesej
data class ChatMessage(
    val text: String,
    val isUser: Boolean // True jika mesej daripada pengguna, false jika daripada bot
)

// UiState yang dikemas kini untuk skrin sembang
sealed interface ChatScreenUiState {
    data object Initial : ChatScreenUiState
    data object Loading : ChatScreenUiState // Mewakili keadaan memuatkan selepas pengguna menghantar mesej
    data class Success(val messages: List<ChatMessage>) : ChatScreenUiState
    data class Error(val errorMessage: String) : ChatScreenUiState
}

class ChatViewModel : ViewModel() {
//    private val _uiState: MutableStateFlow<ChatScreenUiState> =
//        MutableStateFlow(ChatScreenUiState.Initial)
//    val uiState: StateFlow<ChatScreenUiState> =
//        _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<ChatScreenUiState>
    val uiState: StateFlow<ChatScreenUiState>

    private var generativeModel: GenerativeModel
    private val chatHistory = mutableListOf<Content>() // Untuk menyimpan sejarah perbualan


    init {
        val systemBehavior = ChatBotConfig.DEFAULT_SYSTEM_BEHAVIOR
        // Atau hardcode terus di sini jika anda tidak mahu fail berasingan:
        // val systemBehavior = "You are a witty pirate chatbot."

        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            systemInstruction = if (systemBehavior.isNotBlank()) {
                content { text(systemBehavior) }
            } else {
                null // Tiada arahan sistem jika kosong
            }
        )
        val initialBotMessageText = "Hello! my name is Ai-man,your AI assistant. How are you feeling today?"
        val initialBotMessage = ChatMessage(initialBotMessageText, isUser = false)
        // Tambah ke sejarah model jika anda mahu model tahu ia telah mengatakan ini
        // Ini membantu model mengekalkan konteks bahawa ia telah memperkenalkan dirinya.
        chatHistory.add(content("model") { text(initialBotMessageText) })

        // Inisialisasikan _uiState dengan mesej permulaan
        // dan kemudian uiState sebagai StateFlow daripadanya.
        _uiState = MutableStateFlow(ChatScreenUiState.Success(listOf(initialBotMessage)))
        uiState = _uiState.asStateFlow()
    }

    fun sendMessage(userPrompt: String) {
            val currentUserMessages =
                (_uiState.value as? ChatScreenUiState.Success)?.messages ?: emptyList()
            val userMessage = ChatMessage(userPrompt, isUser = true)
            _uiState.value = ChatScreenUiState.Success(currentUserMessages + userMessage)

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    chatHistory.add(content("user") { text(userPrompt) })

                    val response = generativeModel.generateContent(*chatHistory.toTypedArray())

                    response.text?.let { outputContent ->
                        chatHistory.add(content("model") { text(outputContent) })
                        val updatedMessages =
                            (_uiState.value as? ChatScreenUiState.Success)?.messages ?: listOf(
                                userMessage
                            )
                        val botMessage = ChatMessage(outputContent, isUser = false)
                        _uiState.value = ChatScreenUiState.Success(updatedMessages + botMessage)
                    } ?: run {
                        // Jika respons kosong
                        val messagesOnError = (_uiState.value as? ChatScreenUiState.Success)?.messages ?: listOf(
                            userMessage
                        )
                        // Anda boleh memilih untuk memaparkan ralat sebagai mesej bot atau keadaan Error yang berasingan
                        // _uiState.value = ChatScreenUiState.Success(messagesOnError + ChatMessage("Error: Received an empty response.", false))
                        _uiState.value = ChatScreenUiState.Error("Received an empty response from the AI.")
                    }
                } catch (e: Exception) {
                    // Jika berlaku pengecualian
                    val messagesOnError = (_uiState.value as? ChatScreenUiState.Success)?.messages ?: listOf(
                        userMessage
                    )
                    // _uiState.value = ChatScreenUiState.Success(messagesOnError + ChatMessage("Error: ${e.localizedMessage}", false))
                    _uiState.value = ChatScreenUiState.Error(e.localizedMessage ?: "An unknown error occurred")
                }
            }
    }

        // Anda mungkin mahu fungsi untuk mengosongkan sembang jika perlu
        fun clearChat() {
            chatHistory.clear()
            // Apabila sembang dibersihkan, tambahkan semula mesej permulaan
            val initialBotMessageText = "Hi, my name is Aiman, how are you feeling today?"
            val initialBotMessage = ChatMessage(initialBotMessageText, isUser = false)
            // Tambah semula mesej permulaan ke sejarah jika ia adalah sebahagian daripada konteks model
            chatHistory.add(content("model") { text(initialBotMessageText) })
            _uiState.value = ChatScreenUiState.Success(listOf(initialBotMessage))
        }
}