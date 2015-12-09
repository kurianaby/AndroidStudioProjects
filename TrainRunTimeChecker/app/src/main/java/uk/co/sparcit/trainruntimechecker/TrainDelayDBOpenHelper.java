package uk.co.sparcit.trainruntimechecker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kurianaby on 08/12/2015.
 */
public class TrainDelayDBOpenHelper extends SQLiteOpenHelper{
    public TrainDelayDBOpenHelper(Context context) {
        super(context, TrainDelayDBTableContract.DATABASE_NAME, null, TrainDelayDBTableContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TrainDelayDBTableContract.TrainDelayRec.CREATE_TABLE);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TrainDelayDBTableContract.TrainDelayRec.DELETE_TABLE);
        onCreate(db);
    }
}
