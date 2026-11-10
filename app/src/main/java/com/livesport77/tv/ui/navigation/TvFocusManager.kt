package com.livesport77.tv.ui.navigation

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.*

/**
 * TV D-Pad Key Event Handler.
 * Intercepts Android TV Remote KeyEvents to provide fluid, predictable focus transitions.
 */
object TvKeyEventHandler {

    fun handleDpadNavigation(
        event: KeyEvent,
        focusManager: FocusManager,
        onSelect: (() -> Unit)? = null,
        onBack: (() -> Boolean)? = null
    ): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                focusManager.moveFocus(FocusDirection.Up)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                focusManager.moveFocus(FocusDirection.Down)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                focusManager.moveFocus(FocusDirection.Left)
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                focusManager.moveFocus(FocusDirection.Right)
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                onSelect?.invoke()
                true
            }
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                onBack?.invoke() ?: false
            }
            else -> false
        }
    }
}

