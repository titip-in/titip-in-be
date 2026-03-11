package com.titipin.modules.preloved

import com.titipin.database.tables.PrelovedCondition
import com.titipin.database.tables.PrelovedStatus
import com.titipin.database.tables.PrelovedTable
import com.titipin.database.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class PrelovedRepository {

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getAll(): List<PrelovedDto> = dbQuery {
        (PrelovedTable innerJoin UsersTable)
            .selectAll()
            .where { PrelovedTable.status eq PrelovedStatus.AVAILABLE }
            .map { it.toPrelovedDto() }
    }

    suspend fun getById(id: String): PrelovedDto? = dbQuery {
        (PrelovedTable innerJoin UsersTable)
            .selectAll()
            .where { PrelovedTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toPrelovedDto()
    }

    suspend fun create(userId: String, request: CreatePrelovedRequest): PrelovedDto = dbQuery {
        val newId = UUID.randomUUID()
        val now   = LocalDateTime.now()

        PrelovedTable.insert {
            it[PrelovedTable.id]          = newId
            it[PrelovedTable.userId]      = UUID.fromString(userId)
            it[PrelovedTable.title]       = request.title
            it[PrelovedTable.description] = request.description
            it[PrelovedTable.price]       = BigDecimal(request.price)
            it[PrelovedTable.category]    = request.category
            it[PrelovedTable.condition]   = PrelovedCondition.valueOf(request.condition)
            it[PrelovedTable.imageUrl]    = request.imageUrl
            it[PrelovedTable.status]      = PrelovedStatus.AVAILABLE
            it[PrelovedTable.createdAt]   = now
        }

        // query ulang setelah insert biar dapet user object via JOIN
        (PrelovedTable innerJoin UsersTable)
            .selectAll()
            .where { PrelovedTable.id eq newId }
            .single()
            .toPrelovedDto()
    }

    suspend fun delete(id: String, userId: String): Boolean = dbQuery {
        val deleted = PrelovedTable.deleteWhere {
            (PrelovedTable.id eq UUID.fromString(id)) and
                    (PrelovedTable.userId eq UUID.fromString(userId))
        }
        deleted > 0
    }

    suspend fun updateStatus(id: String, userId: String, status: PrelovedStatus): Boolean = dbQuery {
        val updated = PrelovedTable.update({
            (PrelovedTable.id eq UUID.fromString(id)) and
                    (PrelovedTable.userId eq UUID.fromString(userId))
        }) {
            it[PrelovedTable.status] = status
        }
        updated > 0
    }

    private fun ResultRow.toPrelovedDto() = PrelovedDto(
        id          = this[PrelovedTable.id].toString(),
        userId      = this[PrelovedTable.userId].toString(),
        user        = UserSummary(
            name      = this[UsersTable.name],
            waNumber  = this[UsersTable.waNumber],
            avatarUrl = this[UsersTable.avatarUrl]
        ),
        title       = this[PrelovedTable.title],
        description = this[PrelovedTable.description],
        price       = this[PrelovedTable.price].toDouble(),
        category    = this[PrelovedTable.category],
        condition   = this[PrelovedTable.condition].name,
        imageUrl    = this[PrelovedTable.imageUrl],
        status      = this[PrelovedTable.status].name,
        createdAt   = this[PrelovedTable.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}