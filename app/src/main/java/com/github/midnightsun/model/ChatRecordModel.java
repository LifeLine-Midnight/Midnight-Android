package com.github.midnightsun.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class ChatRecordModel {
    public static final int MSG_OTHER = 0;
    public static final int MSG_SELF = 1;
    public static final int MSG_ONLINE = 2;
    public static final int MSG_OFFLINE = 3;
    public static final int MSG_TIME = 4;

    public class ChattingItem {
        public int Rid;
        public int MsgType;
        public String MsgContent;
        public boolean IsRead;
        public String Ctime;

        public ChattingItem(int rid, int msgType, String msgContent, boolean isRead, String ctime) {
            Rid = rid;
            MsgType = msgType;
            MsgContent = msgContent;
            IsRead = isRead;
            Ctime = ctime;
        }
    }

    private Context ctx;
    private MidnightDBHelper dbHelper;

    public ChatRecordModel(Context ctx, String username) {
        this.ctx = ctx;
        this.dbHelper = new MidnightDBHelper(ctx, username);
    }

    // 添加聊天过程中的 item
    public void addChattingItem(int msgType, String content, boolean isRead) {
        String sqlseq = "INSERT INTO chat_record(msg_type, msg_content, is_read) " +
                        "VALUES(?, ?, ?)";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int isReadI = isRead ? 1 : 0;
        db.execSQL(sqlseq, new Object[]{msgType, content, isReadI});
        db.close();
    }

    // 获取未读聊天的数量
    public int getUnReadMsgAmount() {
        String sqlsql = "SELECT COUNT(*) FROM chat_record WHERE msg_type IN(?, ?) and not is_read";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlsql,
                new String[]{ Integer.toString(MSG_SELF), Integer.toString(MSG_OTHER)});
        cursor.moveToFirst();
        int amount = cursor.getInt(0);
        cursor.close();
        db.close();

        return amount;
    }

    // 获取最后一条聊天信息
    public ChattingItem getLastChattingItem() {
        String sqlseq = "SELECT rid, msg_type, msg_content, is_read, ctime " +
                        "FROM chat_record " +
                        "WHERE msg_type IN(?, ?) ORDER BY rid DESC LIMIT 1";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlseq,
                new String[]{ Integer.toString(MSG_SELF), Integer.toString(MSG_OTHER)});
        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();
        ChattingItem item = new ChattingItem(cursor.getInt(0),
                cursor.getInt(1), cursor.getString(2),
                cursor.getInt(3) == 0? false : true, cursor.getString(4));

        cursor.close();
        db.close();

        return item;
    }

    // 获取 amount 数量的聊天 items
    public ArrayList<ChattingItem> getChattingItems(int amount) {
        String sqlseq = "SELECT rid, msg_type, msg_content, is_read, ctime " +
                        "FROM chat_record " +
                        "ORDER BY rid DESC LIMIT ?";
        ArrayList<ChattingItem> items = new ArrayList<ChattingItem>(50);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlseq, new String[]{ Integer.toString(amount) });

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChattingItem item = new ChattingItem(cursor.getInt(0),
                    cursor.getInt(1), cursor.getString(2),
                    cursor.getInt(3) == 0? false : true, cursor.getString(4));
            items.add(item);
            cursor.moveToNext();
        }

        return items;
    }

    // 刷掉所有未读消息
    public void flushUnReadMsg() {
        String sqlseq = "UPDATE chat_record SET is_read=1 WHERE not is_read";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sqlseq);
        db.close();
    }
}
