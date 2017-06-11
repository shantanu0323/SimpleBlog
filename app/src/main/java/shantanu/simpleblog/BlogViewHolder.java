package shantanu.simpleblog;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class BlogViewHolder extends RecyclerView.ViewHolder {

    View view;
    ImageButton bLike;
    CircularImageView bProfilePic;
    TextView tvUsername;

    FirebaseAuth mAuth;
    DatabaseReference mDatabaseLikes;

    public BlogViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        bLike = (ImageButton) view.findViewById(R.id.bLike);
        bProfilePic = (CircularImageView) view.findViewById(R.id.profilePic);
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);

    }

    public void setLikeButton(final String blogKey) {
        mDatabaseLikes.addValueEventListener(new ValueEventListener
                () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mAuth.getCurrentUser() != null) {
                    if (dataSnapshot.child(blogKey).hasChild(mAuth.getCurrentUser
                            ().getUid())) {
                        bLike.setImageResource(R.drawable.ic_like_blue);
                    } else {
                        bLike.setImageResource(R.drawable.ic_like_grey);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setTitle(String title) {
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }

    public void setDesc(String desc) {
        TextView tvDesc = (TextView) view.findViewById(R.id.tvDesc);
        tvDesc.setText(desc);
    }

    public void setUsername(String username) {
        TextView tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        tvUsername.setText(username);
    }

    public void setTime(String time) {
        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        tvTime.setText(time);
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

    public void setProfilePic(final Context context, final String profilePicUrl) {
        final ImageView profilePic = (ImageView) view.findViewById(R.id.profilePic);

        Picasso.with(context).load(profilePicUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profilePic,
                new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(profilePicUrl).into(profilePic);
                    }
                });
    }
}