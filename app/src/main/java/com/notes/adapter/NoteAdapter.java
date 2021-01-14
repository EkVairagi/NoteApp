package com.notes.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.notes.R;
import com.notes.listener.NoteListener;
import com.notes.room.Note;
import com.notes.utils.SelectableRoundedImageView;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements NoteListener
{
    private List<Note> notes;
    private Context context;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> sourceNotes;


    public NoteAdapter(List<Note> notes, Context context,NoteListener noteListener) {
        this.notes = notes;
        this.context = context;
        this.noteListener = noteListener;
        sourceNotes = notes;
    }


    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_note,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListener.onNoteClicked(notes.get(position),position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onNoteClicked(Note note, int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView textTitle,textSubtitle,textDateTime;
        private LinearLayout layoutNote;
        private SelectableRoundedImageView srivImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textSubtitle = itemView.findViewById(R.id.text_subtitle);
            textDateTime = itemView.findViewById(R.id.text_date_time);
            layoutNote = itemView.findViewById(R.id.layout_note);
            srivImage = itemView.findViewById(R.id.sriv_image);
        }

        void setNote(Note note)
        {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty())
            {
                textSubtitle.setVisibility(View.GONE);
            }
            else
            {
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor()!=null)
            {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else
            {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getImagePath()!=null)
            {
                srivImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                srivImage.setVisibility(View.VISIBLE);
            }
            else
            {
                srivImage.setVisibility(View.GONE);
            }

        }

    }

    public void searchNotes(final String keywords)
    {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keywords.trim().isEmpty())
                {
                    notes = sourceNotes;
                }
                else
                {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note:sourceNotes)
                    {
                        if (note.getTitle().toLowerCase().contains(keywords.toLowerCase())||
                        note.getSubtitle().toLowerCase().contains(keywords.toLowerCase())||
                        note.getNoteText().toLowerCase().contains(keywords.toLowerCase()))
                        {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        },500);
    }

    public void cancelTimer()
    {
        if (timer!=null)
        {
            timer.cancel();
        }
    }


}
