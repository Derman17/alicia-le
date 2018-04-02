package me.yurboirene.alicia_le;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import me.yurboirene.alicia_le.common.DatabaseHelper;

public class BoardRVAdapter extends RecyclerView.Adapter<BoardRVAdapter.BoardViewHolder> {
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private List<Board> boards;
    private Context context;

    BoardRVAdapter(List<Board> boards, Context context) {
        this.boards = boards;
        this.context = context;
    }

    @Override
    public BoardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.board_item, viewGroup, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Board board = ((Board) view.getTag());

                FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.addToBackStack("tag");
                ft.replace(R.id.frameLayout, BoardFragment.newInstance(board.getUid())).commit();
            }
        });
        BoardViewHolder pvh = new BoardViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final BoardViewHolder postViewHolder, int i) {
        postViewHolder.setIsRecyclable(false);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        Board selectedBoard = boards.get(i);

        postViewHolder.cv.setTag(selectedBoard);
        postViewHolder.boardTitle.setText(selectedBoard.getName());
        try {
            postViewHolder.boardRegion.setText(DatabaseHelper.getInstance().getRegion(selectedBoard.getRegionuid().intValue()).getName());
        } catch (GettingDataException e) {
            postViewHolder.boardRegion.setText("sec");
        }
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
        notifyDataSetChanged();
    }

    public Board getBoardFromId(int id) {
        return boards.get(id);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void add(Board board) {
        for (Board boardd : boards) {
            if (board.getUid().equals(boardd.getUid())) {
                return;
            }
        }
        boards.add(board);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView boardTitle;
        TextView boardRegion;

        BoardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.boardCardView);
            boardTitle = itemView.findViewById(R.id.boardTitleText);
            boardRegion = itemView.findViewById(R.id.boardRegionText);
        }
    }
}
