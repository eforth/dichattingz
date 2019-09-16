package com.vectorjm.dichattinz;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Uri> audioUri = new MutableLiveData<Uri>();

    public LiveData<Uri> getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(Uri uri) {
        audioUri.setValue(uri);
    }
}
