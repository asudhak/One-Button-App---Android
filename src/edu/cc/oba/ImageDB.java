package edu.cc.oba;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ImageDB extends ContentProvider {

	public static final Uri CONTENT_URI = Uri
			.parse("content://edu.cc.oba/images");

	// Create the constants used to differentiate between the different URI
	// requests.
	private static final int IMAGES = 1;
	private static final int IMAGE_ID = 2;

	private static final UriMatcher uriMatcher;
	// Allocate the UriMatcher object, where a URI ending in 'alerts' will
	// correspond to a request for all alerts, and 'alerts' with a trailing
	// '/[rowID]' will represent a single alert row.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("edu.cc.oba", "images", IMAGES);
		uriMatcher.addURI("edu.cc.oba", "images/#", IMAGE_ID);
	}

	public SQLiteDatabase imageDB;
	
	@Override
	public boolean onCreate() {
		Context context = getContext();

		alertsDatabaseHelper dbHelper;
		dbHelper = new alertsDatabaseHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
		imageDB = dbHelper.getWritableDatabase();
		return (imageDB == null) ? false : true;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case IMAGES:
			return "vnd.android.cursor.dir/vnd.cc.image";
		case IMAGE_ID:
			return "vnd.android.cursor.item/vnd.cc.image";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(IMAGES_TABLE);

		// If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) {
		case IMAGE_ID:
			qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			break;
		}

		// If no sort order is specified sort by date / time
		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = KEY_IMAGENAME;
		} else {
			orderBy = sort;
		}

		// Apply the query to the underlying database.
		Cursor c = qb.query(imageDB, projection, selection, selectionArgs, null,
				null, orderBy);

		// Register the contexts ContentResolver to be notified if
		// the cursor result set changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);

		// Return a cursor to the query result.
		return c;
	}

	@Override
	public Uri insert(Uri _uri, ContentValues _initialValues) {
		// Insert the new row, will return the row number if successful.
		long rowID = imageDB.insert(IMAGES_TABLE, "image", _initialValues);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		throw new SQLException("Failed to insert row into " + _uri);
	}

	public static void simplydelete(int id, SQLiteDatabase locDB) {
		locDB.execSQL("DELETE FROM LOCS_TABLE where _id = " + id);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;

		switch (uriMatcher.match(uri)) {
		case IMAGES:
			count = imageDB.delete(IMAGES_TABLE, where, whereArgs);
			break;

		case IMAGE_ID:
			String segment = uri.getPathSegments().get(1);
			count = imageDB.delete(IMAGES_TABLE,
					KEY_ID
							+ "="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case IMAGES:
			count = imageDB.update(IMAGES_TABLE, values, where, whereArgs);
			break;

		case IMAGE_ID:
			String segment = uri.getPathSegments().get(1);
			count = imageDB.update(IMAGES_TABLE, values,
					KEY_ID
							+ "="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/** The underlying database */

	private static final String TAG = "ImageDB";
	private static final String DATABASE_NAME = "images.db";
	private static final int DATABASE_VERSION = 1;
	private static final String IMAGES_TABLE = "images";

	// Column Names
	public static final String KEY_ID = "_id";
	public static final String KEY_IMAGENAME = "imagename";
	public static final String KEY_REQUESTID = "requestid";
	public static final String KEY_STATUS = "status";

	// Column indexes
	// public static final int LOCATION_COLUMN = 1;
	public static final int IMAGENAME_COLUMN = 1;
	public static final int REQUESTID_COLUMN = 2;
	public static final int STATUS_COLUMN = 3;

	// Helper class for opening, creating, and managing database version control
	private static class alertsDatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE = "create table "
				+ IMAGES_TABLE + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_IMAGENAME
				+ " TEXT NOT NULL, " + KEY_REQUESTID + " TEXT NOT NULL, " + KEY_STATUS + " TEXT NOT NULL" + " );";

		public alertsDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + IMAGES_TABLE);
			onCreate(db);
		}
	}
}