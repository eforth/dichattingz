package com.vectorjm.dichattinz;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class MainFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    static final int REQUEST_IMAGE_CAPTURE = 13;

    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private ImageView profileImage;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private SharedPreferences preferences;

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        auth = FirebaseAuth.getInstance();
        preferences = getContext()
                .getSharedPreferences("com.vectorjm.dichattinz", Context.MODE_PRIVATE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        BaseActivity baseActivity = ((BaseActivity) getActivity());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navigationView = view.findViewById(R.id.navigation);
        drawerLayout = view.findViewById(R.id.drawer);


        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.Open,
                R.string.Close);

        if (baseActivity == null) return;

        baseActivity.setSupportActionBar(toolbar);

        drawerLayout.addDrawerListener(drawerToggle);

        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        setupHeaderView(navigationView);

        loadScreen(null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.account:
                Toast.makeText(getActivity(), R.string.account, Toast.LENGTH_SHORT).show();
                loadScreen(null);
                break;
            case R.id.home:
                Toast.makeText(getActivity(), R.string.home, Toast.LENGTH_SHORT).show();
                loadScreen(null);
                break;
            case R.id.friends:
                Toast.makeText(getActivity(), R.string.friends, Toast.LENGTH_SHORT).show();
                loadScreen(null);
                break;
            case R.id.messaging:
                Toast.makeText(getActivity(), R.string.messaging, Toast.LENGTH_SHORT).show();
                loadScreen(null);
                break;
            case R.id.logout:
                logoutAction();
                break;
            default:
                return true;
        }

        navigationView.getMenu().getItem(0).setChecked(false);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadScreen(Fragment fragment) {

        if (fragment == null) fragment = new HomeFragment();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupHeaderView(NavigationView navigationView) {

        View header = navigationView.getHeaderView(0);
        profileImage = header.findViewById(R.id.nav_header_imageView);
        TextView headerName = header.findViewById(R.id.nav_header_textView);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        if (auth.getCurrentUser() == null) return;

        FirebaseUser user = auth.getCurrentUser();

        headerName.setText(user.getDisplayName());

        // TODO: Download profile image from Google Cloud Storage

        String uriString = preferences.getString(auth.getCurrentUser().getEmail(), null);

        if (uriString == null) return;

        ImageLoader.getInstance().displayImage(uriString, profileImage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(imageBitmap);
            uploadImage();
        }
    }

    private void uploadImage() {

        final StorageReference storageRef = storage.getReference().child("images/profile.jpg");

        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();

        Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
                Toast.makeText(getContext(), "Profile image failed to upload!", Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Profile image upload successful!", Toast.LENGTH_SHORT)
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
                    Log.i("URI", downloadUri.toString());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(auth.getCurrentUser().getEmail(), downloadUri.toString());
                    editor.commit();

                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    private void logoutAction() {
        if (getActivity() == null) return;

        auth.signOut();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.baseContainer, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}
