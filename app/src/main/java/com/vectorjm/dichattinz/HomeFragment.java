package com.vectorjm.dichattinz;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<ChatBout> chatBouts = new ArrayList<>();
    private FeedAdapter adapter;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_chatbout, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            // TODO: Launch the ChatBout Sheet
            showChatBoutAddSheet();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView = view.findViewById(R.id.feedList);

        adapter = new FeedAdapter(getActivity(), chatBouts);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        loadData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity baseActivity = ((BaseActivity) getActivity());

        if (baseActivity == null) return;

        if (baseActivity.getSupportActionBar() == null) return;

        baseActivity.setTitle("Feed");
        baseActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadData() {

        chatBouts.clear();

        String[] imageUrls = new String[] {
                "https://live.staticflickr.com/6031/6328081652_c020799cf9_o_d.jpg",
                "https://live.staticflickr.com/8511/8490855333_8910327404_b_d.jpg",
                "https://live.staticflickr.com/4812/46682249304_de758b81d0_b_d.jpg",
                "https://live.staticflickr.com/3063/3027226153_842214cfc2_o_d.jpg",
                "https://live.staticflickr.com/4820/33529545498_d6cee0287d_b_d.jpg",
                "https://live.staticflickr.com/7890/46490289895_f1a6f8b0c9_b_d.jpg"
        };

        String[] playableUrls = new String[] {
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
        };

        String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim";

        for (int i = 0; i < 10; i++) {

            ChatBout chatBout;

            int random = (int)(Math.random() * 6);
            int r = (int)(Math.random() * 3);

            if (i % 2 == 0 && i % 3 != 0) {
                chatBout = new ChatBoutImage("" + i, "" + i, 454965L, message,
                        imageUrls[random]);
            } else if (i % 3 == 0) {
                chatBout = new ChatBoutPlayable("" + i, "" + i, 454965L, message,
                        playableUrls[r]);
            } else {
                chatBout = new ChatBout("" + i, "" + i, 59458L, message);
            }

            chatBouts.add(chatBout);
            adapter.notifyDataSetChanged();
        }
    }

    private void showChatBoutAddSheet() {
        ChatBoutSheetFragment sheetDialog = new ChatBoutSheetFragment();
        sheetDialog.show(getActivity().getSupportFragmentManager(), null);
    }
}
