package com.adrian.diptrack.domain.common

sealed class ModeState {
    data object Idle : ModeState()
    data object Wait : ModeState()
    data object Drive : ModeState()
    data object Arrive : ModeState()
}