package com.goflash.dispatch.model

import co.uk.rushorm.core.Rush
import co.uk.rushorm.core.RushCallback
import co.uk.rushorm.core.RushCore
import java.io.Serializable

/**
 *Created by Ravi on 2019-09-17.
 */
data class BagDetails(var bagId: String,
                      var reason: String? = null): Rush, Serializable{

    constructor(): this("", null)

    override fun save() {
        RushCore.getInstance().saveOnlyWithoutConflict(this)
    }

    override fun save(callback: RushCallback) {
        RushCore.getInstance().save(this, callback)
    }

    override fun delete() {
        RushCore.getInstance().delete(this)
    }

    override fun delete(callback: RushCallback) {
        RushCore.getInstance().delete(this, callback)
    }

    override fun getId(): String? {
        return null
    }

}