package com.vectorjm.dichattinz;


import android.content.Intent;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.android.exoplayer2.ui.PlayerView;
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

public class ChatBoutFragment extends DialogFragment {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 1046;

    private ImageView photo;
    private AppCompatImageButton clearPhotoBtn;
    private PlayerView playerView;
    private AppCompatImageButton clearMediaBtn;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private TextInputEditText textField;
    private String currentPhotoPath;


    public ChatBoutFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_DialogWhenLarge);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_bout, container, false);

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
                onSelectFromCamera();
            }
        });
        v.findViewById(R.id.galleryBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectFromGallery();
            }
        });

        photo = v.findViewById(R.id.photo);
        clearPhotoBtn = v.findViewById(R.id.closeMediaBtn);
        playerView = v.findViewById(R.id.playerView);
        clearMediaBtn = v.findViewById(R.id.closePlayableBtn);
        textField = v.findViewById(R.id.text);


        return v;
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
        currentPhotoPath = image.getAbsolutePath();
        return image;
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

    private void setImageView(Uri imageUri) {
        // Show image View
        photo.setVisibility(View.VISIBLE);
        clearPhotoBtn.setVisibility(View.VISIBLE);

        // Set a clickListener on the post_menu button to clear any images that
        // were set previously.
        clearPhotoBtn
                .findViewById(R.id.closeMediaBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentPhotoPath = null;
                        photo.setVisibility(View.GONE);
                        clearPhotoBtn.setVisibility(View.GONE);
                    }
                });

        //Set the Uri (content://....) for the imageview
        photo.setImageURI(imageUri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Uri capturedImageUrl = Uri.parse(currentPhotoPath);
            setImageView(capturedImageUrl);
        } else if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            currentPhotoPath = selectedImageUri.toString();
            setImageView(selectedImageUri);
        }
    }

    // Upload the additional content (image/video/audio)
    private void save() {

        // Save a post without any media content
        if (currentPhotoPath == null) {
            saveChatBout(null, "ChatBout");
            return;
        }

        // TODO: the last segment of the Uri (filename)
        final StorageReference storageRef = storage.getReference().child("images/filename");
        Uri filePath = Uri.parse(currentPhotoPath);

        UploadTask uploadTask = storageRef.putFile(filePath);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
                Toast.makeText(getContext(), "Image failed to upload!", Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Image upload successful!", Toast.LENGTH_SHORT)
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

                    Uri downloadUri = task.getResult();
                    saveChatBout(downloadUri, "ChatBoutImage");

                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    private void saveChatBout(Uri uri, String type) {

        if (TextUtils.isEmpty(textField.getText().toString())) {
            Toast.makeText(getContext(), "ChatBout text field cannot be empty!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> chatBout = new HashMap<>();

        chatBout.put("uid", auth.getCurrentUser().getEmail());
        chatBout.put("dateCreated", (new Date()).getTime());
        chatBout.put("text", textField.getText().toString());

        if (type.equals("ChatBoutImage")) {

            chatBout.put("imageUrl", uri);
            chatBout.put("type", "ChatBoutImage");

        } else if (type.equals("ChatBoutPlayable")) {

            chatBout.put("playableUrl", uri);
            chatBout.put("type", "ChatBoutPlayable");
        } else {

            chatBout.put("type", "ChatBout");
        }

        db.collection("posts")
                .add(chatBout)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Document",
                                "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.w("Error", "Error adding document", e);
                    }
                });

        dismiss();
    }
}

