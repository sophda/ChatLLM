package com.example.chatllm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatllm.ui.theme.ChatLLMTheme

class ChatScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ChatScreen", "ChatScreen onCreate called") // 调试信息
        setContent { 
            ChatLLMTheme { 
                Surface(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 左侧聊天主界面
                        ChatMessages(modifier = Modifier.weight(0.7f))
                        // 右侧在线状态/设置
                        SettingsPanel(modifier = Modifier.weight(0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessages(modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // 消息展示区
        LazyColumn(modifier = Modifier.weight(0.8f)) {
            items(20) { index ->
                MessageBubble(isUserMessage = index % 2 == 0, message = "消息 $index")
            }
        }
        // 输入控制区
        InputControl(modifier = Modifier.fillMaxWidth())
        Text(text = "聊天消息区", modifier = Modifier.padding(8.dp)) // 调试文本
    }
}

@Composable
fun MessageBubble(isUserMessage: Boolean, message: String) {
    val backgroundColor = if (isUserMessage) Color(0xFFdcf8c6) else Color.White
    val borderColor = if (isUserMessage) Color(0xFFd3f0b5) else Color(0xFFE0E0E0)
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start

    Box(modifier = Modifier
        .padding(8.dp)
        .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
        .border(1.dp, borderColor, RoundedCornerShape(12.dp))) {
        Text(text = message, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
    }
}

@Composable
fun InputControl(modifier: Modifier) {
    Row(modifier = modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(value = "", onValueChange = {}, modifier = Modifier.weight(1f), placeholder = { Text("输入消息...") })
        Button(onClick = { /* 发送消息 */ }, modifier = Modifier.padding(start = 8.dp)) {
            Text("发送")
        }
    }
}

@Composable
fun SettingsPanel(modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "在线状态", fontSize = 20.sp)
        Text(text = "设置面板", modifier = Modifier.padding(8.dp)) // 调试文本
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    ChatLLMTheme {
        ChatMessages(Modifier.fillMaxSize())
    }
} 