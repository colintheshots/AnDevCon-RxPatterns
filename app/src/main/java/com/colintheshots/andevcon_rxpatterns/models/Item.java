package com.colintheshots.andevcon_rxpatterns.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

/**
 * Item model class for Example2 reactive database example
 *
 * Created by colinlee on 8/4/16.
 */
@StorIOSQLiteType(table = "Items")
public class Item {

    /**
     * If object was not inserted into db, id will be null
     */
    @Nullable
    @StorIOSQLiteColumn(name = "id", key = true)
    Long id;

    @NonNull
    @StorIOSQLiteColumn(name = "desc")
    String desc;

    Item() {
        // required default package level constructor
    }

    public Item(Long id, String description) {
        this.id = id;
        this.desc = description;
    }

    @Nullable
    public Long id() {
        return id;
    }

    @NonNull
    public String desc() {
        return desc;
    }
}
