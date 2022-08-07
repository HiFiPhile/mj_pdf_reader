package com.gitlab.mudlej.MjPdfReader;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SavedLocationDao {
    @Query("SELECT pageNumber FROM SavedLocation WHERE hash = :hash")
    Integer findSavedPage(String hash);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SavedLocation saveLocations);
}
