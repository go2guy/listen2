package com.interact.listen.android.voicemail;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.Voicemails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

public final class Voicemail implements Parcelable, Comparable<Voicemail>
{
    public enum Label
    {
        INBOX, TRASH
    }
    
    public static final Parcelable.Creator<Voicemail> CREATOR = new Parcelable.Creator<Voicemail>()
    {
        public Voicemail createFromParcel(Parcel in)
        {
            return new Voicemail(in);
        }

        public Voicemail[] newArray(int size)
        {
            return new Voicemail[size];
        }
    };

    public static final String[] ALL_PROJECTION = new String[] {Voicemails._ID, Voicemails.USER_NAME,
                                                                Voicemails.DATE_CREATED, Voicemails.VOICEMAIL_ID,
                                                                Voicemails.IS_NEW, Voicemails.HAS_NOTIFIED,
                                                                Voicemails.LEFT_BY, Voicemails.LEFT_BY_NAME,
                                                                Voicemails.DESCRIPTION,
                                                                Voicemails.DURATION, Voicemails.TRANSCRIPT,
                                                                Voicemails.LABEL, Voicemails.STATE,
                                                                Voicemails.AUDIO_STATE, Voicemails.AUDIO_DATE,
                                                                Voicemails.DISPLAY_NAME, Voicemails.MIME_TYPE,
                                                                Voicemails.SIZE, Voicemails.TITLE};

    private static final int MARKED_READ     = 0x01;
    private static final int MARKED_DELETED  = 0x02;
    
    private static final int AUDIO_DOWNLOADING = 0x01;
    private static final int AUDIO_DOWNLOADED  = 0x02;
    private static final int AUDIO_ERROR       = 0x04;
    
    private static final String TAG = Constants.TAG + "Voicemail";
    
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private int id;
    private String userName;
    
    private long voicemailID;
    private boolean isNew;
    private boolean hasNotified;
    private String leftBy;
    private String leftByName;
    private String description;
    private long dateCreated;
    private int duration;
    private String transcription;

    private Label label;
    private int state;
    
    private int audioState;
    private long audioDate;
    
    private String audioDisplayName;
    private String audioMimeType;
    private long audioFileSize;
    private String audioTitle;
    
    public static int getAudioDownloadedState()
    {
        return AUDIO_DOWNLOADED;
    }
    public static int getAudioDownloadingState()
    {
        return AUDIO_DOWNLOADING;
    }
    public static boolean isDownloadedState(int audioState)
    {
        return audioState == AUDIO_DOWNLOADED;
    }
    public static boolean isDownloadingState(int audioState)
    {
        return audioState == AUDIO_DOWNLOADING;
    }
    public static boolean isDownloadErrorState(int audioState)
    {
        return audioState == AUDIO_ERROR;
    }
    public static boolean isAudioFileState(int audioState)
    {
        return audioState == AUDIO_DOWNLOADING || audioState == AUDIO_DOWNLOADED;
    }
    public static Voicemail create(Cursor cursor, String[] projection)
    {
        Voicemail vm = new Voicemail();
        if(projection == ALL_PROJECTION)
        {
            int idx = 0;

            vm.id = cursor.getInt(idx++);
            vm.userName = cursor.getString(idx++);
            vm.dateCreated = cursor.getLong(idx++);
            vm.voicemailID = cursor.getLong(idx++);
            vm.isNew = cursor.getInt(idx++) != 0;
            vm.hasNotified = cursor.getInt(idx++) != 0;
            vm.leftBy = getStringFromCursor(cursor, idx++);
            vm.leftByName = getStringFromCursor(cursor, idx++);
            vm.description = getStringFromCursor(cursor, idx++);
            vm.duration = (int)cursor.getLong(idx++);
            vm.transcription = getStringFromCursor(cursor, idx++);
            vm.label = getLabel(getStringFromCursor(cursor, idx++));
            vm.state = cursor.getInt(idx++);
            vm.audioState = cursor.getInt(idx++);
            vm.audioDate = cursor.getLong(idx++);
            
            vm.audioDisplayName = getStringFromCursor(cursor, idx++);
            vm.audioMimeType = getStringFromCursor(cursor, idx++);
            vm.audioFileSize = cursor.getLong(idx++);
            vm.audioTitle = getStringFromCursor(cursor, idx++);
            
            if(TextUtils.equals(vm.leftByName, "null"))
            {
                vm.leftByName = null;
            }

            return vm;
        }
        throw new IllegalArgumentException("just supporting from the all projection right now");
    }

    public static void addAudioDownloadingStateValues(ContentValues values)
    {
        values.put(Voicemails.AUDIO_STATE, AUDIO_DOWNLOADING);
        values.put(Voicemails.AUDIO_DATE, System.currentTimeMillis());
    }
    public static void addAudioDownloadedStateValues(ContentValues values)
    {
        values.put(Voicemails.AUDIO_STATE, AUDIO_DOWNLOADED);
        values.put(Voicemails.AUDIO_DATE, System.currentTimeMillis());
    }
    public static void addAudioErrorStateValues(ContentValues values)
    {
        values.put(Voicemails.AUDIO_STATE, AUDIO_ERROR);
        values.put(Voicemails.AUDIO_DATE, System.currentTimeMillis());
    }
    public static int getUndesiredDownloadState(boolean forRedownload)
    {
        return forRedownload ? Voicemail.AUDIO_DOWNLOADED : Voicemail.AUDIO_DOWNLOADING;
    }

    public ContentValues getInsertValues()
    {
        ContentValues values = new ContentValues();

        values.put(Voicemails.USER_NAME,    userName);
        values.put(Voicemails.VOICEMAIL_ID, voicemailID);
        values.put(Voicemails.DATE_CREATED, dateCreated);
        values.put(Voicemails.IS_NEW,       isNew);
        values.put(Voicemails.HAS_NOTIFIED, hasNotified);
        values.put(Voicemails.LEFT_BY,      leftBy);
        values.put(Voicemails.LEFT_BY_NAME, leftByName);
        values.put(Voicemails.DESCRIPTION,  description);
        values.put(Voicemails.DURATION,     duration);
        values.put(Voicemails.TRANSCRIPT,   transcription);
        values.put(Voicemails.LABEL,        label.name());
        values.put(Voicemails.STATE,        state);
        values.put(Voicemails.AUDIO_STATE,  audioState);
        values.put(Voicemails.AUDIO_DATE,   audioDate);
        values.put(Voicemails.DISPLAY_NAME, audioDisplayName);
        values.put(Voicemails.TITLE,        audioTitle);
        values.put(Voicemails.MIME_TYPE,    audioMimeType);
        values.put(Voicemails.SIZE,         audioFileSize);
        
        return values;
    }
    
    public void setIdFromInsert(int providerID)
    {
        this.id = providerID;
    }

    public static Voicemail parse(String account, JSONObject json) throws JSONException, java.text.ParseException
    {
        Voicemail vm = new Voicemail();
        vm.id = 0;
        vm.userName = account;

        vm.voicemailID = json.getLong("id");
        vm.isNew = json.getBoolean("isNew");
        vm.hasNotified = !vm.isNew;
        
        vm.leftBy = json.has("leftBy") ? json.getString("leftBy") : null;
        vm.leftByName = json.has("leftByName") ? json.getString("leftByName") : null;
        vm.description = json.has("description") ? json.getString("description") : null;
        vm.setDateCreated(json.has("dateCreated") ? json.getString("dateCreated") : null);
        vm.setDurationFromString(json.has("duration") ? json.getString("duration") : null);
        vm.transcription = json.has("transcription") ? json.getString("transcription") : null;
        
        vm.state = 0;
        vm.audioState = 0;
        vm.audioDate = 0;
        vm.label = Label.INBOX;
        
        if(TextUtils.equals(vm.leftByName, "(Unknown)") || TextUtils.equals(vm.leftByName, "null"))
        {
            vm.leftByName = null;
        }
        
        vm.audioDisplayName = "";
        vm.audioTitle = "";
        vm.audioFileSize = 0;
        vm.audioMimeType = WavConversion.Type.UNKNOWN.getMIME();
        
        return vm;
    }
    
    public static String getDateCreatedFromMS(Context context, long ms, boolean dayCheck)
    {
        if(dayCheck)
        {
            return DateUtils.formatSameDayTime(ms, System.currentTimeMillis(), DateFormat.SHORT, DateFormat.SHORT).toString();
        }
        else
        {
            return DateUtils.formatDateTime(context, ms,
                                            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME |
                                            DateUtils.FORMAT_NUMERIC_DATE);
        }
    }

    public Voicemail copy()
    {
        Voicemail vm = new Voicemail();
        
        vm.id = id;
        vm.userName = userName;
        vm.dateCreated = dateCreated;
        vm.voicemailID = voicemailID;
        vm.isNew = isNew;
        vm.hasNotified = hasNotified;
        vm.leftBy = leftBy;
        vm.leftByName = leftByName;
        vm.description = description;
        vm.duration = duration;
        vm.transcription = transcription;
        vm.label = label;
        vm.state = state;
        vm.audioState = audioState;
        vm.audioDate = audioDate;

        return vm;
    }

    @Override
    public int compareTo(Voicemail another)
    {
        if(another == null || this.getVoicemailId() > another.getVoicemailId())
        {
            return 1;
        }
        return this.getVoicemailId() == another.getVoicemailId() ? 0 : -1;
    }

    public Uri getUri()
    {
        return ContentUris.withAppendedId(Voicemails.CONTENT_URI, id);
    }
    
    public int getId()
    {
        return id;
    }
    public String getUserName()
    {
        return userName;
    }
    public long getVoicemailId()
    {
        return voicemailID;
    }
    public boolean getIsNew()
    {
        return isNew;
    }
    public boolean needsNotification()
    {
        return isNew && !hasNotified;
    }
    public String getLeftBy()
    {
        return leftBy;
    }
    public String getLeftByName()
    {
        return leftByName;
    }
    public String getDescription()
    {
        return description;
    }
    public String getDateCreatedString(Context context, boolean dayCheck, String unknownString)
    {
        return dateCreated == 0 ? unknownString : getDateCreatedFromMS(context, dateCreated, dayCheck);
    }
    public long getDateCreatedMS()
    {
        return dateCreated;
    }
    public boolean isOlderThan(long ms)
    {
        return dateCreated < ms;
    }
    public int getDuration()
    {
        return duration;
    }
    public void setDuration(int duration)
    {
        this.duration = duration;
    }
    public String getDurationString()
    {
        
        int s = duration / 1000;
        int h = s / 3600;
        s -= h * 3600;
        int m = s / 60;
        s -= m * 60;

        StringBuilder sb = new StringBuilder();
        if(h > 0)
        {
            // this won't happen has they can only record 2 minutes
            java.util.Formatter formatter = new java.util.Formatter(sb);
            formatter.format("%02d.%02d.%02d", h, m, s);
        }
        else if(m == 0)
        {
            if(s == 1)
            {
                sb.append("1 sec");
            }
            else
            {
                sb.append(s).append(" secs");
            }
        }
        else if(m == 1)
        {
            if(s == 0)
            {
                sb.append("1 min");
            }
            else if(s == 1)
            {
                sb.append("1 min 1 sec");
            }
            else
            {
                sb.append("1 min ").append(s).append(" secs");
            }
        }
        else
        {
            if(s == 0)
            {
                sb.append(m).append(" mins");
            }
            else if(s == 1)
            {
                sb.append(m).append(" mins 1 sec");
            }
            else
            {
                sb.append(m).append(" mins ").append(s).append(" secs");
            }
        }
        return sb.toString();
    }
    public String getTranscription()
    {
        return transcription;
    }
    public boolean hasTranscription()
    {
        return !TextUtils.isEmpty(transcription);
    }
    public String getAudioTitle()
    {
        return audioTitle == null ? "" : audioTitle;
    }
    public Label getLabel()
    {
        return label;
    }
    public long getAudioDateMS()
    {
        return audioDate;
    }
    public boolean isAudioOlderThan(long ms)
    {
        return ms == 0 ? false : audioDate < ms;
    }

    public boolean isDownloaded()
    {
        return isDownloadedState(audioState);
    }
    public boolean isDownloading()
    {
        return isDownloadingState(audioState);
    }
    public boolean isDownloadError()
    {
        return isDownloadErrorState(audioState);
    }
    public boolean needsDownload(long now, long errorTimeout, long downloadingTimeout)
    {
        switch(audioState)
        {
            case 0:
                return true;
            case AUDIO_DOWNLOADED:
                return false;
            case AUDIO_DOWNLOADING:
                return downloadingTimeout != 0 && audioDate + downloadingTimeout < now;
            case AUDIO_ERROR:
                return audioDate + errorTimeout < now;
            default:
                Log.e(TAG, "unknown audio state " + audioState);
                return false;
        }
    }
    public void markDownloading()
    {
        audioDate = System.currentTimeMillis();
        audioState = AUDIO_DOWNLOADING;
    }
    public void markDownloadError()
    {
        audioDate = System.currentTimeMillis();
        audioState = AUDIO_ERROR;
        clearAudioMeta();
    }

    private void clearAudioMeta()
    {
        audioFileSize = 0;
        audioMimeType = WavConversion.Type.UNKNOWN.getMIME();
        audioDisplayName = "";
        audioTitle = "";
    }
    
    /**
     * 
     * @param context
     * @param downloaded -1 on error
     * @param type
     */
    public void markDownloaded(Context context, long downloaded, WavConversion.Type type)
    {
        audioDate = System.currentTimeMillis();
        audioState = downloaded != -1 ? AUDIO_DOWNLOADED : AUDIO_ERROR;
        if(audioState == AUDIO_DOWNLOADED)
        {
            audioFileSize = downloaded;
            audioMimeType = type.getMIME();
            audioDisplayName = createDisplayName(context, type);
            audioTitle = createTitle(context, type);
        }
        else
        {
            clearAudioMeta();
        }
    }
    
    private String createDisplayName(Context context, WavConversion.Type type)
    {
        final CharSequence createStr;
        if(dateCreated == 0)
        {
            createStr = "0";
        }
        else
        {
            createStr = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", dateCreated);
        }

        return context.getString(R.string.voicemail_display_name, createStr, type.getExtension());
    }

    private String createTitle(Context context, WavConversion.Type type)
    {
        final String createdStr = getDateCreatedString(context, false, "");
        final String durStr = getDurationString();
        final String leftByStr = leftByName == null ? leftBy : leftByName;
        return context.getString(R.string.voicemail_title, createdStr, durStr, leftByStr);
    }

    public ContentValues clearDownloaded()
    {
        audioDate = System.currentTimeMillis();
        audioState = 0;

        clearAudioMeta();

        return getClearedDownloadValues();
    }
    public static ContentValues getClearedDownloadValues()
    {
        ContentValues values = new ContentValues();
        values.put(Voicemails.AUDIO_STATE, 0);
        values.put(Voicemails.SIZE, 0);
        values.put(Voicemails.MIME_TYPE, WavConversion.Type.UNKNOWN.getMIME());
        values.put(Voicemails.DISPLAY_NAME, "");
        values.put(Voicemails.TITLE, "");
        
        return values;
    }

    public ContentValues getAudioStateValues()
    {
        ContentValues values = new ContentValues();
        values.put(Voicemails.AUDIO_STATE, audioState);
        values.put(Voicemails.AUDIO_DATE, audioDate);
        values.put(Voicemails.SIZE, audioFileSize);
        values.put(Voicemails.MIME_TYPE, audioMimeType);
        values.put(Voicemails.DISPLAY_NAME, audioDisplayName);
        values.put(Voicemails.TITLE, audioTitle);
        return values;
    }
    
    public ContentValues markRead(boolean read)
    {
        isNew = !read;
        state |= MARKED_READ;
        
        ContentValues values = new ContentValues();
        values.put(Voicemails.IS_NEW, isNew);
        values.put(Voicemails.STATE, state);
        
        if(read)
        {
            hasNotified = true;
            values.put(Voicemails.HAS_NOTIFIED, true);
        }

        return values;
    }
    
    public ContentValues clearMarkedRead()
    {
        state &= ~MARKED_READ;

        ContentValues values = new ContentValues();
        values.put(Voicemails.STATE, state);
        return values;
    }
    
    public ContentValues markDeleted()
    {
        label = Label.TRASH;
        state |= MARKED_DELETED;

        ContentValues values = new ContentValues();
        values.put(Voicemails.LABEL, label.name());
        values.put(Voicemails.STATE, state);
        return values;
    }

    public void setTrashed()
    {
        label = Label.TRASH;
        state = 0;
    }
    
    public boolean isMarkedRead()
    {
        return (state & MARKED_READ) != 0;
    }
    public boolean isMarkedTrash()
    {
        return (state & MARKED_DELETED) != 0;
    }
    
    public void setNotified(boolean b)
    {
        hasNotified = true;
    }

    public ContentValues updateValues(Voicemail source)
    {
        ContentValues values = new ContentValues();
        if(isNew != source.isNew)
        {
            isNew = source.isNew;
            values.put(Voicemails.IS_NEW, source.isNew);
            if(!isNew && !hasNotified)
            {
                hasNotified = true;
                values.put(Voicemails.HAS_NOTIFIED, hasNotified);
            }
        }
        if(!TextUtils.equals(transcription, source.transcription))
        {
            transcription = source.transcription;
            values.put(Voicemails.TRANSCRIPT, source.transcription);
        }
        if(!TextUtils.equals(description, source.description))
        {
            description = source.description;
            values.put(Voicemails.DESCRIPTION, source.description);
        }
        if(!TextUtils.equals(leftBy, source.leftBy))
        {
            leftBy = source.leftBy;
            values.put(Voicemails.LEFT_BY, source.leftBy);
        }
        if(!TextUtils.equals(leftByName, source.leftByName))
        {
            leftByName = source.leftByName;
            values.put(Voicemails.LEFT_BY_NAME, source.leftByName);
        }
        if(dateCreated != source.dateCreated)
        {
            dateCreated = source.dateCreated;
            values.put(Voicemails.DATE_CREATED, source.dateCreated);
        }
        if(duration != source.duration)
        {
            duration = source.duration;
            values.put(Voicemails.DURATION, source.duration);
        }
        return values;
    }

    
    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(id);
        out.writeString(userName);
        out.writeLong(voicemailID);
        out.writeByte(isNew ? (byte)1 : (byte)0);
        out.writeByte(hasNotified ? (byte)1 : (byte)0);
        out.writeString(leftBy);
        out.writeString(leftByName);
        out.writeString(description);
        out.writeLong(dateCreated);
        out.writeInt(duration);
        out.writeString(transcription);
        out.writeString(label.toString());
        out.writeInt(state);
        out.writeInt(audioState);
        out.writeLong(audioDate);
    }

    public void readFromParcel(Parcel in)
    {
        id = in.readInt();
        userName = in.readString();
        voicemailID = in.readLong();
        isNew = in.readByte() != 0;
        hasNotified = in.readByte() != 0;
        leftBy = in.readString();
        leftByName = in.readString();
        description = in.readString();
        dateCreated = in.readLong();
        duration = in.readInt();
        transcription = in.readString();
        label = getLabel(in.readString());
        state = in.readInt();
        audioState = in.readInt();
        audioDate = in.readLong();
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[Voicemail ").append(id).append(' ').append(userName).append('-').append(voicemailID);
        sb.append(" new=").append(isNew).append(" notified=").append(hasNotified).append(" leftBy=").append(leftBy);
        sb.append(" leftByName='").append(leftByName).append("' created=").append(dateCreated);
        sb.append(" label=").append(label.name()).append(" state=").append(state);
        sb.append(" audioState=").append(audioState).append(" audioDate=").append(audioDate);
        sb.append(" size=").append(audioFileSize).append(" type=").append(audioMimeType);
        sb.append(" title=").append(audioTitle).append(" name=").append(audioDisplayName);
        sb.append(" duration=").append(duration).append(" transcription='").append(transcription).append("']");
        return sb.toString();
    }

    private Voicemail()
    {
    }

    private Voicemail(Parcel in)
    {
        readFromParcel(in);
    }

    private void setDateCreated(String dateString) throws java.text.ParseException
    {
        if(dateString == null)
        {
            dateCreated = 0;
            Log.e(TAG, "Date created is null");
        }
        else
        {
            dateCreated = SDF.parse(dateString).getTime();
        }
    }

    private void setDurationFromString(String hhMMss)
    {
        if (TextUtils.isEmpty(hhMMss))
        {
            duration = 0;
            return;
        }
        
        if (!hhMMss.contains(":"))
        {
            // assume milliseconds
            duration = Integer.parseInt(hhMMss);
        }
        else
        {
            String[] parts = hhMMss.split("[\\:]");
            int h = 0, m = 0, s = 0;
    
            int i = 0;
            if (parts.length > 2)
            {
                h = Integer.parseInt(parts[i++], 10);
            }
            if (parts.length > 1)
            {
                m = Integer.parseInt(parts[i++], 10);
            }
            if (parts.length > 0)
            {
                s = Integer.parseInt(parts[0], 10);
            }
            duration = ((h * 60 + m) * 60 + s) * 1000;
        }
    }
    
    private static Label getLabel(String label)
    {
        try
        {
            return label == null ? Label.INBOX : Label.valueOf(label);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Label '" + label + "' is unknown");
            return Label.INBOX;
        }
    }

    private static String getStringFromCursor(Cursor c, int idx)
    {
        return c.isNull(idx) ? null : c.getString(idx);
    }
    
}
