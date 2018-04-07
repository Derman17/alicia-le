package me.yurboirene.alicia_le;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import me.yurboirene.alicia_le.common.DatabaseHelper;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POST_ID = "postid";

    // TODO: Rename and change types of parameters
    private String postId;

    private OnFragmentInteractionListener mListener;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    TextView mainHeader, bodyText, mainSub, karmaText;
    ImageButton upvoteButton, downvoteButton;
    Post post = null;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(String postId) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        mainHeader = getActivity().findViewById(R.id.mainHeaderText);
        mainSub = getActivity().findViewById(R.id.mainSubText);
        bodyText = getView().findViewById(R.id.bodyText);
        karmaText = getView().findViewById(R.id.postKarma);
        upvoteButton = getView().findViewById(R.id.upvoteButton);
        downvoteButton = getView().findViewById(R.id.downvoteButton);

        db.collection("posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                post = task.getResult().toObject(Post.class);
                post.setUid(task.getResult().getId());
                MainScreenActivity.setCurrentPost(post);
                mainHeader.setText(post.getTitle());
                mainSub.setText(String.format("By: %s", post.getOpUsername()));
                bodyText.setText(post.getBody());
                karmaText.setText(post.getScore().toString());
            }
        });

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DatabaseHelper.getInstance().upvoteTogglePost(db.collection("posts").document(postId)).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            post.setScore(task.getResult() ? post.getScore() + 1 : post.getScore() - 1);
                            karmaText.setTextColor(task.getResult() ? Color.RED : Color.GRAY);
                            karmaText.setText(task.getResult() ? String.valueOf(post.getScore()) : String.valueOf(post.getScore()));
                        }
                    });
                } catch (UpvotingPostException | NullPointerException e) {
                    Toast.makeText(getContext(), "Wait a sec!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
