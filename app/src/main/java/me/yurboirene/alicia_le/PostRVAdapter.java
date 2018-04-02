package me.yurboirene.alicia_le;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import me.yurboirene.alicia_le.common.Common;
import me.yurboirene.alicia_le.common.DatabaseHelper;

public class PostRVAdapter extends RecyclerView.Adapter<PostRVAdapter.PostViewHolder> implements PostFragment.OnFragmentInteractionListener {
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private List<Post> posts;
    private Context context;

    PostRVAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_item, viewGroup, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Post post = ((Post) view.getTag());

                    FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    ft.addToBackStack("");
                    ft.replace(R.id.frameLayout, PostFragment.newInstance(post.getUid())).commit();
                }
            });
            PostViewHolder pvh = new PostViewHolder(v);
            return pvh;
    }

    @Override
    public void onBindViewHolder(final PostViewHolder postViewHolder, int i) {
        postViewHolder.setIsRecyclable(false);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        Post selectedPost = posts.get(i);

        postViewHolder.newsImage.setImageResource(R.drawable.googleg_disabled_color_18);
        if (!selectedPost.getPhotoURL().isEmpty())
            new DownloadImageTask(postViewHolder.newsImage).execute(selectedPost.getPhotoURL());
        postViewHolder.cv.setTag(selectedPost);
        postViewHolder.newsTitle.setText(selectedPost.getTitle());
        postViewHolder.newsOpUsername.setText(selectedPost.getOpUsername());
        postViewHolder.newsScore.setText(selectedPost.getScore().toString());
        postViewHolder.newsScore.setTag(selectedPost);
        postViewHolder.newsDesc.setText(selectedPost.getBody());
        //postViewHolder.upvoteButton.setTag(selectedPost);
        postViewHolder.newsScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DatabaseHelper.getInstance().upvoteTogglePost(db.collection("posts").document(((Post) view.getTag()).getUid())).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            Post post = (Post) postViewHolder.newsScore.getTag();
                            post.setScore(task.getResult() ? post.getScore() + 1 : post.getScore() - 1);
                            posts.set(Common.getIdFromPostUid(posts, ((Post) postViewHolder.newsScore.getTag()).getUid()), post);
                            postViewHolder.newsScore.setTextColor(task.getResult() ? Color.RED : Color.GRAY);
                            postViewHolder.newsScore.setText(task.getResult() ? String.valueOf(Long.valueOf(postViewHolder.newsScore.getText().toString()) + 1) :
                                    String.valueOf(Long.valueOf(postViewHolder.newsScore.getText().toString()) - 1));
                        }
                    });
                } catch (UpvotingPostException e) {
                    Toast toast = Toast.makeText(context, "Wait a sec!", Toast.LENGTH_SHORT);
                }
            }
        });

        db.collection("users").document(auth.getCurrentUser().getUid()).collection("upvotedPosts").orderBy("timestamp").limit(1000).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot snapshot = task.getResult();
                for (DocumentSnapshot document : snapshot) {
                    if (((Post) postViewHolder.newsScore.getTag()).getUid().equals(document.getId())) {
                        postViewHolder.newsScore.setTextColor(Color.RED);
                    }
                }
            }
        });
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public Post getPostFromId(int id) {
        return posts.get(id);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void add(Post post) {
        for (Post postt : posts) {
            if (post.getUid().equals(postt.getUid())) {
                return;
            }
        }
        posts.add(post);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView newsTitle;
        TextView newsOpUsername;
        TextView newsScore;
        TextView newsDesc;
        ImageView newsImage;

        PostViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.card_view);
            newsTitle = itemView.findViewById(R.id.news_title);
            newsOpUsername = itemView.findViewById(R.id.news_username);
            newsScore = itemView.findViewById(R.id.news_score);
            newsDesc = itemView.findViewById(R.id.news_desc);
            newsImage = itemView.findViewById(R.id.newsImageView);
        }
    }

}
