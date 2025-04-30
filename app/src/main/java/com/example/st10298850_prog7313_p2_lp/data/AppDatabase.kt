package com.example.st10298850_prog7313_p2_lp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Transaction::class, Account::class, BudgetGoal::class, Category::class],
    version = 2, // Keep this updated when you change the schema
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_VERSION = 2 // This should match the version in @Database

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val prefs = context.getSharedPreferences("database_version", Context.MODE_PRIVATE)
                val storedVersion = prefs.getInt("version", 0)

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )

                if (storedVersion != DATABASE_VERSION) {
                    builder.fallbackToDestructiveMigration()
                }

                builder.addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedDatabase(getDatabase(context))
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Update the stored version number
                        prefs.edit().putInt("version", DATABASE_VERSION).apply()
                    }
                })

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            if (userDao.getUserCount() == 0) {
                val initialUser = User(
                    email = "admin@gmail.com",
                    password = "Admin123$",
                    name = "Admin"
                )
                userDao.insertUser(initialUser)
            }
            // Add more seeding logic here as needed
        }

        fun deleteDatabase(context: Context) {
            context.deleteDatabase("app_database")
            INSTANCE = null
            // Also clear the stored version
            context.getSharedPreferences("database_version", Context.MODE_PRIVATE)
                .edit().remove("version").apply()
        }
    }
}