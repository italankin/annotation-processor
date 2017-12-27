package com.italankin.mvp.example.ui.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.italankin.mvp.example.R;
import com.squareup.picasso.Picasso;

import java.util.List;

class PhotosAdapter extends RecyclerView.Adapter<PhotoViewHolder> {
    private final LayoutInflater inflater;
    private final Context context;
    private final List<PhotoModelView> dataset;
    private Listener listener;

    public PhotosAdapter(Context context, List<PhotoModelView> dataset) {
        setHasStableIds(true);
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.dataset = dataset;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_photo, parent, false);
        PhotoViewHolder holder = new PhotoViewHolder(view);
        view.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onPhotoClick(dataset.get(pos), pos);
                }
            }

        });
        return holder;
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        PhotoModelView modelView = dataset.get(position);
        holder.textViews.setText(modelView.viewsCount);
        Picasso.with(context)
                .load(modelView.url)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder_photo)
                .into(holder.imagePhoto);
    }

    @Override
    public long getItemId(int position) {
        return dataset.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public interface Listener {
        void onPhotoClick(PhotoModelView modelView, int position);
    }
}

class PhotoViewHolder extends RecyclerView.ViewHolder {
    final ImageView imagePhoto;
    final TextView textViews;

    PhotoViewHolder(View itemView) {
        super(itemView);
        imagePhoto = itemView.findViewById(R.id.image_photo);
        textViews = itemView.findViewById(R.id.text_views);
    }
}
