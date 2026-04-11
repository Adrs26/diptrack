package com.adrian.diptrack.data.common

sealed class ModeState {
    data object Idle : ModeState()
    data class  Wait(val busId: Int) : ModeState()
    data class  Drive(val busId: Int) : ModeState()
    data object Arrive : ModeState()
}
