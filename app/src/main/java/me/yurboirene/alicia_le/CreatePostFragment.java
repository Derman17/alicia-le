package me.yurboirene.alicia_le;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import me.yurboirene.alicia_le.common.DatabaseHelper;


public class CreatePostFragment extends Fragment {
    private static final String ARG_REGION_ID = "argregid";
    private static final String ARG_BOARD_ID = "argbid";

    // TODO: Rename and change types of parameters
    private String regionId;
    private String boardId;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private OnFragmentInteractionListener mListener;
    private TextView mainHeader, mainSub;

    public CreatePostFragment() {
        // Required empty public constructor
    }

 
    // TODO: Rename and change types and number of parameters
    public static CreatePostFragment newInstance(String regionId, String boardId) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REGION_ID, regionId);
        args.putString(ARG_BOARD_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            regionId = getArguments().getString(ARG_REGION_ID);
            boardId = getArguments().getString(ARG_BOARD_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        mainHeader = getActivity().findViewById(R.id.mainHeaderText);
        mainSub = getActivity().findViewById(R.id.mainSubText);

        mainHeader.setText("New Post");
        mainSub.setText("The Global Citizens");

        final TextView regionText = getActivity().findViewById(R.id.newPostRegionText);
        TextView boardText = getActivity().findViewById(R.id.newPostBoardText);

        try {
            db.collection("regions").document(regionId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    regionText.setText(String.format("Region: %s", task.getResult().getString("name")));
                }
            });
            boardText.setText(String.format("Board: %s", DatabaseHelper.getInstance().getBoard(Integer.valueOf(boardId)).getName()));
        } catch (GettingDataException e) {
            e.printStackTrace();
        }

        Button newPostPostButton = getActivity().findViewById(R.id.newPostPostButton);
        newPostPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPost();
            }
        });
    }

    public void createPost() {
        EditText newPostTitleEditText = getActivity().findViewById(R.id.newPostTitleEditText);
        EditText newPostBodyEditText = getActivity().findViewById(R.id.newPostBodyEditText);
        EditText newPostImageURLEditText = getActivity().findViewById(R.id.newPostImageURLEditText);

        try {
            DatabaseHelper.getInstance().createPost(newPostTitleEditText.getText().toString(),
                    newPostBodyEditText.getText().toString(),
                    newPostImageURLEditText.getText().toString(),
                    db.collection("regions").document(regionId),
                    Long.valueOf(boardId));
        } catch (CreatingPostException | GettingDataException e) {
            Toast.makeText(getContext(), "Wait a sec!", Toast.LENGTH_SHORT).show();
        }
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
