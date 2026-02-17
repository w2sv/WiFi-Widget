package com.w2sv.domain.model

data class IpApiData(
    val location: String,
    val gpsCoordinates: String?,
    val timezone: String,
    val isp: String,
    val asn: String?
)
