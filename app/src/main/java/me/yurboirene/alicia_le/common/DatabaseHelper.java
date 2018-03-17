package me.yurboirene.alicia_le.common;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.yurboirene.alicia_le.Post;
import me.yurboirene.alicia_le.Rank;
import me.yurboirene.alicia_le.User;

public class DatabaseHelper {

    private static DatabaseHelper myObj;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private CollectionReference postsReference;
    private DocumentReference userReference;
    private CollectionReference usersReference;
    private CollectionReference userRanksReference;

    private boolean upvotingPost, creatingPost, downvotingPost, upvotingComment;
    private SparseArray<Rank> userRanks;
    private boolean ranksCurrent;

    private DatabaseHelper(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        postsReference = db.collection("posts");
        usersReference = db.collection("users");
        userReference = usersReference.document(firebaseUser.getUid());
        userRanksReference = userReference.collection("ranks");

        userReference.collection("ranks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                userRanks = new SparseArray<>();
                List<DocumentSnapshot> documents = documentSnapshots.getDocuments();
                for (DocumentSnapshot document : documents) {
                    userRanks.put(Integer.valueOf(document.getId()), document.toObject(Rank.class));
                }
                ranksCurrent = true;
            }
        });
    }

    public static DatabaseHelper getInstance(){
        if(myObj == null){
            myObj = new DatabaseHelper();
        }
        return myObj;
    }

    // TODO add map or something with regions and their respective references.
    // TODO also add error codes.
    public Task<DocumentReference> createPost(final String title, final String body, final String photoURL, final DocumentReference region) {
        if (creatingPost || !ranksCurrent)
            return null;
        creatingPost = true;
        return region.get().continueWithTask(new Continuation<DocumentSnapshot, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                int postRegionId = Integer.valueOf(task.getResult().get("uid").toString());
                if (!getUserRanks().get(postRegionId).isMuted() && !getUserRanks().get(postRegionId).isBanned()) {
                    return rapidCreatePost(title, body, photoURL, region).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            creatingPost = false;
                        }
                    });
                } else {
                    return null;
                }
            }
        });
    }

    public Task<DocumentReference> rapidCreatePost(String title, String body, String photoURL, DocumentReference region) {
        Post post = new Post(title, body,
                new Date(), photoURL, region, 0L,
                db.collection("users").document(firebaseUser.getUid()),
                firebaseUser.getDisplayName());

        return postsReference.add(post);
    }

    public Task addUserToDB(String username, String email, String photoUrl, String uid) {

        User user = new User(username, email, photoUrl, 0L, 0L);
        return usersReference.document(uid).set(user);
    }

    public Task addRankToUser(DocumentReference userReference, Rank rank) {
        return userReference.collection("ranks").document(rank.getRegion().toString()).set(rank);
    }

    public Task addPostScoreToUser(final DocumentReference userReference, final Long score) {
        return db.runTransaction(new Transaction.Function<Long>() {
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot userSnapshot = transaction.get(userReference);
                Long newScore = userSnapshot.getLong("postScore") + score;
                transaction.update(userReference, "postScore", newScore);
                return newScore;
            }
        });
    }
    public Task addCommentScoreToUser(final DocumentReference userReference, final Long score) {
        return db.runTransaction(new Transaction.Function<Long>() {
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot userSnapshot = transaction.get(userReference);
                Long newScore = userSnapshot.getLong("commentScore") + score;
                transaction.update(userReference, "commentScore", newScore);
                return newScore;
            }
        });
    }

    public Task addScoreToPost(final DocumentReference postReference, final Long score) {
        return db.runTransaction(new Transaction.Function<Long>() {
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot userSnapshot = transaction.get(postReference);
                Long newScore = userSnapshot.getLong("score") + score;
                transaction.update(postReference, "score", newScore);
                return newScore;
            }
        });
    }

    public Task addPostToUserUpvoted(final DocumentReference post, final DocumentReference user) {
        return user.collection("upvotedPosts").document(post.getId()).get().continueWithTask(new Continuation<DocumentSnapshot, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                boolean alreadyUpvoted = task.getResult().exists();
                if (alreadyUpvoted) {
                    return null;
                } else {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("post", post);
                    data.put("timestamp", new Date());
                    return user.collection("upvotedPosts").document(post.getId()).set(data);
                }
            }
        });
    }

    public Task<Boolean> removePostToUserUpvoted(final DocumentReference post, final DocumentReference user) {
        return user.collection("upvotedPosts").document(post.getId()).get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                boolean alreadyUpvoted = task.getResult().exists();
                if (!alreadyUpvoted) {
                    return null;
                } else {
                    return post.get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
                        @Override
                        public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            final Post postObject = task.getResult().toObject(Post.class);
                            return addScoreToPost(post, -1L).continueWithTask(new Continuation<Void, Task<Boolean>>() {
                                @Override
                                public Task<Boolean> then(@NonNull Task task) throws Exception {
                                    return addPostScoreToUser(postObject.getOp(), -1L).continueWithTask(new Continuation<Void, Task<Boolean>>() {
                                        @Override
                                        public Task<Boolean> then(@NonNull Task task) throws Exception {
                                            return user.collection("upvotedPosts").document(post.getId()).delete().continueWith(new Continuation<Void, Boolean>() {
                                                @Override
                                                public Boolean then(@NonNull Task<Void> task) throws Exception {
                                                    return false;
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Boolean> task) {
                                                    upvotingPost = false;
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    // True if upvoted, false if unupvoted.
    public Task<Boolean> upvotePost(final DocumentReference post) {
        if (upvotingPost)
            return null;
        upvotingPost = true;
        return userReference.collection("upvotedPosts").document(post.getId()).get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
            @Override
            public Task then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                boolean alreadyUpvoted = task.getResult().exists();
                if (alreadyUpvoted) {
                    return removePostToUserUpvoted(post, userReference);
                } else {
                    return post.get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
                        @Override
                        public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            final Post postObject = task.getResult().toObject(Post.class);
                            return addScoreToPost(post, 1L).continueWithTask(new Continuation<Void, Task<Boolean>>() {
                                @Override
                                public Task<Boolean> then(@NonNull Task task) throws Exception {
                                    return addPostScoreToUser(postObject.getOp(), 1L).continueWithTask(new Continuation<Void, Task<Boolean>>() {
                                        @Override
                                        public Task<Boolean> then(@NonNull Task task) throws Exception {
                                            return addPostToUserUpvoted(post, postObject.getOp()).continueWith(new Continuation<Void, Boolean>() {
                                                @Override
                                                public Boolean then(@NonNull Task task) throws Exception {
                                                    return true;
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    upvotingPost = false;
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    public Task<Boolean> deletePost(final DocumentReference post) {
        if (!ranksCurrent)
            return null;

        return post.get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                Post postObject = task.getResult().toObject(Post.class);
                return postObject.getRegion().get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
                    @Override
                    public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        int postRegionId = Integer.valueOf(task.getResult().get("uid").toString());
                        if (getUserRanks().get(postRegionId).isCanDelete()) {
                            return post.delete().continueWith(new Continuation<Void, Boolean>() {
                                @Override
                                public Boolean then(@NonNull Task task) throws Exception {
                                    return true;
                                }
                            });
                        } else {
                            // Probably should grab the post again.
                            return post.get().continueWith(new Continuation<DocumentSnapshot, Boolean>() {
                                @Override
                                public Boolean then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                                    return false;
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    public SparseArray<Rank> getUserRanks() {
        return userRanks;
    }

    public Rank getUserRank(int regionId) {
        return getUserRanks().get(regionId);
    }

    public Task<Void> setUserRank(Rank rank) {
        if (rank.getRegion() != null)
            return userRanksReference.document(rank.getRegion().toString()).set(rank);
        else
            return null;
    }
}
