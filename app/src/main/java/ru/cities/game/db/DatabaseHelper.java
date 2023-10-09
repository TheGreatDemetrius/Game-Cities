package ru.cities.game.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import ru.cities.game.R;
import ru.cities.game.adapter.City;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "database.db";
    private static String DB_PATH;
    private final SQLiteDatabase db;
    private final Context appContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        appContext = context;
        DB_PATH = appContext.getDatabasePath(DB_NAME).getPath();
        createDatabase();
        db = getDatabase();
    }

    private void createDatabase() {
        File file = new File(DB_PATH);
        if (!file.exists())
            try (InputStream myInput = appContext.getAssets().open(DB_NAME);
                 OutputStream myOutput = Files.newOutputStream(Paths.get(DB_PATH))) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0)
                    myOutput.write(buffer, 0, length);
                myOutput.flush();
            } catch (IOException e) {
                Log.d("DatabaseHelper", "IOE", e);
            }
    }

    private SQLiteDatabase getDatabase() {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    @Nullable
    public City getCity(char firstChar, String text) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + firstChar + " WHERE name = '" + text + "'", null);
        if (!cursor.moveToFirst())
            return null;
        City city = new City(cursor.getInt(0), cursor.getString(1));
        cursor.close();
        return city;
    }

    public City getRandomCity(char lastChar) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + lastChar + " ORDER BY RANDOM() LIMIT 1", null);
        cursor.moveToFirst();
        City city = new City(cursor.getInt(0), cursor.getString(1));
        cursor.close();
        return city;
    }

    @Nullable
    public City getCityWithMistake(char firstChar, String text) {
        StringBuilder query = new StringBuilder("SELECT * FROM " + firstChar + " WHERE name = '" + text + "'");
        char[] chars, charArray = text.toCharArray();
        int length = text.length();
        for (int i = 1; i < length; i++) {
            chars = charArray.clone();
            chars[i] = '_';
            query.append(" OR name LIKE '").append(chars).append("'");
        }
        Cursor cursor = db.rawQuery(query.toString(), null);
        if (!cursor.moveToFirst())
            return null;
        City city = new City(cursor.getInt(0), cursor.getString(1));
        cursor.close();
        return city;
    }

    @Nullable
    public String getDescription(String text) {
        Cursor cursor = db.rawQuery("SELECT description FROM country WHERE name = '" + text + "'", null);
        if (!cursor.moveToFirst())
            return null;
        String description = cursor.getString(0);
        cursor.close();
        return description;
    }

    public String getCityInfo(City city) {
        int stringId = R.string.is_city;
        String cityInfo, cityName = city.getName();
        Cursor cursor = db.rawQuery("SELECT genitive FROM country WHERE id = '" + city.getId() +
                "' UNION ALL SELECT genitive FROM country WHERE capital = '" + cityName + "'", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 1) {
            stringId = R.string.is_capital;
            cursor.moveToNext();
        }
        cityInfo = appContext.getString(stringId, cityName, cursor.getString(0));
        cursor.close();
        return cityInfo;
    }
}
