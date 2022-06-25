package studio.foxwoosh.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import studio.foxwoosh.database.dao.ILyricsDao
import studio.foxwoosh.database.dao.IUserDao
import studio.foxwoosh.database.dao.LyricsDao
import studio.foxwoosh.database.dao.UserDao
import studio.foxwoosh.database.tables.Lyrics
import studio.foxwoosh.database.tables.Users
import java.util.concurrent.Executors

object AppDatabase {
    fun init() {
        val url = "jdbc:postgresql://127.0.0.1:5432/${System.getenv("DB_NAME")}"
        val driver = "org.postgresql.Driver"
        val user = System.getenv("DB_USER")
        val password = System.getenv("DB_PASSWORD")

        Flyway
            .configure()
            .dataSource(url, user, password)
            .load()
            .baseline()

//            .migrate()

        Database.connect(url, driver, user, password)

        transaction {
            SchemaUtils.create(
                Lyrics,
                Users
            )
        }
    }

    suspend fun <T> query(dispatcher: CoroutineDispatcher, block: suspend () -> T): T =
        newSuspendedTransaction(dispatcher) { block() }
}

val LyricsDispatcher = Executors.newSingleThreadExecutor {
    Thread(it, "Database Thread")
}.asCoroutineDispatcher()

val UserDispatcher = Dispatchers.IO

val LyricsDao: ILyricsDao = LyricsDao()
val UserDao: IUserDao = UserDao()