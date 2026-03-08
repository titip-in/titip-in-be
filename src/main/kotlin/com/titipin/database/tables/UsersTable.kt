package com.titipin.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val name      = varchar("name", 255)
    val email     = varchar("email", 255).uniqueIndex()
    val password  = varchar("password", 255)
    val waNumber  = varchar("wa_number", 20)
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}