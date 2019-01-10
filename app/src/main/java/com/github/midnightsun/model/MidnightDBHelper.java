package com.github.midnightsun.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MidnightDBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_PREFIX = "user_data";

    public MidnightDBHelper(Context ctx, String username) {
        super(ctx, String.format("%s_%s.db", DB_PREFIX, username), null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlseq = "CREATE TABLE IF NOT EXISTS chat_record ( " +
                            "rid integer PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            "msg_type integer NOT NULL," +
                            "msg_content varchar(128) NOT NULL," +
                            "is_read integer NOT NULL," +
                            "ctime timestamp NOT NULL DEFAULT (datetime('now','localtime')) " +
                        ")";
        db.execSQL(sqlseq);

        sqlseq = "CREATE TABLE IF NOT EXISTS news_record (" +
                    "rid integer PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title varchar(32) NOT NULL," +
                    "is_read integer NOT NULL," +
                    "ctime timestamp NOT NULL DEFAULT (datetime('now','localtime'))," +
                    "content text NOT NULL" +
                 ")";
        db.execSQL(sqlseq);

        sqlseq = "CREATE TABLE IF NOT EXISTS moment_record (" +
                    "rid integer PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "author varchar(32) NOT NULL," +
                    "content varchar(18) NOT NULL," +
                    "is_read integer NOT NULL," +
                    "ctime timestamp NOT NULL DEFAULT (datetime('now','localtime'))," +
                    "img_uri varchar(64) NOT NULL " +
                 ")";
        db.execSQL(sqlseq);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sqlseq = "DROP TABLE IF EXISTS chat_record";
        db.execSQL(sqlseq);
        sqlseq = "DROP TABLE IF EXISTS news_record";
        db.execSQL(sqlseq);
        sqlseq = "DROP TABLE IF EXISTS moment_record";
        db.execSQL(sqlseq);

        onCreate(db);
    }
}
