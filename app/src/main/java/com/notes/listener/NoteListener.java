package com.notes.listener;

import com.notes.room.Note;

public interface NoteListener
{
    void onNoteClicked(Note note,int position);
}
