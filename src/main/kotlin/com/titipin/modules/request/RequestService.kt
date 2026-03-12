package com.titipin.modules.request

import com.titipin.shared.ApiErrorCodes

class RequestService(private val repository: RequestRepository) {

    suspend fun getAll(): List<RequestDto> = repository.getAll()

    suspend fun getById(id: String): RequestDto {
        return repository.getById(id)
            ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)
    }

    suspend fun create(userId: String, request: CreateRequestRequest): RequestDto {
        require(request.fromLocation.isNotBlank()) { "Lokasi awal tidak boleh kosong" }
        require(request.toLocation.isNotBlank()) { "Lokasi tujuan tidak boleh kosong" }

        return repository.create(userId, request)
    }

    suspend fun takeRequest(id: String, takenByUserId: String): TakeRequestResponse {
        val requestItem = repository.getById(id)
            ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)

        if (requestItem.userId == takenByUserId) {
            throw IllegalStateException(ApiErrorCodes.FORBIDDEN)
        }

        if (requestItem.status != RequestStatus.OPEN.name) {
            throw IllegalStateException(ApiErrorCodes.ITEM_NOT_AVAILABLE)
        }

        val updated = repository.takeRequest(id, takenByUserId)
        if (!updated) {
            throw IllegalStateException(ApiErrorCodes.SERVER_ERROR)
        }

        val updatedRequest = repository.getById(id)!!
        val taker = repository.getUserSummary(takenByUserId)!!

        return TakeRequestResponse(updatedRequest, taker)
    }
}