package com.ibrahim.javainstagram.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ibrahim.javainstagram.R;
import com.ibrahim.javainstagram.databinding.ActivityUploadBinding;
import com.ibrahim.javainstagram.model.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Upload extends AppCompatActivity {

    Uri imageData;
    private ActivityUploadBinding binding;
    Bitmap selectedimage;
    ArrayList<Post> postArrayList;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissonLauncher;

    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    //bunları herkesin yazdığı yazıları görmesi i,çin yapıuyoruz bir nevi sql
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();
        //bunu burda tanımlamassak çalışmaz,izin alamaz

        //depo işlmeleri////////////////////////////////////
        firebaseStorage=FirebaseStorage.getInstance();
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference=firebaseStorage.getReference();  //depoyu kontrol etmek
        ////////////////////////////////////////////////////
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    public void image(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)==(PackageManager.PERMISSION_GRANTED)){
            //eğer izin yoska
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                //izin almak içim snacbara gitcez
                Snackbar.make(view,"Permisson Needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ask permisson
                        permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }
                }).show();
            }
            else{
                //ask permisson
                permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

            }

        }else{
            Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
            //burada zaten izin vardır direk diğer sayfaya git diyor
        }
    }
    private void registerLauncher(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentfromresult=result.getData();
                    if(intentfromresult!=null){
                        imageData=intentfromresult.getData();
                        binding.imageView.setImageURI(imageData);
                        //binding i yazmasaydık imageview i alamazdık
                    }
                }
            }
        });
        permissonLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
            }
        });
    }
    public void yuklee(View view){
        if(imageData!=null){
            //universal uniqe id
            UUID uuid=UUID.randomUUID();
            String imageName="images/" + uuid + ".jpg";
            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //dowland url
                    StorageReference newReference=firebaseStorage.getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String dowlandUrl=uri.toString();
                            String commend=binding.textInputEditText.getText().toString();
                            FirebaseUser user=auth.getCurrentUser(); //bu işlem kullanıcıyı alır
                            String email=user.getEmail().toString();
                            //////////////////////////////////////////////////
                            HashMap<String,Object> postData= new HashMap<>();
                            postData.put("useremail",email);
                            postData.put("dowlandUrl",dowlandUrl);
                            postData.put("commend",commend);
                            postData.put("date", FieldValue.serverTimestamp());
                            ///////////////////////////////////////////////////
                            //yukarıdaki işlemleri çekmek için kullanırız
                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(Upload.this,"Basarili",Toast.LENGTH_LONG).show();

                                    Intent intent=new Intent(Upload.this, Feed.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Upload.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Upload.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }

    }

}