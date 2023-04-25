package com.folioreader.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.ui.view.UnderlinedTextView;
import com.folioreader.util.AppUtil;
import com.folioreader.util.DataTypeConversionUtil;
import com.folioreader.util.UiUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * @author gautam chibde on 16/6/17.
 */

public class HighlightAdapter extends RecyclerView.Adapter<HighlightAdapter.HighlightHolder> {
    private List<HighlightImpl> highlights;
    private HighLightAdapterCallback callback;
    private Context context;
    private Config config;

    public HighlightAdapter(Context context, List<HighlightImpl> highlights, HighLightAdapterCallback callback, Config config) {
        this.context = context;
        this.highlights = highlights;
        this.callback = callback;
        this.config = config;
    }

    @Override
    public HighlightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HighlightHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_highlight, parent, false));
    }

    @Override
    public void onBindViewHolder(final HighlightHolder holder, final int position) {

        holder.container.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.container.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                    }
                });
            }
        }, 10);

        holder.content.setText(Html.fromHtml(getItem(position).getContent()));
        UiUtil.setBackColorToTextView(holder.content,
                getItem(position).getType());
        holder.date.setText(AppUtil.formatDate(getItem(position).getDate()));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(getItem(position));
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.deleteHighlight(getItem(position).getId(), getItem(position).getUUID());
                highlights.remove(position);
                notifyDataSetChanged();

            }
        });
        holder.editNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.editNote(getItem(position), position);
            }
        });
        if (getItem(position).getNote() != null) {
            if (getItem(position).getNote().isEmpty()) {
                holder.note.setVisibility(View.GONE);
                holder.illustration.setVisibility(View.GONE);
            } else {
                String curNote = getItem(position).getNote();
                if (curNote.length() > 5) {
                    if (curNote.substring(0, 5).compareTo("<img>") == 0) {
                        curNote = curNote.substring(5);
                        holder.note.setVisibility(View.GONE);
                        holder.illustration.setVisibility(View.VISIBLE);
                        final Bitmap bitmap = DataTypeConversionUtil.stringToBitmap(curNote);
                        holder.illustration.setImageBitmap(bitmap);

                        // View image on click
                        holder.illustration.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String mPath = context.getApplicationContext().getExternalFilesDir(null) + "/epubviewer/view.jpg";
                                File imageFile = new File(mPath);
                                if (!imageFile.exists())
                                    imageFile.getParentFile().mkdir();
                                try {
                                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                                    int quality = 100;
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                                    outputStream.flush();
                                    outputStream.close();

                                    Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", imageFile);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(uri, "image/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent);
                                } catch (Throwable e) {
                                    // Several error may come out with file handling or DOM
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        holder.note.setVisibility(View.VISIBLE);
                        holder.illustration.setVisibility(View.GONE);
                        holder.note.setText(curNote);
                    }
                }
                else {
                    holder.note.setVisibility(View.VISIBLE);
                    holder.illustration.setVisibility(View.GONE);
                    holder.note.setText(curNote);
                }
            }
        } else {
            holder.note.setVisibility(View.GONE);
            holder.illustration.setVisibility(View.GONE);
        }
        holder.container.postDelayed(new Runnable() {
            @Override
            public void run() {
                final int height = holder.container.getHeight();
                ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup.LayoutParams params =
                                holder.swipeLinearLayout.getLayoutParams();
                        params.height = height;
                        holder.swipeLinearLayout.setLayoutParams(params);
                    }
                });
            }
        }, 30);
        if (config.isNightMode()) {
            holder.container.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.note.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.content.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
        } else {
            holder.container.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.note.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.content.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
        }
    }

    private HighlightImpl getItem(int position) {
        return highlights.get(position);
    }

    @Override
    public int getItemCount() {
        return highlights.size();
    }

    public void editNote(String note, int position) {
        highlights.get(position).setNote(note);
        notifyDataSetChanged();
    }

    static class HighlightHolder extends RecyclerView.ViewHolder {
        private UnderlinedTextView content;
        private ImageView delete, editNote;
        private TextView date;
        private RelativeLayout container;
        private TextView note;
        private ImageView illustration;
        private LinearLayout swipeLinearLayout;

        HighlightHolder(View itemView) {
            super(itemView);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
            swipeLinearLayout = (LinearLayout) itemView.findViewById(R.id.swipe_linear_layout);
            content = (UnderlinedTextView) itemView.findViewById(R.id.utv_highlight_content);
            delete = (ImageView) itemView.findViewById(R.id.iv_delete);
            editNote = (ImageView) itemView.findViewById(R.id.iv_edit_note);
            date = (TextView) itemView.findViewById(R.id.tv_highlight_date);
            note = (TextView) itemView.findViewById(R.id.tv_note);
            illustration = (ImageView) itemView.findViewById(R.id.iv_note);
        }
    }

    public interface HighLightAdapterCallback {
        void onItemClick(HighlightImpl highlightImpl);

        void deleteHighlight(int id, String uuid);

        void editNote(HighlightImpl highlightImpl, int position);
    }
}