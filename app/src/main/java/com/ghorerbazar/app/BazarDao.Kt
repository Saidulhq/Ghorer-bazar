package com.ghorerbazar.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BazarDao {
    @Query("SELECT * FROM bazar_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<BazarItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BazarItemEntity)

    @Query("SELECT SUM(totalPrice) FROM bazar_items")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT * FROM shopping_list ORDER BY id DESC")
    fun getShoppingList(): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingListItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistory)
}
