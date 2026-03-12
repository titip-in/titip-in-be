package com.titipin.modules.wanted

import com.titipin.database.tables.WantedStatus
import com.titipin.shared.ApiErrorCodes

class WantedService(private val repository: WantedRepository) {

    suspend fun getAllOpen(): List<WantedDto> = repository.getAllOpen()

    suspend fun create(userId: String, request: CreateWantedRequest): WantedDto {
        require(request.title.isNotBlank()) { "Judul pencarian tidak boleh kosong" }
        if (request.maxPrice != null) require(request.maxPrice > 0) { "Budget harus lebih dari 0" }
        return repository.create(userId, request)
    }

    suspend fun fulfillWanted(id: String, fulfillerId: String): FulfillWantedResponse {
        val wantedItem = repository.getById(id) ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)

        if (wantedItem.userId == fulfillerId) {
            throw IllegalStateException(ApiErrorCodes.FORBIDDEN)
        }

        if (wantedItem.status != WantedStatus.OPEN.name) {
            throw IllegalStateException(ApiErrorCodes.ITEM_NOT_AVAILABLE)
        }

        val updated = repository.fulfillWanted(id, fulfillerId)
        if (!updated) throw IllegalStateException(ApiErrorCodes.SERVER_ERROR)

        val updatedItem = repository.getById(id)!!
        val fulfiller = repository.getUserSummary(fulfillerId)!!

        return FulfillWantedResponse(updatedItem, fulfiller)
    }
}