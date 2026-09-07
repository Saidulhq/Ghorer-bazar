
package com.ghorerbazar.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bazar_items")
data class BazarItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val totalPrice: Double,
    val date: String,
    val time: String = "",
    val shopName: String = "",
    val paymentMethod: String = "নগদ",
    val note: String = "",
    val isPaid: Boolean = true
)

@Entity(tableName = "shopping_list")
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: String,
    val isBought: Boolean = false
)

@Entity(tableName = "favorite_items")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val defaultUnit: String,
    val defaultPrice: Double
)

@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val price: Double,
    val date: String
)

@Dao
interface BazarDao {
    @Query("SELECT * FROM bazar_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<BazarItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BazarItemEntity)

    @Delete
    suspend fun deleteItem(item: BazarItemEntity)

    @Query("SELECT SUM(totalPrice) FROM bazar_items")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT * FROM shopping_list ORDER BY id DESC")
    fun getShoppingList(): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingListItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingListItem)

    @Query("SELECT * FROM favorite_items ORDER BY id DESC")
    fun getFavoriteItems(): Flow<List<FavoriteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteItem)

    @Query("SELECT * FROM price_history WHERE itemName = :name ORDER BY id DESC")
    fun getPriceHistory(name: String): Flow<List<PriceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistory)
}

@Database(
    entities = [BazarItemEntity::class, ShoppingListItem::class, FavoriteItem::class, PriceHistory::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bazarDao(): BazarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghorer_bazar_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
