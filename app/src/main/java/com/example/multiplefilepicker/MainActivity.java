package com.example.multiplefilepicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class MainActivity extends AppCompatActivity {

    private final static int PICK_IMAGE=1;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final String TAG = "MainActivity";
    Button uploadImages, chooseImages, uploadoneImage;
    TextView imageAlert;
    TextInputLayout folderName;
    int upload_count=0;
    ArrayList<Uri> imagesList=new ArrayList<>();
    private Uri ImageUri;
    private ProgressDialog progressDialog;
    private boolean compressIsSuccessful=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageAlert=findViewById(R.id.textImageAlert);
        uploadImages=findViewById(R.id.imagesuploadbtn);
        uploadoneImage=findViewById(R.id.upload1imagebtn);
        chooseImages=findViewById(R.id.imageschoosebtn);
        folderName=findViewById(R.id.editfoldername);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
      //  coreHelper=new AnstronCoreHelper(this);

        chooseImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        uploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadMultipleImages();
            }
        });

    }


    private void UploadMultipleImages()
    {

        if (checkPermissionREAD_EXTERNAL_STORAGE(this))
        {

            String editFolderName = folderName.getEditText().getText().toString().trim();
            if (editFolderName.isEmpty()) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Please Enter a Folder Name", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        for (final Uri imageLists:imagesList)
                        {
                            final ContentResolver contentResolver=getContentResolver();

                            try {
                                Bitmap bitmap= MediaStore.Images.Media.getBitmap(contentResolver,imageLists);
                                OutputStream outputstream = null;
                                String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
                                File sdcard = Environment.getExternalStorageDirectory();
                                File directory = new File(sdcard + "/Compressor");
                                directory.mkdirs();
                                String fileName = time + "." + getFileExtension(imageLists);
                                File outfile = new File(directory, fileName);
                                try {
                                    if (!compressIsSuccessful==true) {
                                        outputstream = new FileOutputStream(outfile);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputstream);
                                        compressIsSuccessful=false;
                                        
                                        // Log.e(TAG, "run: Task Running Success fully",);
                                        Log.d(TAG, "run: Task Completed");
                                        Toast.makeText(MainActivity.this, "Compress Successful", Toast.LENGTH_SHORT).show();
                                    }
                                    outputstream.flush();
                                    outputstream.close();
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },5000);


            }
        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cr=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE){
            if (resultCode==RESULT_OK){
                if (data.getClipData()!=null){
                    int countClipData=data.getClipData().getItemCount();
                    int currentImageSelect=0;

                    while (currentImageSelect<countClipData){
                        ImageUri=data.getClipData().getItemAt(currentImageSelect).getUri();
                        imagesList.add(ImageUri);
                        currentImageSelect=currentImageSelect+1;

                    }
                    imageAlert.setVisibility(View.VISIBLE);
                    imageAlert.setText("You Have Selected "+imagesList.size()+" Images");
                    chooseImages.setVisibility(View.GONE);
                }
                else {
                    Toast.makeText(this, "Select multiple images", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }


    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(MainActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}