package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Category
import com.example.data.model.Transaction
import java.util.concurrent.Executors

@Database(entities = [Category::class, Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_tracker_db"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Execute on background thread
                Executors.newSingleThreadExecutor().execute {
                    // Populate pre-built categories
                    val expenses = listOf(
                        "('Food', 'EXPENSE', 'Fastfood', 0)",
                        "('Shopping', 'EXPENSE', 'ShoppingBag', 0)",
                        "('Transport', 'EXPENSE', 'DirectionsCar', 0)",
                        "('Mobile Recharge', 'EXPENSE', 'PhoneAndroid', 0)",
                        "('Internet', 'EXPENSE', 'Wifi', 0)",
                        "('Bills', 'EXPENSE', 'ReceiptLong', 0)",
                        "('Education', 'EXPENSE', 'School', 0)",
                        "('Entertainment', 'EXPENSE', 'SportsEsports', 0)",
                        "('Medical', 'EXPENSE', 'LocalPharmacy', 0)",
                        "('Others', 'EXPENSE', 'Category', 0)"
                    )
                    val incomes = listOf(
                        "('Salary', 'INCOME', 'Payments', 0)",
                        "('Business', 'INCOME', 'Storefront', 0)",
                        "('Freelancing', 'INCOME', 'Computer', 0)",
                        "('Tuition', 'INCOME', 'School', 0)",
                        "('Gift', 'INCOME', 'CardGiftcard', 0)",
                        "('Others', 'INCOME', 'AccountBalance', 0)"
                    )

                    for (item in expenses) {
                        db.execSQL("INSERT OR IGNORE INTO categories (name, type, iconName, isCustom) VALUES $item")
                    }
                    for (item in incomes) {
                        db.execSQL("INSERT OR IGNORE INTO categories (name, type, iconName, isCustom) VALUES $item")
                    }
                }
            }
        }
    }
}
