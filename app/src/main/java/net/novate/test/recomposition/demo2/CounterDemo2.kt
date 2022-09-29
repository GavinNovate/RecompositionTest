package net.novate.test.recomposition.demo2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier


/**
 * 这个暂时不看了，函数式的 ViewModel 暂时无法解决协程作用域生命周期太短的问题。
 * Android 的 ViewModel 的生命周期比 View 长，所以可以在转屏的时候不暂停数据请求，
 * 但是 Compose的生命周期和 View 一致，在 Compose 中的协程作用域也会比 Android 的 ViewModel 短
 */
@Composable
fun CounterDemo2(modifier: Modifier = Modifier) {

}

//fun rememberCounterViewModel()


@Immutable
data class CounterState(val a: Int, val b: Int) {
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
fun <ViewState, ViewAction> rememberViewModel(
    initState: ViewState,
    viewAction: MutableState<ViewState>.() -> ViewAction
): ViewModel<ViewState, ViewAction> {

    rememberCoroutineScope()

    val state = remember(initState) { mutableStateOf(initState) }
    val action = remember(state) {
        state.viewAction()
    }
    return ViewModel(state.value, action)
}

class ViewModel<ViewState, ViewAction>(private val state: ViewState, private val action: ViewAction) {

    operator fun component1(): ViewState = state

    operator fun component2(): ViewAction = action
}