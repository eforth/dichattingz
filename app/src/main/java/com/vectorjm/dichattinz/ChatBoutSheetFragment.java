package com.vectorjm.dichattinz;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChatBoutSheetFragment extends BottomSheetDialogFragment {

    Button textBtn, photoBtn, videoBtn;

    public ChatBoutSheetFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_bout_sheet, container, false);

        textBtn = v.findViewById(R.id.textBtn);
        photoBtn = v.findViewById(R.id.photoBtn);
        videoBtn = v.findViewById(R.id.videoBtn);

        v.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatBoutDialog dialog = new ChatBoutDialog();
                dialog.show(getActivity().getSupportFragmentManager(), null);
                dismiss();
            }
        });

        return v;
    }

}
