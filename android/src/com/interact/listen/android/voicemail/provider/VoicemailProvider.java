package com.interact.listen.android.voicemail.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.Voicemail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class VoicemailProvider extends ContentProvider
{
    public static final String AUTHORITY = "com.interact.listen.voicemail";

    private static final String TAG = Constants.TAG + "Provider";

    private static final String DATABASE_NAME = "com.interact.listen.voicemail.db";
    private static final int DATABASE_VERSION = 1;
    private static final String VOICEMAIL_TABLE = "voicemails";
    private static final String VOICEMAIL_INDEX = "vmviewidx";
    
    private static final UriMatcher URI_MATCHER;
    private static final int VOICEMAIL_MATCH = 1;
    private static final int SPECIFIC_VOICEMAIL_MATCH = 2;
    private static final String AUDIO_DIR = "audio";
    private static final String AUDIO_FILE_PREFIX = "voicemail_audio-";
    
    private static final String ID_WHERE = Voicemails._ID + "=?";
    private static final String SET_AUDIO_FOR_DOWNLOAD_SELECTION;
    
    private static HashMap<String, String> voicemailProjectionMap;

    private DatabaseHelper helper;

    @Override
    public boolean onCreate()
    {
        helper = new DatabaseHelper(getContext());
        return true;
    }

    private String matchWhere(Uri uri, String where)
    {
        String myWhere = null;
        switch(URI_MATCHER.match(uri))
        {
            case SPECIFIC_VOICEMAIL_MATCH:
                if(TextUtils.isEmpty(where))
                {
                    myWhere = Voicemails._ID + "=" + uri.getLastPathSegment();
                }
                else
                {
                    myWhere = Voicemails._ID + "=" + uri.getLastPathSegment() + " AND (" + where + ")";
                }
                break;
            case VOICEMAIL_MATCH:
                myWhere = where;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return myWhere;
    }
    
    private void deleteFiles(SQLiteDatabase db, String where, String[] whereArgs)
    {
        Log.i(TAG, "deleting files '" + where + "'");
        
        String[] cols = new String[] {Voicemails.DATA};
        Cursor cursor = db.query(VOICEMAIL_TABLE, cols, where, whereArgs, null, null, null);
        while (cursor != null && cursor.moveToNext())
        {
            if(!cursor.isNull(0))
            {
                File file = new File(cursor.getString(0));
                try
                {
                    Log.i(TAG, "deleting voicemail: " + file);
                    if(!file.delete())
                    {
                        Log.e(TAG, "unable to delete audio file " + file);
                    }
                }
                catch(SecurityException e)
                {
                    Log.e(TAG, "security exception deleting audio file", e);
                }
            }
        }
        cursor.close();
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        String myWhere = matchWhere(uri, where);
        db.beginTransaction();
        int count = 0;
        try
        {
            deleteFiles(db, where, whereArgs);
            count = db.delete(VOICEMAIL_TABLE, myWhere, whereArgs);
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri)
    {
        switch(URI_MATCHER.match(uri))
        {
            case VOICEMAIL_MATCH:
            case SPECIFIC_VOICEMAIL_MATCH:
                return Voicemails.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        if(URI_MATCHER.match(uri) != VOICEMAIL_MATCH)
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if(initialValues != null)
        {
            values = new ContentValues(initialValues);
        }
        else
        {
            values = new ContentValues();
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        long rowId = db.insert(VOICEMAIL_TABLE, Voicemails.TRANSCRIPT, values);
        if(rowId > 0)
        {
            Uri voicemailUri = ContentUris.withAppendedId(Voicemails.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(voicemailUri, null);
            return voicemailUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        if(URI_MATCHER.match(uri) != VOICEMAIL_MATCH)
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        int inserts = 0;
        
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            for(ContentValues value : values)
            {
                if(value != null)
                {
                    long rowId = db.insert(VOICEMAIL_TABLE, Voicemails.TRANSCRIPT, value);
                    if(rowId > 0)
                    {
                        ++inserts;
                    }
                    else
                    {
                        Log.e(TAG, "error inserting a record in a bulk insert");
                    }
                }
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }

        return inserts;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(VOICEMAIL_TABLE);
        String myWhere = matchWhere(uri, selection);
        
        qb.setProjectionMap(voicemailProjectionMap);

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = qb.query(db, projection, myWhere, selectionArgs, null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        String myWhere = matchWhere(uri, where);

        boolean needDelete = false;
        if(values.containsKey(Voicemails.AUDIO_STATE))
        {
            int audioState = values.getAsInteger(Voicemails.AUDIO_STATE);
            if(!Voicemail.isAudioFileState(audioState))
            {
                needDelete = true;
            }
        }
        if(!needDelete && values.containsKey(Voicemails.DATA))
        {
            needDelete = values.get(Voicemails.DATA) == null;
        }
        
        int count = 0;
        db.beginTransaction();
        try
        {
            if(needDelete)
            {
                deleteFiles(db, myWhere, whereArgs);
            }
            count = db.update(VOICEMAIL_TABLE, values, myWhere, whereArgs);
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    private Cursor queryAudioInfo(SQLiteDatabase db, int id) throws FileNotFoundException
    {
        String[] columns = new String[]{Voicemails.AUDIO_STATE, Voicemails.DATA};
        String[] selectionArgs = new String[]{Integer.toString(id)};
        Cursor cursor = db.query(VOICEMAIL_TABLE, columns, ID_WHERE, selectionArgs, null, null, null);
        if(cursor == null || !cursor.moveToNext())
        {
            Log.e(TAG, "voicemail not found for audio lookup: " + id);
            throw new FileNotFoundException("voicemail " + id + " not found");
        }
        return cursor;
    }
    private int getAudioStateFromCursor(Cursor c)
    {
        return c.getInt(0);
    }
    private String getAudioFileName(Cursor c)
    {
        return c.isNull(1) ? null : c.getString(1);
    }

    private File getAudioFile(int id)
    {
        File cdir = getContext().getDir(AUDIO_DIR, Context.MODE_WORLD_READABLE);
        File file = new File(cdir, AUDIO_FILE_PREFIX + id);
        return file;
    }
    
    static
    {
        // update state if not downloading (downloaded if reDownloading) or file not set
        //   or date not set or date was set a long time ago
        StringBuffer sb = new StringBuffer();
        sb.append(Voicemails._ID).append("=? AND (");
        sb.append(Voicemails.AUDIO_STATE).append(" != ? OR ");
        sb.append(Voicemails.DATA).append(" IS NULL OR ");
        sb.append(Voicemails.AUDIO_DATE).append(" IS NULL OR ");
        sb.append(Voicemails.AUDIO_DATE).append(" < ?)");
        SET_AUDIO_FOR_DOWNLOAD_SELECTION = sb.toString();
    }

    private File setAudioForDownload(Uri uri, SQLiteDatabase db, int id, boolean reDownloading) throws FileNotFoundException
    {
        File audioFile = getAudioFile(id);
        long now = System.currentTimeMillis();
        long expire = now - VoicemailHelper.STALE_DOWNLOAD;
        
        ContentValues values = new ContentValues();
        Voicemail.addAudioDownloadingStateValues(values);
        values.put(Voicemails.DATA, audioFile.getAbsolutePath());

        int notSourceState = Voicemail.getUndesiredDownloadState(reDownloading);
        String[] whereArgs = new String[] {Integer.toString(id), Integer.toString(notSourceState), Long.toString(expire)};

        int updated = db.update(VOICEMAIL_TABLE, values, SET_AUDIO_FOR_DOWNLOAD_SELECTION, whereArgs);
        if(updated == 0)
        {
            Log.i(TAG, "setting audio for download, but looks like already being downloaded (redownloading: " + reDownloading + ")");
            throw new FileNotFoundException("voicemail " + id + " already downloading"); // somebody else may be writing
        }
        Log.i(TAG, "audio set for download " + audioFile);
        getContext().getContentResolver().notifyChange(uri, null);
        return audioFile;
    }
    
    private boolean setAudioFileDeleted(Uri uri, SQLiteDatabase db, int id)
    {
        ContentValues values = Voicemail.getClearedDownloadValues();
        values.putNull(Voicemails.DATA);
        
        String[] whereArgs = new String[] {Integer.toString(id)};
        int updated = db.update(VOICEMAIL_TABLE, values, ID_WHERE, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updated > 0;
    }
    
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
    {
        if (URI_MATCHER.match(uri) != SPECIFIC_VOICEMAIL_MATCH)
        {
            throw new IllegalArgumentException("Only support getting stream on voicemails: " + uri);
        }
        int modeBits = audioModeBits(uri, mode);
        
        int id = (int)ContentUris.parseId(uri);
        boolean isForWrite = (modeBits & ParcelFileDescriptor.MODE_WRITE_ONLY) != 0;
        File audioFile = null;
        
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            Cursor c = queryAudioInfo(db, id);
            int audioState = getAudioStateFromCursor(c);
            String audioPath = getAudioFileName(c);
            c.close();
            
            if(!Voicemail.isDownloadedState(audioState)) // not marked downloaded
            {
                if(!isForWrite)
                {
                    Log.i(TAG, "voicemail " + id + " not downloaded (" + audioState + ") for mode '" + mode + "'");
                    throw new FileNotFoundException("voicemail id " + id + " not downloaded");
                }
                if((modeBits & ParcelFileDescriptor.MODE_APPEND) != 0 && Voicemail.isDownloadingState(audioState) &&
                    audioPath != null)
                {
                    Log.i(TAG, "continuing download of " + uri);
                    audioFile = new File(audioPath);
                }
                else
                {
                    Log.i(TAG, "starting new download " + uri);
                    audioFile = setAudioForDownload(uri, db, id, false);
                }
            }
            else if(isForWrite) // marked that it's downloaded, but we are updating
            {
                Log.i(TAG, "re-downloading " + uri);
                audioFile = setAudioForDownload(uri, db, id, true);
            }
            else // just reading
            {
                audioFile = audioPath == null ? null : new File(audioPath);
                Log.i(TAG, "reading " + uri + " -> " + audioFile);
                if(audioFile == null || !audioFile.exists())
                {
                    Log.i(TAG, "audio file doesn't exist " + (audioFile == null ? "Not Set" : audioFile.toString()));
                    setAudioFileDeleted(uri, db, id);
                    db.setTransactionSuccessful();
                    throw new FileNotFoundException(audioPath == null ? "_data not set" : audioPath + " not found");
                }
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        return ParcelFileDescriptor.open(audioFile, modeBits);
    }

    static
    {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, VOICEMAIL_TABLE, VOICEMAIL_MATCH);
        URI_MATCHER.addURI(AUTHORITY, VOICEMAIL_TABLE + "/#", SPECIFIC_VOICEMAIL_MATCH);

        voicemailProjectionMap = new HashMap<String, String>();
        voicemailProjectionMap.put(Voicemails._ID, Voicemails._ID);
        voicemailProjectionMap.put(Voicemails.USER_NAME, Voicemails.USER_NAME);
        voicemailProjectionMap.put(Voicemails.VOICEMAIL_ID, Voicemails.VOICEMAIL_ID);
        voicemailProjectionMap.put(Voicemails.DATE_CREATED, Voicemails.DATE_CREATED);
        voicemailProjectionMap.put(Voicemails.IS_NEW, Voicemails.IS_NEW);
        voicemailProjectionMap.put(Voicemails.HAS_NOTIFIED, Voicemails.HAS_NOTIFIED);
        voicemailProjectionMap.put(Voicemails.LEFT_BY, Voicemails.LEFT_BY);
        voicemailProjectionMap.put(Voicemails.DESCRIPTION, Voicemails.DESCRIPTION);
        voicemailProjectionMap.put(Voicemails.DURATION, Voicemails.DURATION);
        voicemailProjectionMap.put(Voicemails.TRANSCRIPT, Voicemails.TRANSCRIPT);
        voicemailProjectionMap.put(Voicemails.LABEL, Voicemails.LABEL);
        voicemailProjectionMap.put(Voicemails.STATE, Voicemails.STATE);
        voicemailProjectionMap.put(Voicemails.AUDIO_STATE, Voicemails.AUDIO_STATE);
        voicemailProjectionMap.put(Voicemails.AUDIO_DATE, Voicemails.AUDIO_DATE);
        voicemailProjectionMap.put(Voicemails.DATA, Voicemails.DATA);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        private Context context;
        
        public DatabaseHelper(Context ctx)
        {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
            context = ctx;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("CREATE TABLE ").append(VOICEMAIL_TABLE).append(" (");
            sb.append(Voicemails._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
            sb.append(Voicemails.USER_NAME).append(" VARCHAR(50),");
            sb.append(Voicemails.DATE_CREATED).append(" INTEGER,");
            sb.append(Voicemails.VOICEMAIL_ID).append(" INTEGER,");
            sb.append(Voicemails.IS_NEW).append(" BOOLEAN,");
            sb.append(Voicemails.HAS_NOTIFIED).append(" BOOLEAN,");
            sb.append(Voicemails.LEFT_BY).append(" VARCHAR(255),");
            sb.append(Voicemails.DESCRIPTION).append(" TEXT,");
            sb.append(Voicemails.DURATION).append(" INTEGER,");
            sb.append(Voicemails.TRANSCRIPT).append(" TEXT,");
            sb.append(Voicemails.LABEL).append(" VARCHAR(50),");
            sb.append(Voicemails.STATE).append(" STATE,");
            sb.append(Voicemails.AUDIO_STATE).append(" INTEGER,");
            sb.append(Voicemails.AUDIO_DATE).append(" INTEGER,");
            sb.append(Voicemails.DATA).append(" VARCHAR(255),");
            sb.append(" UNIQUE (").append(Voicemails.USER_NAME).append(',').append(Voicemails.VOICEMAIL_ID);
            sb.append(") ON CONFLICT REPLACE)");

            Log.i(TAG, "creating database");
            db.execSQL(sb.toString());
            
            sb = new StringBuffer();
            sb.append("CREATE UNIQUE INDEX ").append(VOICEMAIL_INDEX).append(" ON ").append(VOICEMAIL_TABLE);
            sb.append(" (").append(Voicemails.USER_NAME).append(',').append(Voicemails.DATE_CREATED);
            sb.append(',').append(Voicemails.VOICEMAIL_ID).append(");");

            Log.i(TAG, "creating index");
            db.execSQL(sb.toString());
            
            File cdir = context.getDir(AUDIO_DIR, Context.MODE_WORLD_READABLE);
            createNoMediaFile(cdir);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.i(TAG, "upgrade database from " + oldVersion + " to " + newVersion);
            db.execSQL("DROP INDEX IF EXISTS " + VOICEMAIL_INDEX);
            db.execSQL("DROP TABLE IF EXISTS " + VOICEMAIL_TABLE);
            
            deleteAllVoicemailFiles(context);
            
            onCreate(db);
        }
    }

    private static void deleteAllVoicemailFiles(Context context)
    {
        try
        {
            File cdir = context.getCacheDir();
            java.io.FilenameFilter filter = new java.io.FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String filename)
                {
                    return filename != null && filename.startsWith(AUDIO_FILE_PREFIX);
                }
            };
            
            File[] files = cdir.listFiles(filter);
            for (File file : files)
            {
                Log.i(TAG, "deleting all voicemails: " + file);
                file.delete();
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "error removing all voicemail files", e);
        }
    }
    
    private static int audioModeBits(Uri uri, String mode) throws FileNotFoundException
    {
        if (mode == null)
        {
            throw new FileNotFoundException("Mode note set for " + uri);
        }
        
        int modeBits = 0;
        if(mode.equals("r"))
        {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        }
        else if(mode.equals("w") || mode.equals("wt"))
        {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE |
                       ParcelFileDescriptor.MODE_TRUNCATE;
        }
        else if(mode.equals("wa"))
        {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE |
                       ParcelFileDescriptor.MODE_APPEND;
        }
        else if(mode.equals("rw"))
        {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        }
        else if(mode.equals("rwt"))
        {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE |
                       ParcelFileDescriptor.MODE_TRUNCATE;
        }
        else
        {
            throw new FileNotFoundException("Invalid mode for " + uri + ": " + mode);
        }
        return modeBits;
    }
    
    private static void createNoMediaFile(File cdir)
    {
        File noMedia = new File(cdir, ".nomedia");
        if(!noMedia.exists())
        {
            try
            {
                noMedia.createNewFile();
            }
            catch(IOException e)
            {
                Log.e(TAG, "error creating " + noMedia);
            }
        }
        
    }

}