package com.example.vuonnha.ui.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.AssistantRepository
import com.example.vuonnha.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class AssistantState(val messages: List<ChatMessage> = emptyList(), val busy: Boolean = false, val error: String? = null)
class AssistantViewModel(app: Application): AndroidViewModel(app) {
    private val repo = AssistantRepository(app)
    private val mutable = MutableStateFlow(AssistantState())
    val state = mutable.asStateFlow()
    fun refresh() { if (!mutable.value.busy) run { copy(messages = repo.messages()) } }
    fun ask(question: String, onSuccess: () -> Unit) { if (!mutable.value.busy && question.trim().length in 1..1000) run { copy(messages = repo.ask(question)).also { onSuccess() } } }
    fun clear(forgetGarden: Boolean) { if (!mutable.value.busy) run { repo.clear(forgetGarden); copy(messages = emptyList()) } }
    private fun run(block: suspend AssistantState.() -> AssistantState) = viewModelScope.launch {
        mutable.value = mutable.value.copy(busy = true, error = null)
        try { mutable.value = mutable.value.block().copy(busy = false, error = null) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { mutable.value = mutable.value.copy(busy = false, error = e.message ?: "Chưa kết nối được Trợ lý.") }
    }
}
