package shantanu.simpleblog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton bAddPost;
    private RecyclerView blogList;
    private DatabaseReference mDatabase = null;
    private ProgressDialog progressDialog = null;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blogs");
        mDatabase.keepSynced(true);

        blogList.setHasFixedSize(true);
        blogList.setLayoutManager(new LinearLayoutManager(this));

        bAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), NewPost.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog.show();
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> adapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                if (flag) {
                    progressDialog.dismiss();
                    flag = false;
                }
            }
        };

        blogList.setAdapter(adapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;

        public BlogViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setTitle(String title) {
            TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvTitle.setText(title);
        }

        public void setDesc(String desc) {
            TextView tvDesc = (TextView) view.findViewById(R.id.tvDesc);
            tvDesc.setText(desc);
        }

        public void setImage(final Context context, final String imageUrl) {
            final ImageView image = (ImageView) view.findViewById(R.id.image);

            Picasso.with(context).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(imageUrl).into(image);
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            // Refresh the News Feeds
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        bAddPost = (FloatingActionButton) findViewById(R.id.bAddPost);
        blogList = (RecyclerView) findViewById(R.id.blogList);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
    }
}
