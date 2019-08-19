package com.vectorjm.dichattinz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.InputStream;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int CHATBOUT = 0, CHATBOUT_IMAGE = 1, CHATBOUT_PLAYABLE = 2;
    private List<ChatBout> chatBouts;
    private FirebaseUser user;
    private Context ctx;

    FeedAdapter(Context ctx, List<ChatBout> chatBouts) {
        this.chatBouts = chatBouts;
        this.ctx = ctx;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    @Override
    public int getItemCount() {
        return this.chatBouts.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatBouts.get(position).getType()) {
            case "ChatBoutPlayable":
                return 2;
            case "ChatBoutImage":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View v1;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case CHATBOUT_PLAYABLE:
                v1 = inflater.inflate(R.layout.viewholder_chatbout_playable, viewGroup, false);
                return new ChatBoutPlayableHolder(v1);
            case CHATBOUT_IMAGE:
                v1 = inflater.inflate(R.layout.viewholder_chatbout_image, viewGroup, false);
                return new ChartBoutImageHolder(v1);
            default:
                v1 = inflater.inflate(R.layout.viewholder_chatbout, viewGroup, false);
                return new ChatBoutHolder(v1);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case CHATBOUT_PLAYABLE:
                ChatBoutPlayableHolder chatBoutPlayableHolder = (ChatBoutPlayableHolder) viewHolder;
                configureHolder(chatBoutPlayableHolder, position);
                break;
            case CHATBOUT_IMAGE:
                ChartBoutImageHolder chartBoutImageHolder = (ChartBoutImageHolder) viewHolder;
                configureHolder(chartBoutImageHolder, position);
                break;
            default:
                ChatBoutHolder chatBoutHolder = (ChatBoutHolder) viewHolder;
                configureHolder(chatBoutHolder, position);
                break;
        }
    }

    private void configureHolder(ChatBoutHolder holder, int position) {

        ChatBout chatBout = chatBouts.get(position);

        holder.name.setText(user.getDisplayName().trim());
        holder.details.setText("" + chatBout.getDateCreated());
        holder.text.setText(chatBout.getText());

        // TODO: Load profile image
        new DownloadImageTask(holder.profile).execute("https://live.staticflickr.com/7890/46490289895_f1a6f8b0c9_b_d.jpg");
    }

    private void configureHolder(ChartBoutImageHolder holder, int position) {

        ChatBoutImage chatBoutImage = (ChatBoutImage) chatBouts.get(position);

        holder.name.setText(user.getDisplayName().trim());
        holder.details.setText("" + chatBoutImage.getDateCreated());
        holder.text.setText(chatBoutImage.getText());

        // TODO: Load profile image
        new DownloadImageTask(holder.profile).execute("https://live.staticflickr.com/7890/46490289895_f1a6f8b0c9_b_d.jpg");
        // TODO: Load photo
        new DownloadImageTask(holder.image).execute(chatBoutImage.getImageUrl());
    }

    private void configureHolder(ChatBoutPlayableHolder holder, int position) {
        ChatBoutPlayable chatBout = (ChatBoutPlayable) chatBouts.get(position);

        holder.name.setText(user.getDisplayName().trim());
        holder.details.setText("" + chatBout.getDateCreated());
        holder.text.setText(chatBout.getText());

        // TODO: Load profile image
        new DownloadImageTask(holder.profile).execute("https://live.staticflickr.com/7890/46490289895_f1a6f8b0c9_b_d.jpg");

        // TODO: Load media

        Uri vidoeUri = Uri.parse(chatBout.getPlayableUrl());

        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(Util.getUserAgent(ctx, ctx.getString(R.string.app_name)));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(vidoeUri);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(ctx);
        holder.playerView.setPlayer(player);
        player.prepare(mediaSource);
    }

    public static class ChartBoutImageHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView details;
        TextView text;
        CircularImageView profile;
        ImageView image;

        ChartBoutImageHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            details = view.findViewById(R.id.details);
            profile = view.findViewById(R.id.profile);
            image = view.findViewById(R.id.photo);
            text = view.findViewById(R.id.text);
        }
    }

    public static class ChatBoutHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView details;
        TextView text;
        CircularImageView profile;

        ChatBoutHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            details = view.findViewById(R.id.details);
            profile = view.findViewById(R.id.profile);
            text = view.findViewById(R.id.text);
        }
    }

    public static class ChatBoutPlayableHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView details;
        TextView text;
        PlayerView playerView;
        CircularImageView profile;

        ChatBoutPlayableHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            details = view.findViewById(R.id.details);
            profile = view.findViewById(R.id.profile);
            text = view.findViewById(R.id.text);
            playerView = view.findViewById(R.id.playerView);
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
