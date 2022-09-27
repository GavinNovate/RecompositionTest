package net.novate.test.recomposition.demo3

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import net.novate.test.recomposition.CounterContent

private const val TAG = "CounterDemo3"

/**
 * TODO: 描述
 */
@Composable
fun CounterDemo3(modifier: Modifier = Modifier) {
    val (state, action) = rememberCounterViewModel(a = 0, b = 0)
    Log.d(TAG, "CounterDemo3: $state $action")
    CounterContent(
        a = state.a,
        b = state.b,
        sum = state.sum,
        onClickA = action::incrementA,
        onClickB = action::incrementB,
        modifier = modifier
    )
}

@Composable
fun rememberCounterViewModel(a: Int, b: Int): ViewModel<CounterState, CounterAction> {
    val initState = remember { CounterState(a, b) }
    val scope = LocalLifecycleOwner.current.lifecycleScope
    Log.d(TAG, "rememberCounterViewModel: $scope ${scope.hashCode()}")
    return rememberViewModel(initState = initState) {
        object : CounterAction {
            override fun incrementA() {
                // 这个 LifecycleOwner 的 lifecycleScope 将会在转屏的时候自动取消，这样的生命周期没有 ViewModel 生命周期长
                scope.launch {
                    repeat(100) {
                        value = value.copy(a = value.a + 1)
                        delay(1000)
                    }
                }
            }

            override fun incrementB() {
                value = value.copy(b = value.b + 1)
            }
        }
    }
}


@Parcelize
@Immutable
data class CounterState(val a: Int, val b: Int) : Parcelable {
    val sum get() = a + b
}

interface CounterAction {
    fun incrementA()

    fun incrementB()
}

/**
 * 通用的 rememberViewModel 但是感觉不好用
 */
@Composable
fun <ViewState, ViewAction : Any> rememberViewModel(
    initState: ViewState,
    viewAction: MutableState<ViewState>.() -> ViewAction
): ViewModel<ViewState, ViewAction> {

    // rememberSaveable 默认实现必须要能序列化，可以通过 @Parcelize 标记数据类自动序列化
    val state: MutableState<ViewState> = rememberSaveable(initState) { mutableStateOf(initState) }
    val action = remember(state) {
        state.viewAction()
    }
    return ViewModel(state.value, action)
}

class ViewModel<ViewState, ViewAction>(private val state: ViewState, private val action: ViewAction) {

    operator fun component1(): ViewState = state

    operator fun component2(): ViewAction = action
}