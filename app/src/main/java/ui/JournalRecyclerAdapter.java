package ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.selfjournal.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.viewHolder> {
    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, parent, false);

        return new viewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.viewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String url;

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThoughts());
        holder.name.setText(journal.getUserName());
        url = journal.getImageUrl();

        Picasso.get()
                .load(url)
                .placeholder(R.drawable.image_one)
                .fit()
                .into(holder.imageView);

//        https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal
                .getTimeAdded()
                .getSeconds() * 1000);
        holder.dateAdded.setText(timeAgo);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView title, thoughts, dateAdded, name;
        private ImageView imageView;
        private ImageButton shareButton;
        public viewHolder(@NonNull View itemView, Context ctx) { // context for if we need to go somewhere by clicking the row
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.tvJournalTitle);
            thoughts = itemView.findViewById(R.id.tvJournalThoughts);
            dateAdded = itemView.findViewById(R.id.tvJournalTimestamp);
            imageView = itemView.findViewById(R.id.iv_journal);
            name = itemView.findViewById(R.id.journal_row_username);
            shareButton = itemView.findViewById(R.id.row_shareButton);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String imageUrl = journalList.get(position).getImageUrl();

                    shareImage(imageUrl, ctx, position, journalList);
                }
            });
        }
    }

    static public void shareImage(String url, final Context context, final int position, final List<Journal>journalList) {

        Picasso.get().load(url).into(new Target() {
            @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "From Journal App");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Title: " + journalList.get(position).getTitle()
                        + "\n\n" + "Thought: " + journalList.get(position).getThoughts());

                shareIntent.setType("image/*");

                Uri imageUri = getImageUri(context , bitmap);

                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                context.startActivity(Intent.createChooser(shareIntent, "Send Image"));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
        });
    }
    static public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
