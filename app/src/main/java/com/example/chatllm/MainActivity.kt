package com.example.chatllm

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.chatllm.ui.theme.ChatLLMTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 定义消息类型
sealed class MessageContent {
    data class TextMessage(val text: String) : MessageContent()
    data class ImageMessage(val imageUri: Uri) : MessageContent()
    data class TextWithImageMessage(val text: String, val imageUri: Uri) : MessageContent() // 新增：文字和图片的组合消息
}

// 用户信息数据类
//data class UserInfo(
//    val name: String,
//    val email: String,
//    val avatarResId: Int? = null,
//    val avatarUri: Uri? = null
//)

class MainActivity : ComponentActivity() {
    // 修改消息数据结构，支持不同类型的消息
    private val messages = mutableStateListOf<Pair<Boolean, MessageContent>>() // 创建一个可变的消息列表
    private var isTyping by mutableStateOf(false) // 机器人是否正在打字
    
    // 模拟用户信息
    private val userInfo = UserInfo(
        name = "张三",
        email = "zhangsan@example.com",
        avatarResId = R.drawable.ic_launcher_foreground
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "MainActivity onCreate called") // 调试信息
        setContent { 
            ChatLLMTheme { 
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(userInfo)
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(userInfo: UserInfo) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        
        // 侧边栏
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SidebarContent(userInfo)
            }
        ) {
            // 主界面
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("ChatLLM") },
                        navigationIcon = {
                            IconButton(onClick = {
                                // 打开侧边栏
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "菜单")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    // 聊天主界面
                    ChatMessages(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
    
    @Composable
    fun UserInfoPanel(userInfo: UserInfo) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户头像
            if (userInfo.avatarResId != null) {
                Image(
                    painter = painterResource(id = userInfo.avatarResId),
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else if (userInfo.avatarUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userInfo.avatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 默认头像
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInfo.name.first().toString(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 用户名
            Text(
                text = userInfo.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 用户邮箱
            Text(
                text = userInfo.email,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
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
                    MessageBubble(isUserMessage = message.first, content = message.second)
                }
                
                // 如果机器人正在打字，显示加载动画
                if (isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            // 输入控制区
            InputControl(modifier = Modifier.fillMaxWidth())
        }
    }
    
    @Composable
    fun TypingIndicator() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        LoadingDot(delay = index * 300)
                    }
                }
            }
        }
    }
    
    @Composable
    fun LoadingDot(delay: Int) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, delayMillis = delay),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(scale)
                .background(Color.Gray, CircleShape)
        )
    }

    @Composable
    fun InputControl(modifier: Modifier) {
        var text by remember { mutableStateOf("") } // 创建一个可变状态变量
        var botMessage by remember { mutableStateOf("") } // 机器人的消息
        var isSending by remember { mutableStateOf(false) } // 发送状态
        var userMessage by remember { mutableStateOf("") } // 保存用户的消息
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) } // 保存用户选择的图片URI
        
        // 获取当前上下文和焦点管理器
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        
        // 图片选择器
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                // 保存选择的图片URI，但不立即发送
                selectedImageUri = it
            }
        }
        
        Row(modifier = modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // 图片选择按钮
            Box {
                IconButton(onClick = {
                    imagePickerLauncher.launch("image/*")
                }) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "选择图片",
                        tint = if (selectedImageUri != null) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                // 如果已选择图片，显示一个标记
                if (selectedImageUri != null) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("1")
                    }
                }
            }
            
            // 文本输入框
            TextField(
                value = text,
                onValueChange = { text = it }, // 更新状态变量
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") }
            )
            
            // 发送按钮
            Button(onClick = { 
                if (text.isNotBlank() || selectedImageUri != null) { // 发送非空消息或有图片
                    userMessage = text // 保存用户的消息
                    
                    // 根据是否有图片选择不同的发送方式
                    if (selectedImageUri != null) {
                        if (text.isNotBlank()) {
                            // 发送文字和图片的组合消息
                            messages.add(Pair(true, MessageContent.TextWithImageMessage(text, selectedImageUri!!)))
                            // 设置机器人正在打字
                            botMessage = ""
                            isSending = true
                            isTyping = true
                            messages.add(Pair(false, MessageContent.TextWithImageMessage("", selectedImageUri!!)))
                        } else {
                            // 只发送图片
                            messages.add(Pair(true, MessageContent.ImageMessage(selectedImageUri!!)))
                            // 机器人回复相同的图片
                            messages.add(Pair(false, MessageContent.ImageMessage(selectedImageUri!!)))
                        }
                        // 清空选择的图片
                        selectedImageUri = null
                    } else {
                        // 只发送文字
                        messages.add(Pair(true, MessageContent.TextMessage(text)))
                        // 设置机器人正在打字
                        botMessage = ""
                        isSending = true
                        isTyping = true
                        messages.add(Pair(false, MessageContent.TextMessage(botMessage)))
                    }
                    
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
                // 使用 NativeInterface 获取回复
                val response = try {
                    // 尝试使用真实的C++接口
                    NativeInterface.sendMessageToCpp(userMessage)
                } catch (e: UnsatisfiedLinkError) {
                    // 如果本地库未加载，使用模拟回复
                    NativeInterface.getMockResponse(userMessage)
                }
                
                // 逐字显示回复
                for (char in response) {
                    botMessage += char
                    // 更新机器人的消息气泡
                    val lastMessage = messages[messages.size - 1]
                    if (lastMessage.second is MessageContent.TextMessage) {
                        messages[messages.size - 1] = Pair(false, MessageContent.TextMessage(botMessage))
                    } else if (lastMessage.second is MessageContent.TextWithImageMessage) {
                        val imageUri = (lastMessage.second as MessageContent.TextWithImageMessage).imageUri
                        messages[messages.size - 1] = Pair(false, MessageContent.TextWithImageMessage(botMessage, imageUri))
                    }
                    delay(50) // 每个字符之间的延迟
                }
                isSending = false // 重置发送状态
                isTyping = false // 机器人打字结束
            }
        }
    }

    @Composable
    fun MessageBubble(isUserMessage: Boolean, content: MessageContent) {
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
                when (content) {
                    is MessageContent.TextMessage -> {
                        Text(text = content.text, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
                    }
                    is MessageContent.ImageMessage -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(content.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "图片消息",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    is MessageContent.TextWithImageMessage -> {
                        Column {
                            if (content.text.isNotBlank()) {
                                Text(text = content.text, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(content.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "图片消息",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
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