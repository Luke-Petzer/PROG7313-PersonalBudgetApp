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
    entities = [User::class, Transaction::class, Account::class, BudgetGoal::class, Category::class, MonthlyGoal::class],
    version = 7, // Increment the version number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun categoryDao(): CategoryDao
    abstract fun monthlyGoalDao(): MonthlyGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "app_database"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = createDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun createDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(getDatabase(context))
                    }
                }
            })
            .build()
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            if (userDao.getUserCount() == 0) {
                val initialUser = User(
                    email = "admin@gmail.com",
                    password = "Admin123$",
                    name = "Admin",
                    username = "admin"
                )
                userDao.insertUser(initialUser)
            }
            // Add more seeding logic here as needed
        }

        fun deleteDatabase(context: Context) {
            context.deleteDatabase(DATABASE_NAME)
            INSTANCE = null
        }

        fun reinitializeDatabase(context: Context) {
            deleteDatabase(context)
            INSTANCE = createDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                seedDatabase(INSTANCE!!)
            }
        }

        fun failoverAndReinitialize(context: Context): AppDatabase {
            try {
                return getDatabase(context)
            } catch (e: Exception) {
                // Log the exception
                e.printStackTrace()

                // Attempt to reinitialize the database
                reinitializeDatabase(context)

                // Return the new instance or throw an exception if it fails again
                return INSTANCE ?: throw IllegalStateException("Database failed to initialize")
            }
        }

        // New method for failover database deletion and recreation
        fun forceDeleteAndRecreateDatabase(context: Context): AppDatabase {
            // Delete the existing database
            deleteDatabase(context)

            // Recreate the database
            val newInstance = createDatabase(context)
            INSTANCE = newInstance

            // Reseed the database
            CoroutineScope(Dispatchers.IO).launch {
                seedDatabase(newInstance)
            }

            return newInstance
        }
    }
}