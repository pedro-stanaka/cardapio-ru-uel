package br.uel.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import br.uel.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class SqliteDao<T> implements Dao<T> {

    private SQLiteDatabase database;

    private String tableName;

    public SqliteDao(Context context, String tableName) {
        DbHelper helper = DbHelper.getInstance(context);
        database = helper.getWritableDatabase();
        this.tableName = tableName;
    }

    @Override
    public void insert(T object) {
        ContentValues values = new ContentValues();
        populateValues(values, object);
        database.insert(tableName, null, values);
    }

    @Override
    public void insert(Collection<T> objects) {
        for(T object : objects) {
            this.insert(object);
        }
    }

    @Override
    public T findById(long id) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE _id = ?", new String[]{id + ""});
        cursor.moveToFirst();
        return (isCursorNotEmpty(cursor)) ? buildObject(cursor) : null;
    }

    @Override
    public void delete(long id) {
        String selection = "_id = ?";
        String[] args = {String.valueOf(id)};
        database.delete(tableName, selection, args);
    }

    @Override
    public List<T> fetchAll() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
        List<T> objects = new ArrayList<T>();
        while (cursor.moveToNext()) {
            objects.add(buildObject(cursor));
        }
        return objects;
    }

    private boolean isCursorNotEmpty(Cursor cursor) {
        return (cursor != null && cursor.getCount() > 0);
    }

    protected abstract void populateValues(ContentValues values, T object);

    protected abstract T buildObject(Cursor cursor);

    protected long getLongFromColumn(String column, Cursor cursor) {
        int index = cursor.getColumnIndex(column);
        return cursor.getLong(index);
    }

    protected int getIntFromColumn(String column, Cursor cursor) {
        int index = cursor.getColumnIndex(column);
        return cursor.getInt(index);
    }

    protected String getStringFromColumn(String column, Cursor cursor) {
        int index = cursor.getColumnIndex(column);
        return cursor.getString(index);
    }
}
