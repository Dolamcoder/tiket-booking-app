package com.example.dacs3_ticket_booking_app.ui.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dacs3_ticket_booking_app.data.api.ChatAIRequest
import com.example.dacs3_ticket_booking_app.data.api.RetrofitClient
import com.example.dacs3_ticket_booking_app.data.model.ChatMessage
import com.example.dacs3_ticket_booking_app.databinding.ActivityChatBinding
import com.example.dacs3_ticket_booking_app.ui.view.adaper.ChatAdapter
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.btnBackChat.setOnClickListener { finish() }

        binding.btnSendChat.setOnClickListener {
            val text = binding.editChatMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
        
        // Tin nhắn chào mừng mặc định
        addMessage("Chào bạn! Tôi là MovieBuddy, tôi có thể giúp gì cho bạn?", false)
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewChat.adapter = adapter
    }

    private fun sendMessage(text: String) {
        // Thêm tin nhắn của user vào list
        addMessage(text, true)
        binding.editChatMessage.text.clear()

        // Gọi API
        binding.progressBarChat.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.chatService.getChatResponse(ChatAIRequest(text))
                binding.progressBarChat.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val reply = response.body()!!.reply
                    addMessage(reply, false)
                } else {
                    Toast.makeText(this@ChatActivity, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBarChat.visibility = View.GONE
                Toast.makeText(this@ChatActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewChat.scrollToPosition(messages.size - 1)
    }
}
