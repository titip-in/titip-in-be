package com.titipin.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class PrelovedStatus { AVAILABLE, SOLD, RESERVED }
enum class PrelovedCondition { NEW, LIKE_NEW, GOOD, FAIR }

object PrelovedTable : Table("preloved_items") {
    val id          = uuid("id").autoGenerate()
    val userId      = uuid("user_id").references(UsersTable.id)
    val title       = varchar("title", 255)
    val description = varchar("description", 1000).nullable()
    val price       = decimal("price", 12, 2)
    val category    = varchar("category", 100)
    val condition   = enumerationByName("condition", 20, PrelovedCondition::class)
    val imageUrl    = varchar("image_url", 500).nullable()
    val status      = enumerationByName("status", 20, PrelovedStatus::class)
    val createdAt   = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}