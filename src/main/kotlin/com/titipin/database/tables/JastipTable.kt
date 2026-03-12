package com.titipin.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class JastipStatus{ ACTIVE, CLOSED }

object JastipTable : Table("jastip_listings") {
    val id           = uuid("id").autoGenerate()
    val userId       = uuid("user_id").references(UsersTable.id)
    val fromLocation = varchar("from_location", 255)
    val toLocation   = varchar("to_location", 255)
    val deadline     = datetime("deadline")
    val latitude     = double("latitude")
    val longitude    = double("longitude")
    val notes        = varchar("notes", 500).nullable()
    val status       = enumerationByName("status", 20, JastipStatus::class)
    val createdAt    = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}