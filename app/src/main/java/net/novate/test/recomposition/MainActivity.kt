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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

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
//        CounterWithButton(modifier = Modifier.align(Alignment.Center))
        CounterWithPresenter(modifier = Modifier.align(Alignment.Center))
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

        Button(a = b) { b++ }

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

@Composable
fun CounterWithPresenter(modifier: Modifier = Modifier) {

    val channel = remember { Channel<CounterAction>() }
    val flow = remember(channel) { channel.consumeAsFlow() }

    // counterPresenter 方式把 a 和 b 组合起来，而 a 或 b 任一变化都会导致 state 的变化，进而扩大重组范围，导致性能降低
    val state = counterPresenter(flow)

    Row(modifier = modifier) {

        Button(a = state.a) { channel.trySend(CounterAction.IncrementA) }

        Text(
            text = "+",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Button(a = state.b) { channel.trySend(CounterAction.IncrementB) }

        Text(
            text = "=",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = state.sum.toString(),
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

sealed interface CounterAction {

    object IncrementA : CounterAction

    object IncrementB : CounterAction
}

@Immutable
data class CounterState(val a: Int, val b: Int) {
    val sum get() = a + b
}

@Composable
fun counterPresenter(
    actions: Flow<CounterAction>
): CounterState {

    var a by remember { mutableStateOf(0) }
    var b by remember { mutableStateOf(0) }

    LaunchedEffect(actions) {
        actions.collect { action ->
            when (action) {
                is CounterAction.IncrementA -> a++
                is CounterAction.IncrementB -> b++
            }
        }
    }

    return CounterState(a, b)
}