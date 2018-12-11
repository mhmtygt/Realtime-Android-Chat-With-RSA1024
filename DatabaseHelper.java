package com.yigit.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLClientInfoException;

/**
 * Created by Mahmut on 26.06.2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="Users.db";
    public static final String TABLE_NAME="users_table";
    public static final String COL1="ID";
    public static final String COL2="UID";
    public static final String COL3="NAME";
    public static final String COL4="EMAIL";
    public static final String COL5="PRIVATE";




    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" create table " + TABLE_NAME+ " (ID INTEGER PRIMARY  KEY AUTOINCREMENT,UID TEXT,NAME TEXT ,EMAIL TEXT ,PRIVATE TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME );
        onCreate(db);
    }
    public boolean insertData(String uid, String name, String email, String privateKey){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(COL2,uid);
        contentValues.put(COL3,name);
        contentValues.put(COL4,email);
        contentValues.put(COL5,privateKey);

        long result =db.insert(TABLE_NAME,null,contentValues);
        if(result==-1)
            return false;
        else
            return true;
    }

    public Cursor getAllData(){

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from "+TABLE_NAME,null);

        return res;
    }
}
