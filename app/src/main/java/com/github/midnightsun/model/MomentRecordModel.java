package com.github.midnightsun.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class MomentRecordModel {
    public class MomentItem {
        public int Rid;
        public String Author;
        public String Content;
        public String ImgURI;
        public boolean IsRead;
        public String Ctime;

        public MomentItem(int rid, String author, String content, String imgURI, boolean isRead, String ctime) {
            Rid = rid;
            Author = author;
            ImgURI = imgURI;
            Content = content;
            IsRead = isRead;
            Ctime = ctime;
        }
    }

    private Context ctx;
    private MidnightDBHelper dbHelper;

    public MomentRecordModel(Context ctx, String username) {
        this.ctx = ctx;
        this.dbHelper = new MidnightDBHelper(ctx, username);
    }

    // 将朋友圈文章存入数据库
    public void addPostItem(String author, String content, String imgURI, boolean isRead) {
        String sqlseq = "INSERT INTO moment_record(author, content,  img_uri, is_read)" +
                        "VALUES(?, ?, ?, ?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int isReadI = isRead ? 1 : 0;
        db.execSQL(sqlseq, new Object[]{author, content, imgURI, isReadI});
        db.close();
    }

    // 获取未读文章数量
    public int getUnReadPostAmount() {
        String sqlsql = "SELECT COUNT(*) FROM moment_record WHERE not is_read";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlsql, null);
        cursor.moveToFirst();
        int amount = cursor.getInt(0);
        cursor.close();
        db.close();

        return amount;
    }

    // 获取 amount 数量的票圈 items
    public ArrayList<MomentItem> getMomentPosts(int amount) {
        String sqlseq = "SELECT rid, author, content, img_uri, is_read, ctime " +
                        "FROM moment_record " +
                        "ORDER BY rid DESC LIMIT ?";
        ArrayList<MomentItem> items = new ArrayList<MomentItem>(16);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlseq, new String[]{ Integer.toString(amount) });

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MomentItem item = new MomentItem(cursor.getInt(0),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4) == 0? false : true,
                    cursor.getString(5));
            items.add(item);
            cursor.moveToNext();
        }

        return items;
    }

    // 刷掉所有未读票圈文章
    public void flushUnReadPosts() {
        String sqlseq = "UPDATE moment_record SET is_read=1 WHERE not is_read";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sqlseq);
        db.close();
    }
}
