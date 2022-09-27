package net.novate.test.recomposition.demo3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import net.novate.test.recomposition.CounterContent

/**
 * TODO: 描述
 */
@Composable
fun CounterDemo3(modifier: Modifier = Modifier) {
    val (state, action) = rememberCounterViewModel(a = 0, b = 0)
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
    return rememberViewModel(initState = initState) {
        object : CounterAction {
            override fun incrementA() {
                value = value.copy(a = value.a + 1)
            }

            override fun incrementB() {
                value = value.copy(b = value.b + 1)
            }
        }
    }
}


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

    val state = rememberSaveable(initState) { mutableStateOf(initState) }
    val action = remember(state) {
        state.viewAction()
    }
    return ViewModel(state.value, action)
}

class ViewModel<ViewState, ViewAction>(private val state: ViewState, private val action: ViewAction) {

    operator fun component1(): ViewState = state

    operator fun component2(): ViewAction = action
}