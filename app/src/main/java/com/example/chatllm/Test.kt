package com.example.chatllm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview

class Test : ComponentActivity() {
    private val message = mutableStateListOf<String>();
    var str = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                text()
            }
        }
    }
    @Composable
    fun text() {
        Column {
            Text(text = "hello, $str")

            Button(onClick = {
                message.add("123");
            }) {

            }
        }
        if(message.contains("123")) {
            str = "kotlin"
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun prev() {
        text()
    }

}