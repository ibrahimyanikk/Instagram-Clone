package com.ibrahim.javainstagram.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ibrahim.javainstagram.R;
import com.ibrahim.javainstagram.adapter.PostAdapter;
import com.ibrahim.javainstagram.databinding.ActivityFeedBinding;
import com.ibrahim.javainstagram.model.Post;

import java.util.ArrayList;
import java.util.Map;

import javax.security.auth.Destroyable;

public class Feed extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    private ActivityFeedBinding binding;

    PostAdapter postAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        postArrayList=new ArrayList<>();

        getData();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter=new PostAdapter(postArrayList);
        binding.recyclerView.setAdapter(postAdapter);

    }
    private void getData(){
        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            //yukarıdaki orderby e dikkat aynı şekilde where li şekilleri de var tarihi order by ile yaptık
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Toast.makeText(Feed.this,error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(value!=null){
                    for(DocumentSnapshot snapshot:value.getDocuments()){  //tek tek gezcek snapcahht a kaydetcek
                        Map<String,Object> data=snapshot.getData();

                        //casting
                        String userEmail=(String) data.get("useremail");//firebse deki aynı adı yaz
                        String comment=(String) data.get("commend");
                        String dowlandUrl=(String) data.get("dowlandUrl");

                        Post post=new Post(userEmail,dowlandUrl,comment);
                        postArrayList.add(post);//post u ekledik array liste
                    }
                    postAdapter.notifyDataSetChanged();
                    //yeni elemna geldiğinde göster
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.addPost){
            Intent intent1=new Intent(Feed.this, Upload.class);
            startActivity(intent1);
        } else if(item.getItemId()==R.id.logout) {

            auth.signOut();
            Intent intent2=new Intent(Feed.this, MainActivity.class);
            startActivity(intent2);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

}