package me.yurboirene.alicia_le;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import me.yurboirene.alicia_le.common.DatabaseHelper;

public class BoardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BOARD_ID_PARAM = "board_param";

    // TODO: Rename and change types of parameters
    private Long boardId;

    private OnFragmentInteractionListener mListener;

    FirebaseFirestore db;
    FirebaseAuth auth;

    FirebaseUser user;

    private boolean loading = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    RecyclerView postsRecyclerView;
    LinearLayoutManager mLayoutManager;

    PostRVAdapter adapter;

    private List<Post> posts;

    DocumentSnapshot lastDoc;

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            if(dy > 0) //check for scroll down
            {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                if (!loading)
                {
                    if ( (visibleItemCount + pastVisiblesItems + 10) >= totalItemCount)
                    {
                        loading = true;
                        postsRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());

                        db.collection("posts").whereEqualTo("boardid", boardId).orderBy("timestamp").limit(15).startAt(lastDoc.toObject(Post.class).getTimestamp()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                QuerySnapshot snapshot = task.getResult();
                                for (DocumentSnapshot document : snapshot) {
                                    Log.d(MainActivity.LOG_TAG, document.getId());
                                    Post post = document.toObject(Post.class);
                                    post.setUid(document.getId());
                                    adapter.add(post);
                                }
                                if (snapshot.getDocuments().size() > 0)
                                    lastDoc = snapshot.getDocuments().get(snapshot.getDocuments().size() - 1);

                                loading = false;
                            }
                        });
                    }
                }
            }
        }
    };

    public BoardFragment() {
        // Required empty public constructor
    }

    public static BoardFragment newInstance(Long boardId) {
        BoardFragment fragment = new BoardFragment();
        Bundle args = new Bundle();
        args.putLong(BOARD_ID_PARAM, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.boardId = getArguments().getLong(BOARD_ID_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        postsRecyclerView = getView().findViewById(R.id.postsRecyclerView);
        postsRecyclerView.addOnScrollListener(scrollListener);
        mLayoutManager = new LinearLayoutManager(getContext());
        postsRecyclerView.setLayoutManager(mLayoutManager);
        posts = new ArrayList<>();
        adapter = new PostRVAdapter(posts, getContext());
        postsRecyclerView.setAdapter(adapter);

        TextView mainHeader = getActivity().findViewById(R.id.mainHeaderText);
        TextView subHeader = getActivity().findViewById(R.id.mainSubText);
        try {
            mainHeader.setText(DatabaseHelper.getInstance().getBoard(boardId.intValue()).getName());
            subHeader.setText(DatabaseHelper.getInstance().getRegion(DatabaseHelper.getInstance().getBoard(boardId.intValue()).getRegionuid().intValue()).getName());
            MainScreenActivity.setCurrentRegion(String.valueOf(DatabaseHelper.getInstance().getBoard(boardId.intValue()).getRegionuid()));
        } catch (GettingDataException e) {
            subHeader.setText("Error!");
        }

        MainScreenActivity.setCurrentBoard(boardId.toString());

        updateData();
    }

    @Override
    public void onPause() {
        MainScreenActivity.setCurrentBoard(null);
        super.onPause();
    }

    private void updateData() {
        posts = new ArrayList<>();

        db.collection("posts").whereEqualTo("boardid", boardId).orderBy("timestamp").limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot snapshot = task.getResult();
                for (DocumentSnapshot document : snapshot) {
                    Post post = document.toObject(Post.class);
                    post.setUid(document.getId());
                    posts.add(post);
                    Log.d(MainActivity.LOG_TAG, String.valueOf(snapshot.getMetadata().isFromCache()));
                }
                adapter.setPosts(posts);
                if (snapshot.getDocuments().size() > 0)
                    lastDoc = snapshot.getDocuments().get(snapshot.getDocuments().size() - 1);
            }
        });

        postsRecyclerView.addOnScrollListener(scrollListener);
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
