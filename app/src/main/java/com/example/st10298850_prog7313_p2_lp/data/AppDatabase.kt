package com.example.st10298850_prog7313_p2_lp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase is the main database class for the application.
 * It uses Room, a persistence library, to manage SQLite operations.
 * This class follows the singleton pattern to ensure only one instance of the database is created.
 */
@Database(
    entities = [User::class, Transaction::class, Account::class, BudgetGoal::class, Category::class, MonthlyGoal::class],
    version = 7, // Database version, increment when schema changes
    exportSchema = false // Don't export database schema
)
abstract class AppDatabase : RoomDatabase() {
    // Data Access Objects (DAOs) for different entities
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun categoryDao(): CategoryDao
    abstract fun monthlyGoalDao(): MonthlyGoalDao

    companion object {
        // Singleton instance of the database
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "app_database"

        /**
         * Gets the singleton instance of the database.
         * If the instance doesn't exist, it creates one.
         *
         * @param context The application context
         * @return The singleton database instance
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = createDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Creates a new database instance.
         *
         * @param context The application context
         * @return A new database instance
         */
        private fun createDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // Allows Room to destructively recreate database tables if Migrations that would migrate old database schemas to the latest schema version are not found
            .addCallback(object : RoomDatabase.Callback() {
                // Called when the database is created for the first time
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Populate the database in the background
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(getDatabase(context))
                    }
                }
            })
            .build()
        }

        /**
         * Seeds the database with initial data.
         *
         * @param database The database instance to seed
         */
        private suspend fun seedDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            // Check if the database is empty
            if (userDao.getUserCount() == 0) {
                // Create an initial admin user
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

        /**
         * Deletes the existing database.
         *
         * @param context The application context
         */
        fun deleteDatabase(context: Context) {
            context.deleteDatabase(DATABASE_NAME)
            INSTANCE = null
        }

        /**
         * Reinitializes the database by deleting the existing one and creating a new instance.
         *
         * @param context The application context
         */
        fun reinitializeDatabase(context: Context) {
            deleteDatabase(context)
            INSTANCE = createDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                seedDatabase(INSTANCE!!)
            }
        }

        /**
         * Attempts to get the database instance, and if it fails, reinitializes the database.
         *
         * @param context The application context
         * @return The database instance
         * @throws IllegalStateException if the database fails to initialize after reinitializing
         */
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

        /**
         * Forces deletion of the existing database and creates a new one.
         * This is a more aggressive approach to handling database issues.
         *
         * @param context The application context
         * @return A new database instance
         */
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