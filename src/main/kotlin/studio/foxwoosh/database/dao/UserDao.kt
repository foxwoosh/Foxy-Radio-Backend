package studio.foxwoosh.database.dao

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import studio.foxwoosh.database.AppDatabase
import studio.foxwoosh.database.UserDispatcher
import studio.foxwoosh.database.tables.User
import studio.foxwoosh.database.tables.UserRole
import studio.foxwoosh.database.tables.Users

interface IUserDao {
    suspend fun get(login: String): User?
    suspend fun get(id: Long): User?
    suspend fun save(login: String, password: String, name: String, email: String, role: UserRole): User?
    suspend fun update(login: String, name: String?, email: String?): Boolean
}

class UserDao : IUserDao {
    override suspend fun get(login: String): User? = AppDatabase.query(UserDispatcher) {
        Users
            .select { Users.login eq login }
            .map { get(it) }
            .singleOrNull()
    }

    override suspend fun get(id: Long): User? = AppDatabase.query(UserDispatcher) {
        Users
            .select { Users.id eq id }
            .map { get(it) }
            .singleOrNull()
    }

    override suspend fun save(login: String, password: String, name: String, email: String, role: UserRole): User? {
        val statement = AppDatabase.query(UserDispatcher) {
            Users.insert { s ->
                s[Users.login] = login
                s[Users.password] = password
                s[Users.email] = email
                s[Users.name] = name
                s[Users.role] = role.name
            }
        }

        return statement.resultedValues?.first()?.let { get(it) }
    }

    override suspend fun update(login: String, name: String?, email: String?): Boolean {
        return Users.update({ Users.login eq login }) { statement ->
            name?.let { statement[Users.name] = it }
            email?.let { statement[Users.email] = it }
        } > 0
    }

    private fun get(row: ResultRow) = User(
        row[Users.id],
        row[Users.login],
        row[Users.password],
        row[Users.email],
        row[Users.name],
        UserRole.valueOf(row[Users.role])
    )
}