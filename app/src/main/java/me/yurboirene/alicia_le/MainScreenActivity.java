package me.yurboirene.alicia_le;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import me.yurboirene.alicia_le.common.DatabaseHelper;

public class MainScreenActivity extends AppCompatActivity implements
        MainViewFragment.OnFragmentInteractionListener,
        BoardFragment.OnFragmentInteractionListener,
        PostFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.getResult().exists()) {
                    DatabaseHelper.getInstance().addUserToDB(user.getDisplayName(),
                            user.getEmail(),
                            user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString(),
                            user.getUid());
                }
            }
        });

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.frameLayout, new MainViewFragment()).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
