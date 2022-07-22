package com.example.buildyourselfapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buildyourself.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class Activity_Add_Image extends AppCompatActivity implements View.OnClickListener {

    private ImageView cover;
    private Button processImageButton, moveToPlanBtn;
    private FloatingActionButton floatingActionButton;
    private StorageReference storageRef;
    private Bitmap image_bp, afterImage = null;
    private String image_data;
    private Boolean enable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        cover = findViewById(R.id.cover);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        moveToPlanBtn = findViewById(R.id.moveToPlanBtn);
        processImageButton = findViewById(R.id.processImageBtn);

        processImageButton.setOnClickListener(this);
        moveToPlanBtn.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);

        processImageButton.setEnabled(enable);
        moveToPlanBtn.setEnabled(enable);

        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();

        try {
            image_bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            image_data = getStringImage(image_bp);

            cover.setImageBitmap(image_bp);
            upload_img_to_firebase(image_bp, FirebaseAuth.getInstance().getCurrentUser().getUid(), "before");
        } catch (Exception e) {
            Toast.makeText(Activity_Add_Image.this, "Failed!", Toast.LENGTH_LONG).show();
        }
    }

    public void upload_img_to_firebase(Bitmap image_bp, String uid, String status) {
        byte[] img = bitmap_to_bytes(image_bp);
        Log.d("byte55555", "upload_img_to_firebase: " + img);
        StorageReference file_ref = storageRef.child("Images/users/" + uid + "-" + status).child(uid + ".jpg");
        file_ref.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                processImageButton.setEnabled(!enable);
                Toast.makeText(Activity_Add_Image.this, "Image uploaded successfully!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Activity_Add_Image.this, "Failed to load image! Please try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public byte[] bitmap_to_bytes(Bitmap image_bp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image_bp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void getAfterImage(String data) throws JSONException, IOException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://build-yourself-backend.herokuapp.com/process";

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("img", data);

        RequestBody body = RequestBody.create(jsonParam.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    String imageBase64 = responseBody.string();

                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    afterImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //method which was problematic and was casing a problem
                            processImageButton.setEnabled(enable);
                            moveToPlanBtn.setEnabled(!enable);
                            cover.setImageBitmap(afterImage);
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floatingActionButton:
                ImagePicker.with(Activity_Add_Image.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
                break;
            case R.id.processImageBtn:
                try {
                    Log.i("After Image", "onClick: Why The Fuck I Am Here?!");
                    getAfterImage(image_data);
                    while (afterImage == null) ;
                    upload_img_to_firebase(afterImage, FirebaseAuth.getInstance().getCurrentUser().getUid(), "after");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
            case R.id.moveToPlanBtn:
                startActivity(new Intent(this, Activity_Plan.class));
        }
    }
}