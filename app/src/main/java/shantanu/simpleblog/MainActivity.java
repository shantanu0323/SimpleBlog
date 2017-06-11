package shantanu.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton bAddPost;
    private RecyclerView blogList;

    private static final String TAG = "MainActivity";

    private DatabaseReference mDatabaseBlogs = null;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLikes;

    private ProgressDialog progressDialog = null;
    private boolean flag = true;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean likeButtonClicked = false;

    private String currentUsername;
    private String clickedUserId = "Profile";
    private boolean profileClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Log.e(TAG, "onAuthStateChanged: NO USER LOGGED IN");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mDatabaseBlogs = FirebaseDatabase.getInstance().getReference().child("Blogs");
        mDatabaseBlogs.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);

        blogList.setHasFixedSize(true);
        blogList.setLayoutManager(new LinearLayoutManager(this));

        bAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), NewPost.class);
                startActivity(intent);
            }
        });

        getCurrentUsername();

    }

    private void getCurrentUsername() {
        if (mAuth.getCurrentUser() != null) {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("name")) {
                                currentUsername = dataSnapshot.child("name").getValue().toString();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserExist();
        mAuth.addAuthStateListener(mAuthListener);
        progressDialog.show();
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> adapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabaseBlogs
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, final Blog model, final int position) {

                final String blogKey = getRef(position).getKey().toString();


                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setTime(model.getTime());
                viewHolder.setLikeButton(blogKey);
                viewHolder.setImage(getApplicationContext(), model.getImage());

                mDatabaseUsers.child(model.getUid()).child("image")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    viewHolder.setProfilePic(getApplicationContext(), dataSnapshot
                                            .getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                // Adding OnClickListener() to the entire Card
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                // OnClickListener() for the Like button
                viewHolder.bLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeButtonClicked = true;
                        Log.e(TAG, "onClick: Like button clicked");
                        mDatabaseLikes.addValueEventListener(new ValueEventListener
                                () {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (likeButtonClicked) {
                                    if (dataSnapshot.child(blogKey).hasChild(mAuth.getCurrentUser
                                            ().getUid())) {
                                        mDatabaseLikes.child(blogKey).child(mAuth.getCurrentUser
                                                ().getUid()).removeValue();
                                        likeButtonClicked = false;
                                    } else {
                                        mDatabaseLikes.child(blogKey).child(mAuth.getCurrentUser()
                                                .getUid()).setValue(currentUsername);
                                        likeButtonClicked = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                // OnClickListener() for the profile picture
                viewHolder.bProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openProfile(blogKey);
                    }
                });

                // OnClickListener() for the Username
                viewHolder.tvUsername.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openProfile(blogKey);
                    }
                });

            }
        };

        blogList.setAdapter(adapter);

        if (flag) {
            progressDialog.dismiss();
            flag = false;
        }
    }

    private void openProfile(String blogKey) {
        profileClicked = true;
        Log.e(TAG, "onClick: BLOG KEY : " + blogKey);
        mDatabaseBlogs.child(blogKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (profileClicked) {
                    if (dataSnapshot.hasChild("uid")) {
                        clickedUserId = dataSnapshot.child("uid").getValue()
                                .toString();
                        profileClicked = false;
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.putExtra("uid", clickedUserId);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {
                        Log.e(TAG, "onDataChange: NO SUCH CHILD : REDIRECTING TO SetupActivity");
                        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

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
        if (item.getItemId() == R.id.logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    private void init() {
        bAddPost = (FloatingActionButton) findViewById(R.id.bAddPost);
        blogList = (RecyclerView) findViewById(R.id.blogList);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: CAALLED");
        progressDialog.dismiss();
    }
}
