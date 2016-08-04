package com.colintheshots.andevcon_rxpatterns;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.colintheshots.andevcon_rxpatterns.models.Item;
import com.colintheshots.andevcon_rxpatterns.models.ItemSQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Demonstration of using StorIO to provide reactive database access.
 *
 * Created by colin on 7/26/15.
 */
public class Example2 extends Activity {

    private ListView mListView;
    private StorIOSQLite sq;
    private MyAdapter adapter;

    public StorIOSQLite provideStorIOSQLite(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        return DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(sqLiteOpenHelper)
                // ItemSQLiteTypeMapping is auto-generated at compile-time by StorIO
                .addTypeMapping(Item.class, new ItemSQLiteTypeMapping())
                .build();
    }

    public SQLiteOpenHelper provideSQLiteOpenHelper(@NonNull Context context) {
        return new DbOpenHelper(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example2);
        mListView = (ListView) findViewById(R.id.listView);

        deleteDatabase("sample_db");

        sq = provideStorIOSQLite(provideSQLiteOpenHelper(this));
        List<Item> newItems = new ArrayList<>();
        newItems.add(new Item(1L, "1st item"));
        newItems.add(new Item(2L, "2nd item"));

        adapter = new MyAdapter();
        mListView.setAdapter(adapter);

        // INSERT
        sq.put().objects(newItems).prepare().asRxSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(itemPutResults -> {
                    Log.d("Example2", "Num inserts is : "+itemPutResults.numberOfInserts());
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // QUERY
        sq.get().listOfObjects(Item.class)
                .withQuery(Query.builder().table("Items").build()).prepare().asRxObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    Log.d("Example2", "Queried items");
                    adapter.setItems(items);
                });

        // DELETE
        mListView.setOnItemClickListener((adapterView, view, i, l) ->
                sq.delete().object(adapter.getItem(i)).prepare().asRxCompletable()
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d("Example2", "Deleted row");
                }));
    }

    private class DbOpenHelper extends SQLiteOpenHelper {

        public DbOpenHelper(@NonNull Context context) {
            super(context, "sample_db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE Items(id INTEGER NOT NULL PRIMARY KEY, desc TEXT NOT NULL);");
        }


        @Override
        public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
            // no impl
        }
    }

    private class MyAdapter extends BaseAdapter {

        List<Item> items = new ArrayList<>();

        public MyAdapter() {}

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return items.get(i).id();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);

            TextView title = (TextView) row.findViewById(android.R.id.text1);
            title.setText(items.get(i).desc());

            return row;
        }

        public void setItems(List<Item> items) {
            this.items = items;
            notifyDataSetChanged();
        }
    }
}
