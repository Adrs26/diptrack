package com.adrian.diptrack.data.common.result

import com.adrian.diptrack.data.common.model.Coordinate

sealed interface MapsPermissionResult {
    data class  Granted(val location: Coordinate) : MapsPermissionResult
    data object Denied : MapsPermissionResult
}