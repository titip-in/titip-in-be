package com.titipin.modules.jastip


import com.titipin.database.tables.JastipStatus
import com.titipin.shared.ApiErrorCodes

class JastipService(private val repository: JastipRepository) {

    // GET semua jastip
    suspend fun getAll(): List<JastipDto> {
        return repository.getAll()
    }

    // GET by ID
    suspend fun getById(id: String): JastipDto {
        return repository.getById(id)
            ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)
    }

    // POST — buat jastip baru
    suspend fun create(userId: String, request: CreateJastipRequest): JastipDto {
        require(request.fromLocation.isNotBlank()) { "From location tidak boleh kosong" }
        require(request.toLocation.isNotBlank())   { "To location tidak boleh kosong" }
        require(request.deadline.isNotBlank())     { "Deadline tidak boleh kosong" }

        return repository.create(userId, request)
    }

    // DELETE
    suspend fun delete(id: String, userId: String) {
        // cek jastip ada ga dulu
        repository.getById(id)
            ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)

        // hapus — kalau return false berarti bukan punya dia
        val deleted = repository.delete(id, userId)
        if (!deleted) {
            throw IllegalStateException(ApiErrorCodes.INSUFFICIENT_PERMISSION)
        }
    }

    suspend fun updateStatus(id: String, userId: String, request: UpdateJastipRequest): JastipDto {
        // validasi status
        val status = try {
            JastipStatus.valueOf(request.status)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Status tidak valid. Gunakan: ACTIVE, CLOSED")
        }

        // cek exist
        repository.getById(id) ?: throw IllegalStateException(ApiErrorCodes.NOT_FOUND)

        // update
        val updated = repository.updateStatus(id, userId, status)
        if (!updated) throw IllegalStateException(ApiErrorCodes.INSUFFICIENT_PERMISSION)

        return repository.getById(id)!!
    }
}