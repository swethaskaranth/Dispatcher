package com.goflash.dispatch.features.dispatch.view

interface TripRowView {

    fun setTripDate(date : String)

    fun setTripId( id : String)

    fun setTripDestination(destination : String)

    fun setSprinterName(name : String)

    fun setTripStatus(status : String)
}