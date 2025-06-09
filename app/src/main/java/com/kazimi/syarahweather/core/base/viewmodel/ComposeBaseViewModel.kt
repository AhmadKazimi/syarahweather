package com.kazimi.syarahweather.core.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class ComposeBaseViewModel : ViewModel() {
    /**
     * Intent is an action that ViewModel will send only <b>once</b> to interact with the view; for instance:
     * - Navigation.
     * - Show SnackBar
     */
    val viewAction =
        MutableSharedFlow<ViewAction>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /**
     * Handle view interaction with ViewModel
     */
    fun proceed(viewIntent: ViewIntent) {
        viewModelScope.launch {
            processViewIntent(viewIntent)
        }
    }

    /**
     * Listen to the view intents
     */
    protected open suspend fun processViewIntent(intent: ViewIntent) = Unit

    /**
     * Will send an action to view
     */
    open fun sendAction(viewAction: ViewAction) = this.viewAction.tryEmit(viewAction)

    open fun resetViewAction() = viewAction.tryEmit(EmptyAction())
} 