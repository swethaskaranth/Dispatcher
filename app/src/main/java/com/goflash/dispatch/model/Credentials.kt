package com.goflash.dispatch.model

import com.goflash.dispatch.app_constants.APP_NAME

data class Credentials(
    val token: String? = null,
    val mobileNumber: String? = null,
    val resendOtp: Boolean = false,
    val otp: String? = null,
    val app: String = APP_NAME,
    val version: String = com.goflash.dispatch.BuildConfig.VERSION_NAME,
    val domainName: String = "BOLT"
)
