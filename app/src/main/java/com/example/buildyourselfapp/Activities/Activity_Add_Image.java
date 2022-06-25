package com.example.buildyourselfapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.buildyourself.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


public class Activity_Add_Image extends AppCompatActivity {

    private ImageView cover;
    private FloatingActionButton floatingActionButton;
    private StorageReference storageRef ;
    private Bitmap image_bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        cover = findViewById(R.id.cover);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        storageRef = FirebaseStorage.getInstance().getReference();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(Activity_Add_Image.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        cover.setImageURI(uri);
        Log.d("cover123", cover.toString());
        try {
            image_bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
          // image_bp = Bitmap.createScaledBitmap(image_bp, image_bp.getWidth(), image_bp.getHeight()
            //      , false);
           String sendImgToAPI =  getStringImage(image_bp);
            Log.d("image_bp8888", sendImgToAPI);
            cover.setImageBitmap(image_bp);
            upload_img_to_firebase( FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (Exception e) {
            Toast.makeText(Activity_Add_Image.this, "Failed!", Toast.LENGTH_LONG).show();
        }
    }
    public void upload_img_to_firebase( String uid) {
        byte[] img = bitmap_to_bytes();
        Log.d("byte55555", "upload_img_to_firebase: " + img );
        StorageReference file_ref = storageRef.child("Images/users/" + uid ).child(uid + ".jpg");
        file_ref.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Activity_Add_Image.this, "Image uploaded successfully!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Activity_Add_Image.this, "Failed to load image! Please try again!", Toast.LENGTH_LONG).show();
            }
        });
    }
    public byte[] bitmap_to_bytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image_bp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public void httpCall(String url) {

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // enjoy your response
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // enjoy your error status
            }


        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("image",cover.toString());
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

}