package shantanu.simpleblog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String username = getIntent().getStringExtra("username");
        if (username != null) {
            getSupportActionBar().setTitle(username);
        } else {
            getSupportActionBar().setTitle("Profile");
        }
    }
}
