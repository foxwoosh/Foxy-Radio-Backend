package studio.foxwoosh.database.tables

import org.jetbrains.exposed.sql.Table

data class User(
    val id: Long,
    val login: String,
    val password: String,
    val email: String,
    val name: String
)

object Users : Table() {
    val id = long("id").autoIncrement()
    val login = text("login")
    val password = text("password")
    val email = text("email")
    val name = text("name")

    override val primaryKey = PrimaryKey(id)
}