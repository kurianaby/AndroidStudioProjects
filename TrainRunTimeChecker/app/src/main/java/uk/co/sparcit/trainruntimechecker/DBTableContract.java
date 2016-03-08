package uk.co.sparcit.trainruntimechecker;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kurianaby on 08/12/2015.
 *
 * This class represents a contract or a schema
 * for a TrainDelay table containing
 * train delay records
 *
 * Refer to -   http://stackoverflow.com/questions/17451931/how-to-use-a-contract-class-in-android
 *              http://developer.android.com/training/basics/data-storage/databases.html
 *              http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public final class DBTableContract {


    public static final String AUTHORITY = "uk.co.sparcit.trainruntimechecker";
    public static final String SCHEME = "content://";
    public static final String SLASH = "/";
    public static final String DATABASE_NAME = "TrainDelays.db";
    public static final  int    DATABASE_VERSION   = 1;

    // To prevent someone from accidentally instantiating the contract class,
    // give it a private constructor.
    private DBTableContract() {}

    public static final class TrainDelayRec implements BaseColumns {

        /* Do not allow this class to be instantiated */
        private TrainDelayRec() {
        }

        public static final String TABLE_NAME = "TrainDelays";

        //Field names in the table
        public static final String Fld_GeneratedAt = "GeneratedAt";
        public static final String Fld_To = "To";
        public static final String Fld_From = "From";
        public static final String Fld_Scehduled = "Scheduled";
        public static final String Fld_Expected = "Expected";
        public static final String Fld_Cancelled = "Cancelled";



        /*
         * URI definitions
         */

        /**
         * The content style URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        /**
         * The content URI base for a single row. An ID must be appended.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = Fld_To + " ASC";

        //TODO Figure out whether the MIME type stuff below is of use
        //TODO IF so modify it ot fit, f not delete it
        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing rows
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/vnd.uk.co.sparcit.trainruntimechecker.TrainDelays";

        /**
         * The MIME type of a {@link #CONTENT_URI} single row
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/vnd.uk.co.sparcit.trainruntimechecker.TrainDelays";


        // used for the UriMatcher
        private static final int DELAYS = 10;
        private static final int DELAYS_ID = 20;

        public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        static {
            sURIMatcher.addURI(AUTHORITY, TABLE_NAME, DELAYS);
            sURIMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", DELAYS_ID);
        }

        /**
         * SQL Statement to create the TrainDelays table
         */
        public static final String CREATE_TABLE = "CREATE TABLE if not exists" + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + Fld_To + " TEXT,"
                + Fld_From + " TEXT,"
                + Fld_Scehduled + " TEXT,"
                + Fld_Expected + " TEXT,"
                + Fld_Cancelled + " TEXT,"
                + Fld_GeneratedAt + " TEXT"
                + ");";

        /**
         * SQL statement to delete the table
         */
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Array of all the Fields. Makes for cleaner code
         */
        public static final String[] FLD_ARRAY = {
                Fld_To,
                Fld_From,
                Fld_Scehduled,
                Fld_Expected,
                Fld_Cancelled,
                Fld_GeneratedAt,
        };
    }
}
