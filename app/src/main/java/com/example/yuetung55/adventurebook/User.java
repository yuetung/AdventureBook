package com.example.yuetung55.adventurebook;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Yuetung55 on 21/12/2017.
 */

public class User {

    private boolean noExistingInfo=false;
    private int currentStoryNode;
    private static User user;
    private UserDbHelper userDbHelper;
    private SQLiteDatabase userDb;
    private Context context;

    /* constructor */
    private User (Context context){
        this.context=context;
        userDbHelper=new UserDbHelper(context);
        userDb=userDbHelper.getWritableDatabase();
        retrieveUserInfo();
    }

    /* Singleton implementation */
    public static User getUser(Context context) {
        if (user==null) {
            // System.out.println("no instance, creating new instance");      //debug
            user=new User(context);
        }
        return user;
    }

    /* getters and setters */
    public int getCurrentStoryNode() {
        return currentStoryNode;
    }

    public void setCurrentStoryNode(int currentStoryNode) {
        this.currentStoryNode = currentStoryNode;

    }
    /* reset all user information to default value*/
    private void initializeUserInfo() {
        ContentValues cv = new ContentValues();
        cv.put(UserTable.UserEntry.COL_CURRENT_STORY_NODE, 0);
        userDb.insert(UserTable.UserEntry.TABLE_NAME, null, cv);
    }

    /* Retrieve User info from Db */
    private void retrieveUserInfo() {
        final String SQL_QUERY_TABLE="SELECT * FROM "+UserTable.UserEntry.TABLE_NAME;
        Cursor cursor = userDb.rawQuery(SQL_QUERY_TABLE, null);
        int indexCurrentStoryNode = cursor.getColumnIndex(UserTable.UserEntry.COL_CURRENT_STORY_NODE);
        try {
            cursor.moveToFirst();
            currentStoryNode=Integer.parseInt(cursor.getString(indexCurrentStoryNode));
            //System.out.println(currentStoryNode);    //debug
        } catch (NumberFormatException ex) {
            System.out.println("corrupted database / text file");
        } catch (CursorIndexOutOfBoundsException ex) {
            System.out.println("failed to retrieve existing user information, initializing new user information");
            noExistingInfo=true;
            initializeUserInfo();
            retrieveUserInfo();
        }
        cursor.close();
    }

    /* Check if user is first time */
    public boolean noExistingInfo() {
        return noExistingInfo;
    }

    /* Check if user is first time */
    public void setExistingInfo(boolean b) {
        noExistingInfo=b;
    }

    /* reset all user information, when calling from outside, remember to refresh the User as well */
    public void resetUserInfo() {
        userDb.delete(UserTable.UserEntry.TABLE_NAME, null, null);
        initializeUserInfo();
        currentStoryNode=0;
        user=new User(context);
    }

    /* update database for user */
    public void updateDatabase() {
        ContentValues cv = new ContentValues();
        cv.put(UserTable.UserEntry.COL_CURRENT_STORY_NODE, currentStoryNode);
        userDb.update(UserTable.UserEntry.TABLE_NAME, cv, null,null);
    }
}
class UserDbHelper extends SQLiteOpenHelper {
    private final Context context;
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase;

    UserDbHelper(Context context) {
        super(context, UserTable.UserEntry.TABLE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                +UserTable.UserEntry.TABLE_NAME + "("
                +UserTable.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +UserTable.UserEntry.COL_CURRENT_STORY_NODE + " TEXT NOT NULL );";

        //Execute the SQL command
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS "+UserTable.UserEntry.TABLE_NAME;
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
class UserTable {

    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "UserTableTest9";
        public static final String COL_CURRENT_STORY_NODE = "currentStoryNode";

    }
}