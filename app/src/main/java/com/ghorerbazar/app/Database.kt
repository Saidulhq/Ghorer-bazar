package com.ghorerbazar.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Data Entity (বাজার আইটেমের টেবিল)
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
    val isPaid: Boolean = true
)

// 2. Data Access Object (DAO)
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
}

// 3. Room Database
@Database(entities = [BazarItemEntity::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
