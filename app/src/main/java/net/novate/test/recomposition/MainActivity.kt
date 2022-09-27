package net.novate.test.recomposition

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.novate.test.recomposition.demo3.CounterDemo3

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CounterApp()
        }
    }
}

@Composable
fun CounterApp() {
    Box(modifier = Modifier.fillMaxSize()) {
        CounterDemo3(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun CounterContent(
    a: Int,
    b: Int,
    sum: Int,
    onClickA: () -> Unit,
    onClickB: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalView.current.context as? Activity
    Row(modifier = modifier) {

        Button(a = a, onClick = onClickA)

        Text(
            text = "+",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Button(a = b, onClick = onClickB)

        Text(
            text = "=",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Button(a = sum) {
            // 测试跳转到新页面后，入栈的 Activity 上的 Compose 组件是否还会响应 state 变化
            // 测试结果是不会响应 state 变化，不必担心修改了 state 导致无谓的重组
            activity?.run {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }
}

@Stable
@Composable
fun Button(a: Int, onClick: () -> Unit) {
    Text(
        text = a.toString(),
        fontSize = 24.sp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}