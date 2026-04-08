package com.adrian.diptrack.domain.common.result

import com.adrian.diptrack.domain.model.Coordinate

sealed interface MapsPermissionResult {
    data class  Granted(val location: Coordinate) : MapsPermissionResult
    data object Denied : MapsPermissionResult
}