package com.vectorjm.dichattinz;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class ChatBoutDialog extends DialogFragment {

    static String TAG = "Post";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 1046;
    private static final int REQUEST_TAKE_VIDEO = 1016;

    private static final int REQUEST_CAMERA_PERMISSION = 250;

    private ImageView photo;
    private AppCompatImageButton clearPhotoBtn;
    private PlayerView playerView;
    private AppCompatImageButton clearMediaBtn;

    private SharedViewModel model;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private TextInputEditText textField;
    private String currentPath;
    private TYPE currentType = TYPE.text;

    private enum TYPE {
        text, image, video, audio
    }

    // Requesting permission to CAMERA
    private boolean permissionToUseCameraAccepted = false;
    private String [] permissions = {Manifest.permission.CAMERA};


    public ChatBoutDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_DialogWhenLarge);
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_chat_bout, container, false);

        Toolbar toolbar = v.findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.post);
        toolbar.inflateMenu(R.menu.post_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.close:
                        dismiss();
                        return true;
                    case R.id.save:
                        save();
                        return true;
                    default:
                        return false;
                }
            }
        });

        v.findViewById(R.id.cameraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentType = TYPE.image;
                onSelectFromCamera();
            }
        });
        v.findViewById(R.id.galleryBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentType = TYPE.image;
                onSelectFromGallery();
            }
        });
        v.findViewById(R.id.audioBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentType = TYPE.audio;
                onSelectAudio();
            }
        });
        v.findViewById(R.id.videoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentType = TYPE.video;
                onSelectVideo();
            }
        });

        photo = v.findViewById(R.id.photo);
        clearPhotoBtn = v.findViewById(R.id.closeMediaBtn);
        playerView = v.findViewById(R.id.playerView);
        clearMediaBtn = v.findViewById(R.id.closePlayableBtn);
        textField = v.findViewById(R.id.text);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup the shared view model so communication can take place
        // between RecordAudioDialog and ChatBoutDialog
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        // After the audio is recorded and saved to the filesystem
        // load the audio into the playerview
        model.getAudioUri().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                if (uri != null) {
                    currentPath = uri.toString();
                    setPlayableView(uri);
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPath = image.getAbsolutePath();
        return image;
    }

    private File createVideoFile() throws IOException {
        // Create an video file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPath = video.getAbsolutePath();
        return video;
    }

    private void onSelectFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File imagePath = null;
            try {
                imagePath = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imagePath != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.vectorjm.dichattinz.fileprovider",
                        imagePath);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void onSelectFromGallery() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, REQUEST_PICK_PHOTO);
        }
    }

    private void onSelectAudio() {
        // Show audio capture fragment
        RecordAudioDialog dialog = new RecordAudioDialog();
        dialog.show(getFragmentManager(), null);
    }

    private void onSelectVideo() {
        Intent cptureVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (cptureVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File videoPath = null;
            try {
                videoPath = createVideoFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (videoPath != null) {
                Uri videoURI = FileProvider.getUriForFile(getActivity(),
                        "com.vectorjm.dichattinz.fileprovider",
                        videoPath);
                cptureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                startActivityForResult(cptureVideoIntent, REQUEST_TAKE_VIDEO);
            }
        }
    }

    private void setImageView(Uri imageUri) {
        // Show image View
        photo.setVisibility(View.VISIBLE);
        clearPhotoBtn.setVisibility(View.VISIBLE);

        // Set a clickListener on the post_menu button to clear any images that
        // were set previously.
        clearPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPath = null;
                currentType = TYPE.text;
                photo.setVisibility(View.GONE);
                clearPhotoBtn.setVisibility(View.GONE);
            }
        });

        //Set the Uri (content://....) for the imageview
        photo.setImageURI(imageUri);
    }

    private void setPlayableView(Uri playableUri) {
        // Show video player View
        playerView.setVisibility(View.VISIBLE);
        clearMediaBtn.setVisibility(View.VISIBLE);

        // Set a clickListener on the post_menu button to clear any images that
        // were set previously.
        clearMediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPath = null;
                currentType = TYPE.text;
                model.setAudioUri(null);
                playerView.setVisibility(View.GONE);
                clearMediaBtn.setVisibility(View.GONE);
            }
        });

        //Set the Uri (content://....) for the playerView
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                Util.getUserAgent(getActivity(), getContext().getString(R.string.app_name)));
        MediaSource mediaSource = new ExtractorMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(playableUri);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getActivity());
        playerView.setPlayer(player);
        player.prepare(mediaSource);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            Uri capturedImageUrl = Uri.parse(currentPath);
            setImageView(capturedImageUrl);

        } else if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK) {

            currentPath = data.getData().toString();
            setImageView(data.getData());

        } else if (requestCode == REQUEST_TAKE_VIDEO && resultCode == RESULT_OK) {

            currentPath = data.getData().toString();
            setPlayableView(data.getData());
        }
    }

    // Upload the media first if the post has any media
    private void save() {
        switch (currentType) {
            case audio:
            case image:
            case video:
                saveMedia();
                break;
            case text:
                saveChatBout(null);
                break;
        }
    }

    // Save the post to the collection "posts" in the database
    private void saveChatBout(Uri uri) {

        if (TextUtils.isEmpty(textField.getText().toString())) {
            Toast.makeText(getContext(), "ChatBout text field cannot be empty!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> chatBout = new HashMap<>();

        chatBout.put("uid", auth.getCurrentUser().getEmail());
        chatBout.put("dateCreated", (new Date()).getTime());
        chatBout.put("text", textField.getText().toString());

        switch (currentType) {
            case text:
                chatBout.put("type", "ChatBout");
                break;
            case image:
                chatBout.put("imageUrl", uri);
                chatBout.put("type", "ChatBoutImage");
                break;
            case audio:
            case video:
                chatBout.put("playableUrl", uri);
                chatBout.put("type", "ChatBoutPlayable");
                break;
        }

        db.collection("posts")
                .add(chatBout)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i(TAG, documentReference.get().getResult().getData().toString());
                        Toast.makeText(getContext(),"ChatBout Created!", Toast.LENGTH_SHORT)
                                .show();
                        dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(),"ChatBout Not Created!", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    // Configure the storage reference depending on the media type
    private StorageReference getFileUri() {

        Uri uri = Uri.parse(currentPath);
        String fileName = uri.getLastPathSegment();

        switch (currentType) {
            case video:
                return storage.getReference().child("videos/".concat(fileName));
            case image:
                return storage.getReference().child("images/".concat(fileName));
            case audio:
                return storage.getReference().child("audio/".concat(fileName));
                default:
                    return null;
        }
    }

    // Upload the media to the cloud via firbase storage
    private void saveMedia() {

        Uri uri = Uri.parse(currentPath);
        final StorageReference storageRef = getFileUri();
        UploadTask uploadTask = storageRef.putFile(uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
                Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Upload successful!", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    task.getException().printStackTrace();
                    throw task.getException();
                }

                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    saveChatBout(task.getResult());
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    // Results of whether the user allowed access to the camera or not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA_PERMISSION:
                permissionToUseCameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }
}

