package com.example.yuetung55.adventurebook;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Yuetung55 on 21/12/2017.
 */

class StoryPaths {
    private String optionText;
    private int pageNumber;
    private Resource resourceNeeded;
    private int amountNeeded;

    public StoryPaths(String optionText, int pageNumber, Resource resourceNeeded, int amountNeeded) {
        this.optionText = optionText;
        this.pageNumber = pageNumber;
        this.resourceNeeded = resourceNeeded;
        this.amountNeeded = amountNeeded;
    }

    public String getOptionText() {
        return optionText;
    }

    public int getNextPage() {
        return pageNumber;
    }

    public Resource getResourceNeeded() {
        return resourceNeeded;
    }

    public int getAmountNeeded() {
        return amountNeeded;
    }
}
class StoryNode {
    private String text;
    private int pageNumber;
    private Resource resourceGained;
    private int amountGained;
    private ArrayList<StoryPaths> storyPaths;
    public StoryNode(int pageNumber, Resource resourceGained, int amountGained, String text, ArrayList<StoryPaths> storyPaths ) {
        this.text=text;
        this.pageNumber=pageNumber;
        this.resourceGained=resourceGained;
        this.amountGained=amountGained;
        this.storyPaths=storyPaths;
    }

    public String getText() {
        return text;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public Resource getResourceGained() {
        return resourceGained;
    }

    public int getAmountGained() {
        return amountGained;
    }

    public ArrayList<StoryPaths> getStoryPaths() {
        return storyPaths;
    }
}

/* Retrieve data from database/ create database from storybook.txt. Use getStory(int pageNumber) to get corresponding story */
public class StoryManager {
    private static StoryManager instance=null;
    private StoryDbHelper storyDbHelper;
    private SQLiteDatabase storyDb;
    private Context context;

    /* Constructor: create table using information from SQLiteDatabase (if exists), otherwise from storybook.txt*/
    private StoryManager(Context context) {
        this.context=context;
        storyDbHelper=new StoryDbHelper(context);
        storyDb=storyDbHelper.getWritableDatabase();
        if (!tableStoryExists(StoryTable.StoryEntry.TABLE_NAME)) {
            //System.out.println("creating new story table");    //debug
            // Get initialization info from assets->storybook.txt (modify this file to change current story or add new stories)
            initializeStories();
        }
    }

    /* Singleton implementation */
    public static StoryManager getInstance(Context context) {
        if (instance==null) {
            // System.out.println("no instance, creating new instance");      //debug
            instance=new StoryManager(context);
        }
        return instance;
    }

    /* parse storybook.txt, add to StoryTable */
    private void initializeStories() {
        String fileName="storybook.txt";
        AssetManager assetManager=context.getAssets();
        String pageNumber="0";
        String text="";
        String resourceGained="none";
        String amountGained="0";
        String[] options={"none","none","none","none","none"};
        int optionCount=0;
        try {
            InputStream input = assetManager.open(fileName);
            Scanner scanner=new Scanner(input);
            scanner.nextLine(); scanner.nextLine();           //skips 1st & 2nd line
            ContentValues cv = new ContentValues();
            while (scanner.hasNextLine()) {
                String line=scanner.nextLine();
                if (line.charAt(0)=='@') {     // is header
                    String[] parts = line.trim().split(" ");
                    pageNumber=parts[1];
                    resourceGained=parts[2];
                    if (!resourceGained.equals("none")) {
                        amountGained=parts[3];
                    } else {
                        amountGained="0";
                    }
                }
                else if (line.charAt(0)=='#') {    // is option
                    options[optionCount]=line.substring(2,line.length());
                    if (optionCount<4) optionCount++;
                }
                else if (line.charAt(0)=='~') {   // is end
                    cv.put(StoryTable.StoryEntry.COL_TEXT, text);
                    cv.put(StoryTable.StoryEntry.COL_PAGE_NUMBER, pageNumber);
                    cv.put(StoryTable.StoryEntry.COL_RESOURCE_GAINED, resourceGained);
                    cv.put(StoryTable.StoryEntry.COL_AMOUNT_GAINED, amountGained);
                    cv.put(StoryTable.StoryEntry.COL_OPTION1, options[0]);
                    cv.put(StoryTable.StoryEntry.COL_OPTION2, options[1]);
                    cv.put(StoryTable.StoryEntry.COL_OPTION3, options[2]);
                    cv.put(StoryTable.StoryEntry.COL_OPTION4, options[3]);
                    cv.put(StoryTable.StoryEntry.COL_OPTION5, options[4]);
                    storyDb.insert(StoryTable.StoryEntry.TABLE_NAME, null, cv);
                    /*System.out.println("inserted"+text);   //debug
                    System.out.println("inserted"+pageNumber);
                    System.out.println("inserted"+resourceGained);
                    System.out.println("inserted"+amountGained);
                    System.out.println("inserted"+options[0]);
                    System.out.println("inserted"+options[1]);
                    System.out.println("inserted"+options[2]);
                    System.out.println("inserted"+options[3]);
                    System.out.println("inserted"+options[4]);*/
                    pageNumber="0";
                    text="";
                    resourceGained="none";
                    amountGained="0";
                    for (int i=0; i<options.length; i++) {
                        options[i] = "none";
                    }
                    optionCount=0;
                }
                else {    // is text
                    text+="\n\n"+line;
                }
            }
        } catch (IOException ex) {
            System.out.println(fileName+"not found");
        }

    }

    /* main function of StoryManager, return a StoryNode from Db based on pageNumber requested. return null if page does not exists */
    public StoryNode getStory(int pageNumber) {
        final String SQL_QUERY_TABLE="select * from " + StoryTable.StoryEntry.TABLE_NAME + " where "+
                StoryTable.StoryEntry.COL_PAGE_NUMBER + " = '" + pageNumber + "'";
        Cursor cursor = storyDb.rawQuery(SQL_QUERY_TABLE, null);
        int indexText = cursor.getColumnIndex(StoryTable.StoryEntry.COL_TEXT);
        int indexResourceGained = cursor.getColumnIndex(StoryTable.StoryEntry.COL_RESOURCE_GAINED);
        int indexAmountGained = cursor.getColumnIndex(StoryTable.StoryEntry.COL_AMOUNT_GAINED);
        int indexOption1 = cursor.getColumnIndex(StoryTable.StoryEntry.COL_OPTION1);
        int indexOption2 = cursor.getColumnIndex(StoryTable.StoryEntry.COL_OPTION2);
        int indexOption3 = cursor.getColumnIndex(StoryTable.StoryEntry.COL_OPTION3);
        int indexOption4 = cursor.getColumnIndex(StoryTable.StoryEntry.COL_OPTION4);
        int indexOption5 = cursor.getColumnIndex(StoryTable.StoryEntry.COL_OPTION5);
        try {
            cursor.moveToFirst();
            String text=cursor.getString(indexText);
            //System.out.println("text is: "+text);    //debug
            Resource resourceGained=null;
            int amountGained=0;
            String resourceGainedtxt=cursor.getString(indexResourceGained);
            if (!resourceGainedtxt.equals("none")) {
                resourceGained=ResourceManager.getInstance(context).getResource(resourceGainedtxt);
                amountGained=Integer.parseInt(cursor.getString(indexAmountGained));
            }
            //System.out.println("Resource Gained is : "+resourceGainedtxt);    //debug
            //System.out.println("amount Gained is: "+amountGained);    //debug
            ArrayList<StoryPaths> storyPaths=new ArrayList<>();
            String[] optionTexts=new String[5];
            optionTexts[0]=cursor.getString(indexOption1);
            optionTexts[1]=cursor.getString(indexOption2);
            optionTexts[2]=cursor.getString(indexOption3);
            optionTexts[3]=cursor.getString(indexOption4);
            optionTexts[4]=cursor.getString(indexOption5);
            for (int i=0; i<optionTexts.length; i++) {
                if (optionTexts[i].equals("none")) continue;
                String[] parts = optionTexts[i].split("#");
                //System.out.println("optionText is: "+parts[0]);    //debug
                String optionText=parts[0];
                //System.out.println("next Page is: "+parts[1]);    //debug
                int nextPage=Integer.parseInt(parts[1].trim());
                Resource resourceNeeded=null;
                int amountNeeded=0;
                String resourceNeededTxt=parts[2].trim();
                //System.out.println("resourceNeeded is: "+resourceNeededTxt);    //debug
                if (!resourceNeededTxt.equals("none")){
                    resourceNeeded=ResourceManager.getInstance(context).getResource(resourceNeededTxt);
                    //System.out.println("amountNeeded is: "+parts[3]);    //debug
                    amountNeeded=Integer.parseInt(parts[3].trim());
                }
                storyPaths.add(new StoryPaths(optionText,nextPage,resourceNeeded,amountNeeded));
            }
            cursor.close();
            return new StoryNode(pageNumber,resourceGained,amountGained,text,storyPaths);
        } catch (NumberFormatException ex) {
            System.out.println("corrupted story database");
        }
        return null;
    }

    /* reset all stories, when calling from outside, remember to refresh the StoryManager as well */
    public void resetAllStories() {
        storyDb.delete(StoryTable.StoryEntry.TABLE_NAME, null, null);
        initializeStories();
        instance=new StoryManager(context);
    }

    /* Close Db */
    public void close() {
        storyDb.close();
        instance=null;
    }

    /* method to check if Db exists*/
    private boolean tableStoryExists(String tableName) {
        //System.out.println("table name is:"+tableName );    //debug
        Cursor cursor = storyDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        //System.out.println("inside check table exists");    //debug
        if(cursor!=null) {
            //System.out.println("cursor is"+cursor.getCount());    //debug
            if(cursor.getCount()>0) {
                //System.out.println("cursor count>0");    //debug
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

}
class StoryDbHelper extends SQLiteOpenHelper {
    private final Context context;
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase;

    StoryDbHelper(Context context) {
        super(context, StoryTable.StoryEntry.TABLE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                +StoryTable.StoryEntry.TABLE_NAME + "("
                +StoryTable.StoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +StoryTable.StoryEntry.COL_TEXT + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_PAGE_NUMBER + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_RESOURCE_GAINED + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_AMOUNT_GAINED + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_OPTION1 + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_OPTION2 + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_OPTION3 + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_OPTION4 + " TEXT NOT NULL, "
                +StoryTable.StoryEntry.COL_OPTION5 + " TEXT NOT NULL );";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS "+StoryTable.StoryEntry.TABLE_NAME;
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
class StoryTable {

    public static final class StoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "StoryTableTest";
        public static final String COL_TEXT = "text";
        public static final String COL_PAGE_NUMBER = "pageNumber";
        public static final String COL_RESOURCE_GAINED = "resourceGained";
        public static final String COL_AMOUNT_GAINED = "amountGained";
        public static final String COL_OPTION1 = "option1";
        public static final String COL_OPTION2 = "option2";
        public static final String COL_OPTION3 = "option3";
        public static final String COL_OPTION4 = "option4";
        public static final String COL_OPTION5 = "option5";
    }
}

