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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow

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

    val (state, actions) = rememberViewModel<CounterState, CounterActions>(viewState = remember {
        CounterState(0, 0)
    }) {
        object : CounterActions {
            override fun incrementA() {
                value = value.copy(a = value.a + 1)
            }

            override fun incrementB() {
                value = value.copy(b = value.b + 1)
            }
        }
    }

    Row(modifier = modifier) {

        Button(a = state.a, onClick = actions::incrementA)

        Text(
            text = "+",
            fontSize = 24.sp,
            modifier = Modifier.padding(8.dp)
        )

        Button(a = state.b, onClick = actions::incrementB)

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
data class CounterState(val a: Int, val b: Int) : ViewState {
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

interface CounterActions : ViewActions {
    fun incrementA()

    fun incrementB()
}

data class CounterViewModel(val state: CounterState, val actions: CounterActions)

@Composable
fun rememberCounterViewModel(a: Int, b: Int): CounterViewModel {
    var _a by remember(a) { mutableStateOf(a) }
    var _b by remember(b) { mutableStateOf(b) }
    var state by remember(a, b) { mutableStateOf(CounterState(a, b)) }
    val actions: CounterActions = remember {
        object : CounterActions {
            override fun incrementA() {
                state = state.copy(a = state.a + 1)
            }

            override fun incrementB() {
                state = state.copy(b = state.b + 1)
            }
        }
    }
    return CounterViewModel(state, actions)
}

@Composable
fun <VS : ViewState, VA : ViewActions> rememberViewModel(
    viewState: VS,
    viewActions: MutableState<VS>.() -> VA
): ViewModel<VS, VA> {
    val state = remember(viewState) { mutableStateOf(viewState) }
    val actions = remember {
        state.viewActions()
    }
    return ViewModel(state.value, actions)
}

interface ViewState

interface ViewActions

data class ViewModel<VS : ViewState, VA : ViewActions>(val state: VS, val actions: VA)