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
        CounterWithPresenter(modifier = Modifier.align(Alignment.Center))
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
    val onClickA: () -> Unit = remember { { channel.trySend(CounterAction.IncrementA) } }
    val onClickB: () -> Unit = remember { { channel.trySend(CounterAction.IncrementB) } }

    Row(modifier = modifier) {

        Button(a = state.a, onClick = onClickA)

        Text(
            text = "+",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Button(a = state.b, onClick = onClickB)

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

interface CounterActions {
    fun incrementA()

    fun incrementB()
}

class CounterViewModel(val state: CounterState, val actions: CounterActions)

@Composable
fun rememberCounterViewModel(a: Int, b: Int): CounterViewModel {
    var _a by remember(a) { mutableStateOf(a) }
    var _b by remember(b) { mutableStateOf(b) }
    val actions: CounterActions = remember {
        object : CounterActions {
            override fun incrementA() {
                _a++
            }

            override fun incrementB() {
                _b++
            }
        }
    }
    return CounterViewModel(CounterState(_a, _b), actions)
}