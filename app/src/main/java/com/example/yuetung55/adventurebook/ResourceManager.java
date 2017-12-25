package com.example.yuetung55.adventurebook;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Yuetung55 on 22/12/2017.
 */
class Resource {
    String name;
    private boolean depletable;
    private int stock;
    private int maxStock;
    public Resource(String name, boolean depletable, int stock, int maxStock) {
        this.name = name;
        this.depletable=depletable;
        this.stock=stock;
        this.maxStock=maxStock;
    }
    public int getStock() {
        return stock;
    }
    /* increases stock, capped at maxAmount */
    public void increaseStock(int amount) {
        if (stock+amount>=maxStock) {
            stock=maxStock;
        } else stock+=amount;
    }
    /* return true if decrease is successful, reduces stock if good is depletable*/
    public boolean decreaseStock(int amount) {
        if (stock-amount>=0) {
            if (depletable) {stock -= amount;}
            return true;
        }
        return false;
    }
    public void setStock(int amount) {
        if (amount>=maxStock) {
            stock=maxStock;
        } else {
            stock = amount;
        }
    }
    public boolean getDepletable() {
        return depletable;
    }
    public String getName() {
        return name;
    }
    public int getMaxStock() {
        return maxStock;
    }
}

public class ResourceManager {

    private HashMap<String,Resource> resources=new HashMap<>();
    private static ResourceManager instance=null;
    private ResourceDbHelper resourceDbHelper;
    private SQLiteDatabase resourceDb;
    private Context context;

    /* Constructor: create table using information from SQLiteDatabase (if exists), otherwise from resourceInitialize.txt*/
    private ResourceManager(Context context) {
        this.context=context;
        resourceDbHelper=new ResourceDbHelper(context);
        resourceDb=resourceDbHelper.getWritableDatabase();
        retrieveResourcesFromDb();
    }
    /* reset all resources to default value*/
    private void initializeResources() {
        AssetManager assetManager=context.getAssets();
        try {
            InputStream input = assetManager.open("resources.txt");
            Scanner scanner=new Scanner(input);
            scanner.nextLine();           //skips 1st line
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().trim().split(" ");
                //System.out.println(Arrays.toString(parts));   //debug
                ContentValues cv = new ContentValues();
                cv.put(ResourceTable.ResourceEntry.COL_NAME, parts[0]);
                cv.put(ResourceTable.ResourceEntry.COL_DEPLETABLE, parts[1]);
                cv.put(ResourceTable.ResourceEntry.COL_STOCK, parts[2]);
                cv.put(ResourceTable.ResourceEntry.COL_MAX_STOCK, parts[3]);
                resourceDb.insert(ResourceTable.ResourceEntry.TABLE_NAME, null, cv);
            }
        } catch (IOException ex) {
            System.out.println("File not found");
        }
    }
    /* fill the resources Hashmap with Resource objects retrieved from Db*/
    private void retrieveResourcesFromDb() {
        final String SQL_QUERY_TABLE="SELECT * FROM "+ResourceTable.ResourceEntry.TABLE_NAME;
        Cursor cursor = resourceDb.rawQuery(SQL_QUERY_TABLE, null);
        int indexName = cursor.getColumnIndex(ResourceTable.ResourceEntry.COL_NAME);
        int indexDepletable = cursor.getColumnIndex(ResourceTable.ResourceEntry.COL_DEPLETABLE);
        int indexStock = cursor.getColumnIndex(ResourceTable.ResourceEntry.COL_STOCK);
        int indexMaxStock = cursor.getColumnIndex(ResourceTable.ResourceEntry.COL_MAX_STOCK);
        while(cursor.moveToNext()){
            try {
                String name=cursor.getString(indexName);
                //System.out.println(name);    //debug
                boolean depletable=Boolean.parseBoolean(cursor.getString(indexDepletable));
                //System.out.println(depletable);    //debug
                int stock=Integer.parseInt(cursor.getString(indexStock));
                //System.out.println(stock);    //debug
                int maxStock=Integer.parseInt(cursor.getString(indexMaxStock));
                //System.out.println(maxStock);    //debug
                //System.out.println("creating "+name+" resource");    //debug
                resources.put(name,new Resource(name,depletable,stock,maxStock));
            } catch (NumberFormatException ex) {
                System.out.println("corrupted database / text file");
            } catch (CursorIndexOutOfBoundsException ex) {
                System.out.println("failed to retrieve existing resource information, initializing new resources");
                initializeResources();
                retrieveResourcesFromDb();
            }
        }
        cursor.close();
    }
    /* update database for a particular resource */
    public void updateDatabase(Resource resource) {
        ContentValues cv = new ContentValues();
        cv.put(ResourceTable.ResourceEntry.COL_NAME, resource.getName());
        cv.put(ResourceTable.ResourceEntry.COL_DEPLETABLE, resource.getDepletable());
        cv.put(ResourceTable.ResourceEntry.COL_STOCK, resource.getStock());
        cv.put(ResourceTable.ResourceEntry.COL_MAX_STOCK, resource.getMaxStock());
        resourceDb.update(ResourceTable.ResourceEntry.TABLE_NAME, cv, ResourceTable.ResourceEntry.COL_NAME+"="+"'"+resource.getName()+"'", null);
    }
    /* update database for an array of resources */
    public void updateDatabase(Resource[] resourceName) {
        for (int i=0; i<resourceName.length; i++) {
            updateDatabase(resourceName[i]);
        }
    }
    /* update all resources */
    public void updateDatabase() {
        for (String resourceName:resources.keySet()) {
            Resource resource=resources.get(resourceName);
            updateDatabase(resource);
        }
    }

    /* get resources as a Hashmap*/
    public HashMap<String,Resource> getResources() {
        return resources;
    }

    /* */
    public Resource getResource(String resourceName) {
        return resources.get(resourceName);
    }

    /* Singleton implementation */
    public static ResourceManager getInstance(Context context) {
        if (instance==null) {
            instance=new ResourceManager(context);
        }
        return instance;
    }
    /* reset all resources, when calling from outside, remember to refresh the resourceManager as well */
    public void resetAllResources() {
        resourceDb.delete(ResourceTable.ResourceEntry.TABLE_NAME, null, null);
        resources.clear();
        initializeResources();
        instance=new ResourceManager(context);
    }
    /* return true if resource has sufficient stock, false otherwise */
    public boolean checkResourceStock(String resourceName, int amount) {
        Resource resource=resources.get(resourceName);
        return resource.getStock()-amount>=0;
    }
    /* return true if all resources has sufficient stock, false otherwise */
    public boolean checkResourcesStock(String[] resourceName, int amount[]) {
        for (int i=0; i<resourceName.length; i++) {
            Resource resource=resources.get(resourceName[i]);
            if (resource.getStock()-amount[i]<0) return false;
        }
        return true;
    }
    /* depletes the resource if it's depletable, return true of depletion is successful */
    public boolean decreaseResourceStock(String resourceName, int amount) {
        Resource resource=resources.get(resourceName);
        return resource.decreaseStock(amount);
    }
    /* decreaseResourceStock for multiple resources */
    public boolean decreaseResourcesStock(String[] resourceName, int[] amount) {
        //check if all has sufficient resources;
        if (!checkResourcesStock(resourceName,amount)) return false;
        for (int i=0; i<resourceName.length; i++) {
            Resource resource=resources.get(resourceName[i]);
            resource.decreaseStock(amount[i]);
        }
        return true;
    }
    /* increases stock, capped at maxAmount */
    public void increaseResourceStock(String resourceName, int amount) {
        Resource resource=resources.get(resourceName);
        resource.increaseStock(amount);
    }
    /* set stock, capped at maxAmount */
    public void setResourceStock(String resourceName, int amount) {
        Resource resource=resources.get(resourceName);
        resource.setStock(amount);
    }
    public int getResourceStock(String resourceName) {
        Resource resource=resources.get(resourceName);
        return resource.getStock();
    }
    public void close() {
        instance=null;
        resourceDb.close();
    }
}
class ResourceDbHelper extends SQLiteOpenHelper {
    private final Context context;
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase;

    ResourceDbHelper(Context context) {
        super(context, ResourceTable.ResourceEntry.TABLE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Make this string:
        //CREATE TABLE ResourceTable ( _ID INTEGER PRIMARY KEY AUTOINCREMENT,
        //Amount TEXT NOT NULL,
        //Remarks TEXT NOT NULL );
        final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                +ResourceTable.ResourceEntry.TABLE_NAME + "("
                +ResourceTable.ResourceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +ResourceTable.ResourceEntry.COL_NAME + " TEXT NOT NULL, "
                +ResourceTable.ResourceEntry.COL_DEPLETABLE + " TEXT NOT NULL, "
                +ResourceTable.ResourceEntry.COL_STOCK + " TEXT NOT NULL, "
                +ResourceTable.ResourceEntry.COL_MAX_STOCK + " TEXT NOT NULL );";

        //Execute the SQL command
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Make this string:
        //DROP TABLE IF EXISTS ResourceTable
        final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS "+ResourceTable.ResourceEntry.TABLE_NAME;
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
class ResourceTable {

    public static final class ResourceEntry implements BaseColumns {
        public static final String TABLE_NAME = "ResourceTableTest";
        public static final String COL_NAME = "name";
        public static final String COL_DEPLETABLE = "depletable";
        public static final String COL_STOCK = "stock";
        public static final String COL_MAX_STOCK = "maxStock";

    }
}