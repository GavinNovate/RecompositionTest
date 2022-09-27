package net.novate.test.recomposition.demo1

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import net.novate.test.recomposition.CounterContent

/**
 * 方案1：核心是 [rememberCounterState]，方法入参传入初始值，和 Flow<CounterAction>，flow 用来执行操作；返回值是 CounterState
 *
 * 优点：
 * 1. [rememberCounterState] 中的 [LaunchedEffect] 有协程作用域，可以使用协程
 * 2. [LaunchedEffect] 明显地声明了作用域下的代码为副作用；代码结构比较清晰
 *
 * 缺点：
 * 1. [CounterDemo1] 中手动创建 channel 和 flow 有点麻烦
 * 2. [CounterDemo1] 中使用 channel 时，lambda 代码会在每次重组时候重新创建实例，导致重组范围扩大，需要使用 remember 记住（很奇怪，有时候 Compose 会自动记住，但是这种情况下没有自动记住，应该和 Compose 编译器自动推断有关）
 */
private const val TAG = "CounterDemo1"

@Composable
fun CounterDemo1(modifier: Modifier = Modifier) {
    val channel = remember { Channel<CounterAction>() }
    val flow = remember { channel.consumeAsFlow() }
    val state = rememberCounterState(a = 0, b = 0, actions = flow)

    Log.d(TAG, "CounterDemo1: ${state.a}")
    CounterContent(
        a = state.a,
        b = state.b,
        sum = state.sum,
        // 需要 remember 住函数调用
        onClickA = remember { { channel.trySend(CounterAction.IncrementA) } },
        onClickB = remember { { channel.trySend(CounterAction.IncrementB) } },
        modifier = modifier
    )
}

@Composable
private fun rememberCounterState(
    a: Int,
    b: Int,
    actions: Flow<CounterAction>
): CounterState {
    var aState by rememberSaveable(a) { mutableStateOf(a) }
    var bState by rememberSaveable(b) { mutableStateOf(b) }
    LaunchedEffect(actions) {
        actions.collect {
            when (it) {
                CounterAction.IncrementA -> {
                    // TODO 2022/9/27: 这里的协程作用域会在 rememberCounterState 所处的组件被销毁时终止，比如在转屏时，协程就会终止
                    launch {
                        repeat(100) {
                            aState++
                            delay(1000)
                        }
                    }.invokeOnCompletion { e ->
                        // 终止时会打印异常
                        Log.d(TAG, "rememberCounterState: $e")
                    }
                }
                CounterAction.IncrementB -> bState++
            }
        }
    }
    return CounterState(aState, bState)
}


private sealed interface CounterAction {

    object IncrementA : CounterAction

    object IncrementB : CounterAction
}

@Immutable
private data class CounterState(val a: Int, val b: Int) {
    val sum get() = a + b
}