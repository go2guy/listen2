package com.interact.listen.android.voicemail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;

import com.interact.listen.android.voicemail.sync.Authority;

public class ContactsLiveFolder extends Activity
{
    public static final Uri CONTENT_URI = Uri.parse("content://" + Authority.VOICEMAIL.get() + "/live_contacts");
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if(LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action))
        {
            setResult(RESULT_OK, createLiveFolder(this, CONTENT_URI, getString(R.string.liveFolder_listenContacts),
                                                  R.drawable.ic_launcher_folder_live_contacts));
        }
        else
        {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
    
    private static Intent createLiveFolder(Context context, Uri uri, String name, int icon)
    {
        final Intent bIntent = new Intent(Intent.ACTION_VIEW, CONTENT_URI);
        //bIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final Intent intent = new Intent();
        intent.setData(uri);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT, bIntent);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, name);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, Intent.ShortcutIconResource.fromContext(context, icon));
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_LIST);

        return intent;
    }

}
