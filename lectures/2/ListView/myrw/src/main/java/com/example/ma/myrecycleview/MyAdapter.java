package com.example.ma.myrecycleview;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dan on 10/11/16.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.PhotoHolder> {
    private ArrayList<Photo> myPhotos;


    public MyAdapter(ArrayList<Photo> myPhotos) {
        Log.v("adapter", "MyAdapter");
        this.myPhotos = myPhotos;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v("adapter", "onCreateViewHolder b");
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_row, parent, false);
        Log.v("adapter", "onCreateViewHolder");
        return new PhotoHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        Photo itemPhoto = myPhotos.get(position);
        holder.bindPhoto(itemPhoto);
        Log.v("adapter", "onBindViewHolder");

    }

    @Override
    public int getItemCount() {
        Log.v("adapter", "getItemCount: " + myPhotos.size());
        return myPhotos.size();
    }

    public static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final String PHOTO_KEY = "PHOTO";

        private ImageView image;
        private TextView date;
        private TextView description;
        private Photo photo;

        private ProgressBar spinner;

        public PhotoHolder(View view) {
            super(view);
            Log.v("adapter", "PhotoHolder");
            image = (ImageView) view.findViewById(R.id.item_image);
            date = (TextView) view.findViewById(R.id.item_date);
            description = (TextView) view.findViewById(R.id.item_description);
            view.setOnClickListener(this);

            spinner = (ProgressBar) view.findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);

        }

        public void bindPhoto(Photo photo) {
            Log.v("adapter", "bindPhoto");
            this.photo = photo;
            Log.v("adapter", "load url: " + photo.getUrl());
            Picasso.with(image.getContext()).
                    load(photo.getUrl()).
                    error(R.drawable.error).into(image);
            date.setText(photo.getHumanDate());
            description.setText(photo.getExplanation());
        }

        @Override
        public void onClick(View v) {
            Log.v("photoHolder", "Clicked!");
            spinner.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Context context = itemView.getContext();
                    Intent showPhotoIntent = new Intent(context, MainActivity.class);
                    showPhotoIntent.putExtra(PHOTO_KEY, photo);
                    context.startActivity(showPhotoIntent);
                }
            }, 1000);
        }
    }

}
