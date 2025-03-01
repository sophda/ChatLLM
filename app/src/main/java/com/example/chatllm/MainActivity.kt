package com.example.chatllm

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatllm.ui.theme.ChatLLMTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val messages = mutableStateListOf<Pair<Boolean, String>>() // 创建一个可变的消息列表

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "MainActivity onCreate called") // 调试信息
        setContent { 
            ChatLLMTheme { 
                Surface(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 左侧聊天主界面
                        ChatMessages(modifier = Modifier.weight(0.7f))
                        // 右侧在线状态/设置
                    }
                }
            }
        }
    }

    @Composable
    fun ChatMessages(modifier: Modifier) {
        // 创建一个 LazyListState 来控制滚动
        val listState = rememberLazyListState()
        
        // 当消息列表更新时，自动滚动到底部
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
        
        Column(modifier = modifier.fillMaxSize()) {
            // 消息展示区
            LazyColumn(
                modifier = Modifier.weight(0.8f),
                state = listState // 使用 listState 来控制滚动
            ) {
                items(messages) { message ->
                    MessageBubble(isUserMessage = message.first, message = message.second)
                }
            }
            // 输入控制区
            InputControl(modifier = Modifier.fillMaxWidth())
        }
    }

    @Composable
    fun InputControl(modifier: Modifier) {
        var text by remember { mutableStateOf("") } // 创建一个可变状态变量
        var botMessage by remember { mutableStateOf("") } // 机器人的消息
        var isSending by remember { mutableStateOf(false) } // 发送状态
        var userMessage by remember { mutableStateOf("") } // 保存用户的消息
        
        // 获取当前上下文和焦点管理器
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        
        Row(modifier = modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = text,
                onValueChange = { text = it }, // 更新状态变量
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") }
            )
            Button(onClick = { 
                if (text.isNotBlank()) { // 发送非空消息
                    userMessage = text // 保存用户的消息
                    messages.add(Pair(true, text)) // 添加用户消息到列表
                    botMessage = "" // 清空机器人的消息
                    isSending = true // 设置发送状态
                    messages.add(Pair(false, botMessage)) // 添加机器人的空消息气泡
                    text = "" // 清空输入框
                    
                    // 隐藏键盘
                    focusManager.clearFocus() // 清除焦点
                }
            }, modifier = Modifier.padding(start = 8.dp)) {
                Text("发送")
            }
        }
        // 启动协程逐字显示机器人的回复
        if (isSending) {
            LaunchedEffect(userMessage) {
                for (char in userMessage) {
                    botMessage += char
                    messages[messages.size - 1] = Pair(false, botMessage) // 更新机器人的消息气泡
                    delay(100) // 每个字符之间的延迟
                }
                isSending = false // 重置发送状态
            }
        }
    }

    @Composable
    fun MessageBubble(isUserMessage: Boolean, message: String) {
        val backgroundColor = if (isUserMessage) Color(0xFFdcf8c6) else Color.White
        val borderColor = if (isUserMessage) Color(0xFFd3f0b5) else Color(0xFFE0E0E0)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .wrapContentWidth() // 自适应宽度
                    .wrapContentHeight() // 自适应高度
                    .padding(8.dp) // 使气泡自动适应消息长度
            ) {
                Text(text = message, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
            }
        }
    }


}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ChatLLMTheme {
//        Greeting("Android")
//    }
//}