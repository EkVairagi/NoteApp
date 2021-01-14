package com.notes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.notes.R;
import com.notes.room.Note;
import com.notes.room.NoteDao;
import com.notes.room.NotesDatabase;
import com.notes.utils.CheckPermission;
import com.notes.utils.RealFilePathUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.notes.utils.CheckPermission.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class CreateNoteActivity extends AppCompatActivity implements View.OnClickListener
{
    private ImageView mBack,mSaveNote;
    private EditText mNoteTitle,mSubTitle,mCompleteNote;
    private TextView mDateTime;
    private String selectedNoteColor = "#333333";;
    private ImageView imageNote;
    private View viewSubtitileIndicator;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private String selectedImagePath;

    private TextView textWebURL;
    private LinearLayout llLayoutWebUrl;
    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;

    private Note isNoteAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        mBack = findViewById(R.id.iv_back);
        mNoteTitle  = findViewById(R.id.et_note_title);
        mSubTitle = findViewById(R.id.et_subtitle);
        mCompleteNote = findViewById(R.id.et_complete_note);
        mDateTime = findViewById(R.id.tv_dateTime);
        mSaveNote = findViewById(R.id.iv_save_note);
        viewSubtitileIndicator = findViewById(R.id.view_subtitile_indicator);
        imageNote = findViewById(R.id.imageNote);
        textWebURL = findViewById(R.id.tv_text_web_url);
        llLayoutWebUrl = findViewById(R.id.cl_add_url);
        llLayoutWebUrl = findViewById(R.id.ll_layout_web_url);

        mDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));

        setListeners();

        if (getIntent().getBooleanExtra("isFromQuickActions",false))
        {
            String type = getIntent().getStringExtra("quickActionType");
            if (type!=null)
            {
                if (type.equals("image"))
                {
                    selectedImagePath = getIntent().getStringExtra("imagePath");

                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
                }
                else if (type.equals("URL"))
                {
                    textWebURL.setText(getIntent().getStringExtra("URL"));
                    llLayoutWebUrl.setVisibility(View.VISIBLE);

                }
            }
        }


        selectedImagePath = "";




        if (getIntent().getBooleanExtra("isViewOrUpdate",false))
        {
            isNoteAvailable = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdate();
        }

        initMiscellaneous();

        findViewById(R.id.iv_remove_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebURL.setText(null);
                llLayoutWebUrl.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath="";
            }
        });

        setSubtitileIndicator();


    }


    private void setListeners()
    {
        mBack.setOnClickListener(this);
        mSaveNote.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.iv_save_note:
                saveNote();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    public void setViewOrUpdate()
    {
        mNoteTitle.setText(isNoteAvailable.getTitle());
        mSubTitle.setText(isNoteAvailable.getSubtitle());
        mCompleteNote.setText(isNoteAvailable.getNoteText());
        mDateTime.setText(isNoteAvailable.getDateTime());

        if (isNoteAvailable.getImagePath()!=null&&!isNoteAvailable.getImagePath().trim().isEmpty())
        {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(isNoteAvailable.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = isNoteAvailable.getImagePath();
        }

        if (isNoteAvailable.getWeblink()!=null && !isNoteAvailable.getWeblink().trim().isEmpty())
        {
            textWebURL.setText(isNoteAvailable.getWeblink());
            llLayoutWebUrl.setVisibility(View.VISIBLE);
        }

    }

    private void showDeleteNoteDialog()
    {
        if (dialogDeleteNote==null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, findViewById(R.id.cl_delete_note));
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow()!=null)
            {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.tv_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>
                    {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(isNoteAvailable);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });

            dialogDeleteNote.show();
        }
    }

    public void saveNote()
    {
        if (mNoteTitle.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Note title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (mSubTitle.getText().toString().trim().isEmpty() && mCompleteNote.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(mNoteTitle.getText().toString());
        note.setSubtitle(mSubTitle.getText().toString());
        note.setNoteText(mCompleteNote.getText().toString());
        note.setDateTime(mDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);

        if (llLayoutWebUrl.getVisibility()==View.VISIBLE)
        {
            note.setWeblink(textWebURL.getText().toString());
        }

        if (isNoteAvailable!=null)
        {
            note.setId(isNoteAvailable.getId());
        }


        class SaveNoteTask extends AsyncTask<Void,Void,Void>
        {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNode(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }

        new SaveNoteTask().execute();

    }

    private void initMiscellaneous()
    {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.ll_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.tv_miscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED)
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        ImageView imageColor1 = findViewById(R.id.image_color1);
        ImageView imageColor2 = findViewById(R.id.image_color2);
        ImageView imageColor3 = findViewById(R.id.image_color3);
        ImageView imageColor4 = findViewById(R.id.image_color4);
        ImageView imageColor5 = findViewById(R.id.image_color5);

        layoutMiscellaneous.findViewById(R.id.view_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitileIndicator();
            }
        });


        layoutMiscellaneous.findViewById(R.id.view_color1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitileIndicator();
            }
        });


        layoutMiscellaneous.findViewById(R.id.view_color1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitileIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitileIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitileIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitileIndicator();
            }
        });


        if (isNoteAvailable!=null && isNoteAvailable.getColor()!=null && !isNoteAvailable.getColor().trim().isEmpty())
        {
            switch (isNoteAvailable.getColor())
            {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.view_color1).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.view_color3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.view_color4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.view_color5).performClick();
                    break;
            }
        }


        layoutMiscellaneous.findViewById(R.id.ll_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


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
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,new String[]{Manifest.permission.
                            READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
                }
                else
                {
                    selectImage();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.ll_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

//        isNoteAvailable = (Note) getIntent().getSerializableExtra("note");

        if (isNoteAvailable!=null)
        {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();

                }
            });
            System.out.println("CHECK"+isNoteAvailable);

        }
        else
        {
//            Log.e("ELSEInsideISVIEWORUPDATE"," String.valueOf(isNoteAvailable)");


            System.out.println("CHECK"+isNoteAvailable);

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


    private void setSubtitileIndicator()
    {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitileIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_SELECT_IMAGE && resultCode==RESULT_OK)
        {
            if (data!=null) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);

                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

//                        selectedImagePath = getPathFromUri(selectedImagePath);

                        selectedImagePath = RealFilePathUtil.getPath(getApplicationContext(),selectedImageUri);

                        Log.e("SELECTED_IMAGE_PATH",selectedImagePath);

                        Log.e("SELECTED_IMAGE_URI",selectedImageUri.getPath());

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


/*
    private String getPathFromUri(Uri contentUri)
    {
        String filePath;
        Cursor cursor=null;
        cursor = getContentResolver().query(contentUri,null,null,null,null);

        if (cursor.moveToNext())
        {
            filePath = contentUri.getPath();
        }
        else
        {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
*/
private String getPathFromUri(String contentURI) {
    Uri contentUri = Uri.parse(contentURI);
    Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
    if (cursor == null) {
        return contentUri.getPath();
    } else {
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(index);
    }
}

    private void showAddURLDialog()
    {
        if (dialogAddURL==null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches())
                    {
                        Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        textWebURL.setText(inputURL.getText().toString());
                        llLayoutWebUrl.setVisibility(View.VISIBLE);
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