package com.ridwanharts.qrcodebarcodescanner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class DatabaseHelper (context: Context): ManagedSQLiteOpenHelper(
    context, "result.db", null, 1
){
    companion object {
        private var instance: DatabaseHelper? = null
        const val TABLE_RESULT: String = "table_result"
        const val ID: String = "id_"
        const val TITLE: String = "title"
        const val RESULT: String = "result"
        const val DATE: String = "date"

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context)
            }
            return instance as DatabaseHelper
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.createTable(TABLE_RESULT, true,
            ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
            TITLE to TEXT,
            RESULT to TEXT,
            DATE to TEXT
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.dropTable("TABLE_RESULT", true)
    }

}

//extension function
val Context.database: DatabaseHelper
    get() = DatabaseHelper(applicationContext)