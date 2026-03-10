package com.titipin.modules.auth

import com.titipin.database.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class AuthRepository {
    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun findUserByEmail(email: String): ResultRow? = dbQuery {
        UsersTable
            .selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
    }
    suspend fun findUserById(id: String): UserDto? = dbQuery {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.let { with(this) { it.toUserDto() } }
    }

    suspend fun createUser(request: RegisterRequest, hashedPassword: String): UserDto = dbQuery {
        val newId = UUID.randomUUID()

        UsersTable.insert {
            it[id] = newId
            it[name] = request.name
            it[email] = request.email
            it[password] = hashedPassword
            it[waNumber] = request.waNumber
            it[createdAt] = LocalDateTime.now()
        }

        UserDto(
            id = newId.toString(),
            name = request.name,
            email = request.email,
            waNumber = request.waNumber
        )
    }

    fun ResultRow.toUserDto() = UserDto(
        id        = this[UsersTable.id].toString(),
        name      = this[UsersTable.name],
        email     = this[UsersTable.email],
        waNumber  = this[UsersTable.waNumber],
        avatarUrl = this[UsersTable.avatarUrl]
    )

}