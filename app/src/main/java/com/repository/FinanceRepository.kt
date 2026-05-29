package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.Category
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getCategoriesByType(type: String): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun clearAllTransactions() {
        transactionDao.clearAllTransactions()
    }
}
