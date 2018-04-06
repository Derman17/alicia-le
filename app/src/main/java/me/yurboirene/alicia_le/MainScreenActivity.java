package me.yurboirene.alicia_le;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        PostFragment.OnFragmentInteractionListener,
        CreatePostFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private static Post currentPost;
    private static String currentBoard;
    private static String currentRegion;

    FirebaseFirestore db;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        db = FirebaseFirestore.getInstance();

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

        DatabaseHelper.getInstance();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.frameLayout, new MainViewFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.option_delete:
                if (currentPost != null) {
                    try {
                        DatabaseHelper.getInstance().deletePost(db.collection("posts").document(currentPost.getUid())).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                boolean successful = task.getResult();
                                if (successful) {
                                    onBackPressed();
                                    Toast.makeText(getApplicationContext(), "Deletion successful!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (InsufficientPermissionsException e) {
                        Toast.makeText(getApplicationContext(), "You don't have permission to do that!", Toast.LENGTH_SHORT).show();
                    } catch (GettingDataException e) {
                        Toast.makeText(getApplicationContext(), "Wait a sec!", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            case R.id.option_new_post:
                if (currentRegion != null && currentBoard != null) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction ft = manager.beginTransaction();
                    ft.addToBackStack(null);
                    ft.replace(R.id.frameLayout, CreatePostFragment.newInstance(currentRegion, currentBoard))
                            .commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public static Post getCurrentPost() {
        return currentPost;
    }

    public static void setCurrentPost(Post currentPost) {
        MainScreenActivity.currentPost = currentPost;
    }

    public static String getCurrentBoard() {
        return currentBoard;
    }

    public static void setCurrentBoard(String currentBoard) {
        MainScreenActivity.currentBoard = currentBoard;
    }

    public static String getCurrentRegion() {
        return currentRegion;
    }

    public static void setCurrentRegion(String currentRegion) {
        MainScreenActivity.currentRegion = currentRegion;
    }
}
