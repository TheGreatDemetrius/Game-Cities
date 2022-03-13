package ru.cities.game.db

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.cities.game.util.DATABASE_NAME
import ru.cities.game.util.DATABASE_VERSION
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("${context.packageName}.database_versions",
            Context.MODE_PRIVATE)//the variable for the contents of the database version

    private fun installDatabaseFromAssets() {
        val inputStream = context.assets.open("$DATABASE_NAME.db")
        val outputFile =
            File(context.getDatabasePath(DATABASE_NAME).path)//the input stream for the database file opens
        val outputStream =
            FileOutputStream(outputFile)//the output stream will open in the file where the active database is located
        inputStream.copyTo(outputStream)//the data is copied from the input stream to the output stream
        inputStream.close()
        //outputStream.flush()//flushes this stream by writing any buffered output to the underlying stream
        outputStream.close()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary()
        return super.getReadableDatabase()
    }

    override fun onCreate(p0: SQLiteDatabase?) {}

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    /** Checking the DB, and if it's incorrect, the DB is replaced */
    @Synchronized
    private fun installOrUpdateIfNecessary() {
        if (installedDatabaseIsOutdated()) {
            context.deleteDatabase(DATABASE_NAME)
            installDatabaseFromAssets()
            writeDatabaseVersionInPreferences()
        }
    }

    /** Comparison of DB versions */
    private fun installedDatabaseIsOutdated(): Boolean =
        preferences.getInt(DATABASE_NAME, 0) < DATABASE_VERSION

    /** Writing the DB version to preferences */
    private fun writeDatabaseVersionInPreferences() {
        preferences.edit().apply {
            putInt(DATABASE_NAME, DATABASE_VERSION)
            apply()
        }
    }
}
