package com.notes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.notes.R;
import com.notes.adapter.NoteAdapter;
import com.notes.listener.NoteListener;
import com.notes.room.Note;
import com.notes.room.NotesDatabase;
import com.notes.utils.BaseActivity;
import com.notes.utils.CheckPermission;
import com.notes.utils.RealFilePathUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.notes.utils.CheckPermission.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class MainActivity extends BaseActivity implements View.OnClickListener, NoteListener
{
    private ImageView ivCreateNote;
    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTE = 3;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 4;
    private static final int REQUEST_CODE_SELECT_IMAGE = 5;


    private RecyclerView rvAllNotes;
    private List<Note> notesList = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private NoteListener noteListener;
    private int noteClickedPosition = -1;

    private AlertDialog dialogAddURL;


    String selectedImagePath;
    private ImageView ivAdd,ivImage,ivWebUrl;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK)
        {
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        }
        else if (requestCode==REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK)
        {
            if (data!=null)
            {
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
        else if (requestCode ==REQUEST_CODE_SELECT_IMAGE && resultCode ==RESULT_OK)
        {
            if (data!=null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    try {
                        selectedImagePath = RealFilePathUtil.getPath(getApplicationContext(),selectedImageUri);

                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","image");
                        intent.putExtra("imagePath",selectedImagePath);
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findId();
        setListener();


        EditText etSearch = findViewById(R.id.et_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (notesList.size()!=0)
                {
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });

    }

    @Override
    public void findId() {
        ivCreateNote = findViewById(R.id.iv_create_note);
        rvAllNotes = findViewById(R.id.rv_all_notes);



        ivAdd = findViewById(R.id.iv_add);
        ivImage = findViewById(R.id.iv_add_image);
        ivWebUrl = findViewById(R.id.iv_add_url);

        noteListener = MainActivity.this;



        rvAllNotes.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteAdapter = new NoteAdapter(notesList,MainActivity.this,noteListener);
        rvAllNotes.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTE,false);


    }

    @Override
    public void setListener() {
        ivCreateNote.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        ivImage.setOnClickListener(this);
        ivWebUrl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.iv_create_note:
                startActivityForResult(new Intent(getApplicationContext(),
                        CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);
                break;
            case R.id.iv_add:
                startActivityForResult(new Intent(getApplicationContext(),CreateNoteActivity.class),REQUEST_CODE_ADD_NOTE);
                break;
            case R.id.iv_add_image:
                if (CheckPermission.checkCameraPermission(getApplicationContext()))
                {
                    selectImage();
                    //openGallery();
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                                && checkSelfPermission( Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);

                        }
                        else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                                || shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO))
                        {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                        }

                        else
                        {
                            displayNeverAskAgainDialog();
                        }
                    }
                }


                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.
                            READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
                }
                else
                {
                    selectImage();
                }

                break;
            case R.id.iv_add_url:
                showAddURLDialog();
                break;
        }
    }

    private void displayNeverAskAgainDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage("We need to access camera and gallery for performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0)
        {
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                selectImage();
            }
            else
            {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void selectImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);

    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted)
    {
        class GetNoteTask extends AsyncTask<Void,Void, List<Note>>
        {

            @Override
            protected List<Note> doInBackground(Void... voids)
            {
                return NotesDatabase.
                        getDatabase(getApplicationContext()).noteDao().getAllNote();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);

                if (requestCode==REQUEST_CODE_SHOW_NOTE)
                {
                    notesList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                }

                else if (requestCode==REQUEST_CODE_ADD_NOTE)
                {
                    notesList.add(0,notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    rvAllNotes.smoothScrollToPosition(0);
                }
                else if (requestCode==REQUEST_CODE_UPDATE_NOTE)
                {
                    notesList.remove(noteClickedPosition);

                    if (isNoteDeleted)
                    {
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                    else
                    {
                        notesList.add(noteClickedPosition,notes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);

                    }

                }

            }

        }
        new GetNoteTask().execute();
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(MainActivity.this,CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);

        System.out.println("NNNNOTTT"+note);

        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }


    private void showAddURLDialog()
    {
        if (dialogAddURL==null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,(ViewGroup)findViewById(R.id.cl_add_url));
            builder.setView(view);
            dialogAddURL=builder.create();
            if (dialogAddURL.getWindow()!=null)
            {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.et_input_url);
            inputURL.requestFocus();

            view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty())
                    {
                        Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches())
                    {
                        Toast.makeText(MainActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {

                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","URL");
                        intent.putExtra("imagePath",inputURL.getText().toString());
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                        dialogAddURL.dismiss();
                    }
                }
            });

            view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
            dialogAddURL.show();
        }
    }

}