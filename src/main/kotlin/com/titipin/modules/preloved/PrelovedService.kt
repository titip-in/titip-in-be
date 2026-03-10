package com.titipin.modules.preloved

import com.titipin.database.tables.PrelovedStatus
import com.titipin.shared.ApiErrorCodes

class PrelovedService(private val repository: PrelovedRepository) {

    suspend fun getAll(): List<PrelovedDto> = repository.getAll()

    suspend fun getById(id: String): PrelovedDto {
        return repository.getById(id)
            ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)
    }

    suspend fun create(userId: String, request: CreatePrelovedRequest): PrelovedDto {
        require(request.title.isNotBlank())    { "Judul tidak boleh kosong" }
        require(request.price > 0)             { "Harga harus lebih dari 0" }
        require(request.category.isNotBlank()) { "Kategori tidak boleh kosong" }

        // validasi condition enum
        try {
            enumValueOf<com.titipin.database.tables.PrelovedCondition>(request.condition)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Condition tidak valid. Gunakan: NEW, LIKE_NEW, GOOD, FAIR")
        }

        return repository.create(userId, request)
    }

    suspend fun delete(id: String, userId: String) {
        repository.getById(id)
            ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)

        val deleted = repository.delete(id, userId)
        if (!deleted) {
            throw IllegalStateException(ApiErrorCodes.INSUFFICIENT_PERMISSION)
        }
    }

    suspend fun updateStatus(id: String, userId: String, request: UpdatePrelovedRequest): PrelovedDto {
        val status = try {
            PrelovedStatus.valueOf(request.status)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Status tidak valid. Gunakan: AVAILABLE, SOLD, RESERVED")
        }

        repository.getById(id) ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)

        val updated = repository.updateStatus(id, userId, status)
        if (!updated) throw IllegalStateException(ApiErrorCodes.INSUFFICIENT_PERMISSION)

        return repository.getById(id)!!
    }
}