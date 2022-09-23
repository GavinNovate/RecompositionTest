package net.novate.test.recomposition

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
//        CounterWithText(modifier = Modifier.align(Alignment.Center))
        CounterWithButton(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun CounterWithText(
    modifier: Modifier = Modifier,
) {
    var a by rememberSaveable { mutableStateOf(0) }
    var b by rememberSaveable { mutableStateOf(0) }

    Row(modifier = modifier) {

        // TextA: 点击这个组件，改变了 a 的状态，也会导致下面 TextB 组件发生重组，初步判断是由 clickable{ } 导致的
        Text(
            text = a.toString(),
            fontSize = 24.sp,
            modifier = Modifier
                .clickable(onClick = { a++ })
                .padding(8.dp)
        )

        Text(
            text = "+",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        // TextB
        // 这里的 modifier 会生成新的实例，导致 Text 发生重组
        Text(
            text = b.toString(),
            fontSize = 24.sp,
            modifier = Modifier
                .clickable(onClick = { b++ })
                .padding(8.dp)
        )

        Text(
            text = "=",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = (a + b).toString(),
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun CounterWithButton(
    modifier: Modifier = Modifier,
) {
    var a by rememberSaveable { mutableStateOf(0) }
    var b by rememberSaveable { mutableStateOf(0) }

    Row(modifier = modifier) {

        // 把 Text 包装为 Button 单独改变 A 就不会导致 B 重组
        Button(a = a) { a++ }

        Text(
            text = "+",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        // 这里的 onClickB 不会产生新的实例，应该是 Compose 编译器插件自动插入 remember 了（BennyHuo说的）
        // TODO 2022/9/23: 翻一下字节码看看生成的代码是什么样的
        val onClickB: () -> Unit = { b++ }

        // 写成这样就会生成新的实例
        /*val onClickB: () -> Unit = object : () -> Unit {
            override fun invoke() {
                b++
            }
        }*/

        Button(a = b, onClickB)

        Text(
            text = "=",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = (a + b).toString(),
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

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