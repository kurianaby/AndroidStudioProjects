package uk.co.sparcit.trainruntimechecker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by kurianaby on 08/12/2015.
 */
public class DBOpenHelper extends SQLiteOpenHelper{
    public DBOpenHelper(Context context) {
        super(context, DBTableContract.DATABASE_NAME, null, DBTableContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBTableContract.TrainDelayRec.CREATE_TABLE);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBOpenHelper.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        db.execSQL(DBTableContract.TrainDelayRec.DELETE_TABLE);
        onCreate(db);
    }
}
