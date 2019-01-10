package com.github.midnightsun.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class NewsRecordModel {
    public class NewsItem {
        public int Rid;
        public String Title;
        public String Content;
        public boolean IsRead;
        public String Ctime;

        public NewsItem(int rid, String title, String content, boolean isRead, String ctime) {
            Rid = rid;
            Title = title;
            Content = content;
            IsRead = isRead;
            Ctime = ctime;
        }
    }

    private Context ctx;
    private MidnightDBHelper dbHelper;

    public NewsRecordModel(Context ctx, String username) {
        this.ctx = ctx;
        this.dbHelper = new MidnightDBHelper(ctx, username);
    }

    // 添加新闻
    public void addNews(String title, String content, boolean isRead) {
        String sqlseq = "INSERT INTO news_record(title, content, is_read)" +
                        "VALUES(?, ?, ?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int isReadI = isRead ? 1 : 0;
        db.execSQL(sqlseq, new Object[]{title, content, isReadI});
        db.close();
    }

    // 获取未读新闻数量
    public int getUnReadNewsAmount() {
        String sqlsql = "SELECT COUNT(*) FROM news_record WHERE not  is_read";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlsql, null);
        cursor.moveToFirst();
        int amount = cursor.getInt(0);
        cursor.close();
        db.close();

        return amount;
    }

    // 获取最后一条新闻 item
    public NewsItem getLastNewsItem() {
        String sqlseq = "SELECT rid, title, content, is_read, ctime " +
                        "FROM news_record " +
                        "ORDER BY rid DESC LIMIT 1";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlseq, null);
        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();
        NewsItem item = new NewsItem(cursor.getInt(0),
                cursor.getString(1), cursor.getString(2),
                cursor.getInt(3) == 0? false : true, cursor.getString(4));

        cursor.close();
        db.close();

        return item;
    }

    // 获取 amount 数量的新闻 items
    public ArrayList<NewsItem> getNewsItems(int amount) {
        String sqlseq = "SELECT rid, title, content, is_read, ctime " +
                        "FROM news_record " +
                        "ORDER BY rid DESC LIMIT ?";
        ArrayList<NewsItem> items = new ArrayList<NewsItem>(16);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlseq, new String[]{ Integer.toString(amount) });

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            NewsItem item = new NewsItem(cursor.getInt(0),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getInt(3) == 0? false : true, cursor.getString(4));
            items.add(item);
            cursor.moveToNext();
        }

        return items;
    }

    // 刷掉所有未读新闻
    public void flushUnReadNews() {
        String sqlseq = "UPDATE news_record SET is_read=1 WHERE not is_read";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sqlseq);
        db.close();
    }
}
