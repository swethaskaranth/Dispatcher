package com.goflash.dispatch.features.rtoReceiving.view

import com.goflash.dispatch.data.InwardRunItem
import com.goflash.dispatch.data.PackageDto
import com.goflash.dispatch.data.ReceivingShipmentDTO

interface ScanReceiveShipmentView {

    fun onInwardRunItemsFetched(items: List<InwardRunItem>)

    fun onFailure(error: Throwable?)

    fun onInwardRunItemAdded(item: InwardRunItem)

    fun onItemRunChanged(item: InwardRunItem)

    fun onExceptionsFetched(waybillNumber: String, status: String?, list: List<String>)

    fun onStatusUpdated(runItem: InwardRunItem)

    fun enableComplete()

    fun onSuccess(runId: Int)

    fun onMpsRunItemsFetched(runId: Int, total: Int, items: List<InwardRunItem>)

    fun showProgressBar()

    fun disable()

    fun enable()

    fun updateScannedItemCount(count: Int)

    fun showBinName(scannedPackage: PackageDto, shipment: ReceivingShipmentDTO)

    fun showReceivedMessage()
}