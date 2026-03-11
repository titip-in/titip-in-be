package com.titipin.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class WantedStatus { OPEN, FOUND, CLOSED }

object WantedTable : Table("wanted_items") {
    val id          = uuid("id").autoGenerate()
    val userId      = uuid("user_id").references(UsersTable.id)
    val title       = varchar("title", 255)
    val description = varchar("description", 500).nullable()
    val maxPrice    = decimal("max_price", 12, 2).nullable()
    val category    = varchar("category", 100).nullable()
    val status      = enumerationByName("status", 20, WantedStatus::class)
    val createdAt   = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}