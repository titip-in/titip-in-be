package com.titipin.modules.request

import com.titipin.database.tables.UsersTable
import com.titipin.database.tables.RequestTable
import com.titipin.database.tables.RequestStatus
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RequestRepository {

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun getAll(): List<RequestDto> = dbQuery {
        RequestTable
            .join(
                otherTable = UsersTable,
                joinType = JoinType.INNER,
                onColumn = RequestTable.userId,
                otherColumn = UsersTable.id
            )
            .selectAll()
            .where { RequestTable.status eq RequestStatus.OPEN }
            .map { it.toRequestDto() }
    }

    suspend fun getById(id: String): RequestDto? = dbQuery {
        RequestTable
            .join(
                otherTable = UsersTable,
                joinType = JoinType.INNER,
                onColumn = RequestTable.userId,
                otherColumn = UsersTable.id
            )
            .selectAll()
            .where { RequestTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toRequestDto()
    }

    suspend fun create(userId: String, request: CreateRequestRequest): RequestDto = dbQuery {
        val newId = UUID.randomUUID()

        RequestTable.insert {
            it[id]                   = newId
            it[RequestTable.userId]  = UUID.fromString(userId)
            it[fromLocation]         = request.fromLocation
            it[toLocation]           = request.toLocation
            it[notes]                = request.notes
            it[status]               = RequestStatus.OPEN
            it[createdAt]            = LocalDateTime.now()
        }

        RequestTable
            .join(
                otherTable = UsersTable,
                joinType = JoinType.INNER,
                onColumn = RequestTable.userId,
                otherColumn = UsersTable.id
            )
            .selectAll()
            .where { RequestTable.id eq newId }
            .single()
            .toRequestDto()
    }

    suspend fun takeRequest(id: String, takenByUserId: String): Boolean = dbQuery {
        val updated = RequestTable.update({ RequestTable.id eq UUID.fromString(id) }) {
            it[status]        = RequestStatus.TAKEN
            it[RequestTable.takenByUserId] = UUID.fromString(takenByUserId)
        }
        updated > 0
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

    private fun ResultRow.toRequestDto() = RequestDto(
        id           = this[RequestTable.id].toString(),
        userId       = this[RequestTable.userId].toString(),
        user         = UserSummary(
            name      = this[UsersTable.name],
            waNumber  = this[UsersTable.waNumber],
            avatarUrl = this[UsersTable.avatarUrl]
        ),
        fromLocation = this[RequestTable.fromLocation],
        toLocation   = this[RequestTable.toLocation],
        notes        = this[RequestTable.notes],
        status       = this[RequestTable.status].name,
        createdAt    = this[RequestTable.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}