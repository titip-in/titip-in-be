package com.titipin.modules.jastip

import com.titipin.database.tables.JastipStatus
import com.titipin.database.tables.JastipTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class JastipRepository {

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    // GET semua jastip
    suspend fun getAll(): List<JastipDto> = dbQuery {
        JastipTable
            .selectAll()
            .where { JastipTable.status eq JastipStatus.ACTIVE }
            .map { it.toJastipDto() }
    }

    // GET jastip by ID
    suspend fun getById(id: String): JastipDto? = dbQuery {
        JastipTable
            .selectAll()
            .where { JastipTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toJastipDto()
    }

    // POST — buat jastip baru
    suspend fun create(userId: String, request: CreateJastipRequest): JastipDto = dbQuery {
        val newId    = UUID.randomUUID()
        val deadline = LocalDateTime.parse(request.deadline)
        val now      = LocalDateTime.now()

        JastipTable.insert {
            it[JastipTable.id]           = newId
            it[JastipTable.userId]       = UUID.fromString(userId)
            it[JastipTable.fromLocation] = request.fromLocation
            it[JastipTable.toLocation]   = request.toLocation
            it[JastipTable.deadline]     = deadline
            it[JastipTable.latitude]     = request.latitude
            it[JastipTable.longitude]    = request.longitude
            it[JastipTable.notes]        = request.notes
            it[JastipTable.status]       = JastipStatus.ACTIVE
            it[JastipTable.createdAt]    = now
        }

        JastipDto(
            id           = newId.toString(),
            userId       = userId,
            fromLocation = request.fromLocation,
            toLocation   = request.toLocation,
            deadline     = request.deadline,
            latitude     = request.latitude,
            longitude    = request.longitude,
            notes        = request.notes,
            status       = JastipStatus.ACTIVE.name,
            createdAt    = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    // DELETE — hapus jastip (cuma owner yang bisa)
    suspend fun delete(id: String, userId: String): Boolean = dbQuery {
        val deleted = JastipTable.deleteWhere {
            (JastipTable.id eq UUID.fromString(id)) and
                    (JastipTable.userId eq UUID.fromString(userId))
        }
        deleted > 0 // true kalau berhasil dihapus
    }

    // helper — convert ResultRow → JastipDto
    private fun ResultRow.toJastipDto() = JastipDto(
        id           = this[JastipTable.id].toString(),
        userId       = this[JastipTable.userId].toString(),
        fromLocation = this[JastipTable.fromLocation],
        toLocation   = this[JastipTable.toLocation],
        deadline     = this[JastipTable.deadline].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        latitude     = this[JastipTable.latitude],
        longitude    = this[JastipTable.longitude],
        notes        = this[JastipTable.notes],
        status       = this[JastipTable.status].name,
        createdAt    = this[JastipTable.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}