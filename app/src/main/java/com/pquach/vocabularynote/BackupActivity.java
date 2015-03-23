package com.pquach.vocabularynote;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import static android.content.IntentSender.*;

/**
 * An activity that lets users backup database onto google drive.
 * Users will ber asked to login into google account when they first start the activity.
 * The activity uses the intent creator to let users create new file.
 * It also allows users to select the parent folder and the title of the newly created file
 */

public class BackupActivity extends BaseGoogleDriveActivity{

    /**
     * TAG string
     */
    private  static final String TAG = "BackupActivity";
    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_CREATOR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Log.i(TAG,"onCreate");
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Handles resolution callbacks and result callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    showMessage("Data is successfully backed up");
                }
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    final ResultCallback<DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveContentsResult>() {
                @Override
                public void onResult(DriveContentsResult result) {
                    if(!result.getStatus().isSuccess()){
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    /*
                        Write file content to OutputStream
                     */
                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                    FileProcessor fileProcessor = new FileProcessor(getApplicationContext());
                    if(!fileProcessor.exportData(outputStream)){
                        Log.i(TAG,"Error while writing file to OutputStream");
                        showMessage("Error while writing file");
                        return;
                    }
                    /*
                        Build file creator activity
                     */
                    Date backupDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    String fileTitle = dateFormat.format(backupDate);
                    CustomPropertyKey filePropertyKey = new CustomPropertyKey("file",CustomPropertyKey.PRIVATE);
                    MetadataChangeSet changeSetBuilder = new MetadataChangeSet.Builder()
                            .setMimeType("text/plain")
                            .setCustomProperty(filePropertyKey,"file")
                            .setTitle(fileTitle+".vob").build();
                    IntentSender intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(changeSetBuilder)
                            .setInitialDriveContents(result.getDriveContents())
                            .build(getGoogleApiClient());
                    try {
                        startIntentSenderForResult(
                                intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                    } catch (SendIntentException e) {
                        Log.w(TAG, "Unable to send intent", e);
                    }
                }
            };
}
