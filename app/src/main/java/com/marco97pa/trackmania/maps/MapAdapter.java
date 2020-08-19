package com.marco97pa.trackmania.maps;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.utils.FLog;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/*
    Adapter of an ArrayList of Maps into a RecyclerView
 */
public class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {

    private static final String LOG_TAG = "MapAdapter";
    private FLog log = new FLog(LOG_TAG);

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView titleTextView;
        public ImageView imageView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.text_id);
            imageView = (ImageView) itemView.findViewById(R.id.image_id);
        }
    }

    // Store a member variable for the contacts
    private List<Map> mMaps;
    private Context context;

    // Pass in the contact array into the constructor
    public MapAdapter(List<Map> maps, Context context) {
        this.context = context;
        mMaps = maps;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public MapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_row_maps, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(MapAdapter.ViewHolder holder, final int position) {
        // Get the data model based on position
        final Map maps = mMaps.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.titleTextView;
        textView.setText(maps.getName());
        ImageView image = holder.imageView;
        if(maps.getThumbnailUrl().isEmpty()) {
            image.setImageResource(R.drawable.track);
        }else {
            Picasso.get().load(maps.getThumbnailUrl()).into(image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.d( "onClick: clicked on: " + maps);

                Intent intent = new Intent(context, MapDetailsActivity.class);
                intent.putExtra("map_name", maps.getName());
                intent.putExtra("map_url", maps.getFileUrl());
                intent.putExtra("map_image", maps.getThumbnailUrl());
                intent.putExtra("map_id", maps.getMapId());
                intent.putExtra("map_uid", maps.getMapUid());
                intent.putExtra("map_score_author", maps.getAuthorScore());
                intent.putExtra("map_score_gold", maps.getGoldScore());
                intent.putExtra("map_score_silver", maps.getSilverScore());
                intent.putExtra("map_score_bronze", maps.getBronzeScore());
                context.startActivity(intent);
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mMaps.size();
    }
}
