package com.titipin.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class JastipRequestStatus { OPEN, TAKEN, CLOSED }

object JastipRequestTable : Table("jastip_requests") {
    val id           = uuid("id").autoGenerate()
    val userId       = uuid("user_id").references(UsersTable.id)
    val fromLocation = varchar("from_location", 255)
    val toLocation   = varchar("to_location", 255)
    val notes        = varchar("notes", 500).nullable()
    val status       = enumerationByName("status", 20, JastipRequestStatus::class)
    val takenByUserId = uuid("taken_by_user_id").references(UsersTable.id).nullable()
    val createdAt    = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}