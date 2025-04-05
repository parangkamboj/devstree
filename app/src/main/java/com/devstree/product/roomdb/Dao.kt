package com.devstree.product.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devstree.product.model.AddressSuggestModel

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(addressSuggestModel: AddressSuggestModel): Long

    @Query("SELECT * FROM AddressSuggestModel")
    suspend fun getAllUsers(): List<AddressSuggestModel>

    @Query("DELETE FROM AddressSuggestModel WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    @Update
    suspend fun updateUser(address: AddressSuggestModel)
}