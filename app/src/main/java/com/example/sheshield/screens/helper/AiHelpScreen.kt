package com.example.sheshield.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheshield.services.SafetyAiService // Import your new service
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data model for chat messages
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiHelpScreen() {
    // --- Colors ---
    val purplePrimary = Color(0xFF9333EA)
    val bgGray = Color(0xFFF9FAFB)
    val tipBg = Color(0xFFFFFBEB)
    val tipIconColor = Color(0xFFF59E0B)
    val userBubbleColor = Color(0xFFE9D5FF)
    val botBubbleColor = Color.White
    val chipBg = Color(0xFFF3E8FF)
    val chipText = Color(0xFF7E22CE)

    // --- State ---
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) } // Loading state

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Initialize the Real AI Service
    val aiService = remember { SafetyAiService() }

    // Initial greeting
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    text = "Hi! I'm your AI Safety Companion. I'm connected and ready to help with safety advice or emergency guidance.",
                    isUser = false
                )
            )
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // --- Send Message Function ---
    fun sendMessage(text: String) {
        if (text.isBlank() || isTyping) return

        // 1. Add User Message
        messages.add(ChatMessage(text = text, isUser = true))
        inputText = ""
        isTyping = true

        // 2. Call Gemini API
        coroutineScope.launch {
            val responseText = aiService.sendMessage(text)
            messages.add(ChatMessage(text = responseText, isUser = false))
            isTyping = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGray)
    ) {
        // --- 1. HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(purplePrimary)
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Face, "AI", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "AI Safety Companion",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Powered by Gemini",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- 2. TIP BANNER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tipBg)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = tipIconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Tip: Ask specific questions like 'What do I do if I think I'm being followed?'",
                color = Color.DarkGray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        // --- 3. CHAT AREA ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg, userBubbleColor, botBubbleColor)
            }
            if (isTyping) {
                item {
                    Text(
                        "AI is typing...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        // --- 4. QUICK QUESTIONS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Quick questions:",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickChip("I feel unsafe", chipBg, chipText) { sendMessage("I feel unsafe right now, what should I do?") }
                QuickChip("Walking alone tips", chipBg, chipText) { sendMessage("Give me 3 safety tips for walking alone at night") }
                QuickChip("Emergency Check", chipBg, chipText) { sendMessage("What is the first thing to do in an emergency?") }
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(top = 8.dp))

            // --- 5. INPUT AREA ---
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(25.dp))
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text("Type your message...", color = Color.Gray)
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                        cursorBrush = SolidColor(purplePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendMessage(inputText) },
                    enabled = !isTyping,
                    modifier = Modifier
                        .size(50.dp)
                        .background(if (isTyping) Color.Gray else purplePrimary, CircleShape)
                ) {
                    if (isTyping) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, userColor: Color, botColor: Color) {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val time = sdf.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(if(message.isUser) 1f else 0.9f),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFE9D5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = "Bot",
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if(message.isUser) Alignment.End else Alignment.Start) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (message.isUser) 16.dp else 4.dp,
                                bottomEnd = if (message.isUser) 4.dp else 16.dp
                            )
                        )
                        .background(if (message.isUser) userColor else botColor)
                        .border(
                            1.dp,
                            if(message.isUser) Color.Transparent else Color.LightGray.copy(alpha=0.3f),
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (message.isUser) 16.dp else 4.dp,
                                bottomEnd = if (message.isUser) 4.dp else 16.dp
                            )
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.Black,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuickChip(text: String, bgColor: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}