package com.example.adventure.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.adventure.data.local.model.Bookmark
import com.example.adventure.data.local.model.Location

@Database(entities = [Location::class, Bookmark::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `stateName` TEXT NOT NULL, `stateAbbreviation` TEXT NOT NULL, `cityName` TEXT NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `Location_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `name` TEXT NOT NULL)")

                // Copy data from the old table to the new table, excluding the removed column (locationKey)
                db.execSQL(
                    "INSERT INTO `Location_new` (`name`, `latitude`, `longitude`) " +
                            "SELECT `name`, `latitude`, `longitude` " +
                            "FROM `location`"
                )

                // Drop the old table
                db.execSQL("DROP TABLE `Location`")

                // Rename the new table to the original table name
                db.execSQL("ALTER TABLE `Location_new` RENAME TO `Location`")
            }
        }
    }

}