package com.ibrahim.javainstagram.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ibrahim.javainstagram.databinding.RecyclerRowBinding;
import com.ibrahim.javainstagram.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/*
1. constructor
2. post holderu alt enter
3. postadapteri alt enter ve metodlşarı getir
 */


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    public PostAdapter(ArrayList<Post> postArrayList) {
        this.postArrayList = postArrayList;
    }

    private ArrayList<Post> postArrayList;


    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PostHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        holder.recyclerRowBinding.recyclerviewemailtext.setText(postArrayList.get(position).email);
        holder.recyclerRowBinding.recyclerviewcomment.setText(postArrayList.get(position).comment);

        Picasso.get().load(postArrayList.get(position).dowlandUrl).into(holder.recyclerRowBinding.recyclerviewimageview);
        //bu image yi getimek için kullanırız  , picassoyu unutma
    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder{

        RecyclerRowBinding recyclerRowBinding;

        public PostHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding=recyclerRowBinding;
            //bu işlemler klasik post holder işlemleri dir
        }
    }
}
