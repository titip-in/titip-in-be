package com.titipin.modules.wanted

import com.titipin.database.tables.UsersTable
import com.titipin.database.tables.WantedStatus
import com.titipin.database.tables.WantedTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class WantedRepository {

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getAllOpen(): List<WantedDto> = dbQuery {
        (WantedTable innerJoin UsersTable)
            .selectAll()
            .where { WantedTable.status eq WantedStatus.OPEN }
            .map { it.toWantedDto() }
    }

    suspend fun getById(id: String): WantedDto? = dbQuery {
        (WantedTable innerJoin UsersTable)
            .selectAll()
            .where { WantedTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toWantedDto()
    }

    suspend fun create(userId: String, request: CreateWantedRequest): WantedDto = dbQuery {
        val newId = UUID.randomUUID()
        WantedTable.insert {
            it[id] = newId
            it[WantedTable.userId] = UUID.fromString(userId)
            it[title] = request.title
            it[description] = request.description
            it[maxPrice] = request.maxPrice?.let { price -> BigDecimal(price) }
            it[category] = request.category
            it[status] = WantedStatus.OPEN
            it[createdAt] = LocalDateTime.now()
        }

        (WantedTable innerJoin UsersTable).selectAll().where { WantedTable.id eq newId }.single().toWantedDto()
    }

    suspend fun fulfillWanted(id: String, foundByUserId: String): Boolean = dbQuery {
        WantedTable.update({ WantedTable.id eq UUID.fromString(id) }) {
            it[status] = WantedStatus.FOUND
            it[WantedTable.foundByUserId] = UUID.fromString(foundByUserId)
        } > 0
    }

    suspend fun getUserSummary(userId: String): UserSummary? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq UUID.fromString(userId) }
            .singleOrNull()?.let {
                UserSummary(
                    name = it[UsersTable.name],
                    waNumber = it[UsersTable.waNumber],
                    avatarUrl = it[UsersTable.avatarUrl]
                )
            }
    }

    private fun ResultRow.toWantedDto() = WantedDto(
        id          = this[WantedTable.id].toString(),
        userId      = this[WantedTable.userId].toString(),
        user        = UserSummary(
            name      = this[UsersTable.name],
            waNumber  = this[UsersTable.waNumber],
            avatarUrl = this[UsersTable.avatarUrl]
        ),
        title       = this[WantedTable.title],
        description = this[WantedTable.description],
        maxPrice    = this[WantedTable.maxPrice]?.toDouble(),
        category    = this[WantedTable.category],
        status      = this[WantedTable.status].name,
        createdAt   = this[WantedTable.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}