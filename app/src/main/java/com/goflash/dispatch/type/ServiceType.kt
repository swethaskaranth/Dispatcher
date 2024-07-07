package com.goflash.dispatch.type

enum class ServiceType(val displayName: String) {
    B2B_EXP_NC("B2B Express"), B2B_NORMAL_NC("B2B Normal"),
    B2C_EXP_NC("B2C Express"), B2C_NORMAL_NC("B2C Normal"),
    B3_EXP_NC("B3 Express"), B3_NORMAL_NC("B3 Normal");

    companion object{
        fun getEnumNameByName(name: String): ServiceType? {
            for (value in ServiceType.values()) {
                if (value.name == name) {
                    return value
                }
            }
            return null
        }

        fun getEnumNameByDisplayName(displayName: String): ServiceType? {
            for (value in ServiceType.values()) {
                if (value.displayName == displayName) {
                    return value
                }
            }
            return null
        }

        fun getValues(): List<String>{
            return ServiceType.values().map { it.displayName }
        }
    }
}