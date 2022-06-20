package studio.foxwoosh.database

import kotlinx.coroutines.asCoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import studio.foxwoosh.database.dao.ILyricsDao
import studio.foxwoosh.database.dao.LyricsDao
import studio.foxwoosh.database.tables.Lyrics
import java.util.concurrent.Executors

object AppDatabase {
    fun init() {
        val url = "jdbc:postgresql://127.0.0.1:5432/${System.getenv("DB_NAME")}"
        val driver = "org.postgresql.Driver"
        val user = System.getenv("DB_USER")
        val password = System.getenv("DB_PASSWORD")

        Database.connect(url, driver, user, password)

        transaction {
            SchemaUtils.create(Lyrics)
        }
    }

    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(databaseDispatcher) { block() }
}

private val databaseDispatcher = Executors.newSingleThreadExecutor {
    Thread(it, "Database Thread")
}.asCoroutineDispatcher()

val LyricsDao: ILyricsDao = LyricsDao()