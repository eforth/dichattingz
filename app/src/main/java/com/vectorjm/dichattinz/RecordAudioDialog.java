package com.vectorjm.dichattinz;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordAudioDialog extends DialogFragment implements MediaRecorder.OnInfoListener,
        MediaRecorder.OnErrorListener {

    static String TAG = "Recorder";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private SharedViewModel model;
    private Chronometer time;
    private ImageButton recordStopBtn;
    private STATUS recordingStatus = STATUS.stop;
    private MediaRecorder mediaRecorder = null;
    private String currentAudioPath = null;


    private enum STATUS {
        record, stop
    }

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    public RecordAudioDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onResume() {
        super.onResume();
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_record_audio,
                container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.close_menu);
        toolbar.setTitle("Record Audio");

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.close) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        recordStopBtn = v.findViewById(R.id.recordStopBtn);

        recordStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (recordingStatus) {
                    case stop:
                        recordStopBtn.setImageResource(R.drawable.stop);
                        recordingStatus = STATUS.record;
                        record();
                        break;
                    case record:
                        recordStopBtn.setImageResource(R.drawable.record);
                        recordingStatus = STATUS.stop;
                        stop();
                        break;
                }
            }
        });

        time = v.findViewById(R.id.timer);

        return v;
    }

    private void createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "M4A_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".m4a",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentAudioPath = image.getAbsolutePath();
    }

    private void record() {

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            Toast.makeText(getContext(), "Microphone not present!", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(96000);

        try {
            createAudioFile();
            mediaRecorder.setOutputFile(currentAudioPath);
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaRecorder.setOnErrorListener(this);
        mediaRecorder.setOnInfoListener(this);

        time.setBase(SystemClock.elapsedRealtime());
        time.start();

        mediaRecorder.start();
    }

    private void stop() {
        if (mediaRecorder != null) {

            time.stop();

            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            save();
        }
    }

    private void save() {
        Uri uri = Uri.parse(currentAudioPath);
        model.setAudioUri(uri);
        dismiss();
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {

    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }
}
