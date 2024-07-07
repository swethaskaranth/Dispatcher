package com.goflash.dispatch.type

enum class DepositType(val key: String) {
    CMS_PICKUP("CMS Pickup"), MACHINE_DEPOSIT("Machine deposit"), ONLINE_DEPOSIT("Online"),
    SELF_DEPOSIT("Self"), UPI_DEPOSIT("UPI");

    companion object {
        fun toStringList(): ArrayList<String> {
            val list = ArrayList<String>()
            for (mode in values())
                list.add(mode.key)
            return list
        }
    }
}