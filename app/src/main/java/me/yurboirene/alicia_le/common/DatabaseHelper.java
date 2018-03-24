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

import me.yurboirene.alicia_le.AlreadyUpvotedException;
import me.yurboirene.alicia_le.Board;
import me.yurboirene.alicia_le.CreatingPostException;
import me.yurboirene.alicia_le.GettingDataException;
import me.yurboirene.alicia_le.InsufficientPremissionsException;
import me.yurboirene.alicia_le.Post;
import me.yurboirene.alicia_le.Rank;
import me.yurboirene.alicia_le.Region;
import me.yurboirene.alicia_le.UpvotingPostException;
import me.yurboirene.alicia_le.User;

/**
 *
 * Class for interacting with Alicia FireBase.
 *
 * @author Rene Jacques
 *
 */
public class DatabaseHelper {

    private static DatabaseHelper myObj;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private CollectionReference postsReference;
    private DocumentReference currentUserReference;
    private CollectionReference usersReference;
    private CollectionReference userRanksReference;
    private CollectionReference regionsReference;
    private CollectionReference boardsReference;

    private boolean upvotingPost, creatingPost, downvotingPost, upvotingComment;
    private SparseArray<Rank> userRanks;
    private boolean ranksCurrent;
    private SparseArray<Region> regions;
    private boolean regionsCurrent;
    private SparseArray<Board> boards;
    private boolean boardsCurrent;


    private DatabaseHelper(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        postsReference = db.collection("posts");
        usersReference = db.collection("users");
        currentUserReference = usersReference.document(firebaseUser.getUid());
        userRanksReference = db.collection("ranks");
        regionsReference = db.collection("regions");
        boardsReference = db.collection("boards");

        // Set snapshot listener for the user's ranks
        userRanksReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                ranksCurrent = false;
                userRanks = new SparseArray<>();
                List<DocumentSnapshot> documents = documentSnapshots.getDocuments();
                for (DocumentSnapshot document : documents) {
                    userRanks.put(Integer.valueOf(document.getId()), document.toObject(Rank.class));
                }
                ranksCurrent = true;
            }
        });

        // Set snapshot listener for regions
        regionsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                regionsCurrent = false;
                regions = new SparseArray<>();
                List<DocumentSnapshot> documents = documentSnapshots.getDocuments();
                for (DocumentSnapshot document : documents) {
                    Region region = document.toObject(Region.class);
                    region.setUid(Long.getLong(document.getId()));
                    regions.put(Integer.valueOf(document.getId()), region);
                }
                regionsCurrent = true;
            }
        });

        // Set snapshot listener for boards
        boardsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                boardsCurrent = false;
                boards = new SparseArray<>();
                List<DocumentSnapshot> documents = documentSnapshots.getDocuments();
                for (DocumentSnapshot document : documents) {
                    Board board = document.toObject(Board.class);
                    board.setUid(Long.getLong(document.getId()));
                    boards.put(Integer.valueOf(document.getId()), document.toObject(Board.class));
                }
                boardsCurrent = true;
            }
        });
    }

    public static DatabaseHelper getInstance(){
        if(myObj == null){
            myObj = new DatabaseHelper();
        }
        return myObj;
    }
    
    /**
     * Creates a post in the submitted region with the currently signed in user as its poster.
     *
     * @param title           title of post
     * @param body            main meat of post
     * @param photoURL        URL for the image of a post
     * @param regionReference region where the post was created
     * @param boardReference  {@link DocumentReference} of the board that the post is in
     * @return                a {@link Task} that has the new post's
     *                        {@link DocumentReference} as a result
     *
     * @throws CreatingPostException if called while {@link DatabaseHelper} is already
     *                               creating a post
     * @throws GettingDataException if called before ranks are done updating
     */
    public Task<DocumentReference> createPost(final String title,
                                              final String body,
                                              final String photoURL,
                                              final DocumentReference regionReference,
                                              final DocumentReference boardReference)
            throws CreatingPostException, GettingDataException {

        // Check if currently creating post and/or if the ranks are current
        if (creatingPost)
            throw new CreatingPostException();
        if (!ranksCurrent)
            throw new GettingDataException();

        creatingPost = true;

        // Get the region that the post if going to submitted to
        return regionReference.get().continueWithTask(new Continuation<DocumentSnapshot, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                // Get the region uid
                int postRegionId = Integer.valueOf(task.getResult().get("uid").toString());
                // Check if user is muted and/or banned from the region
                if (!getUserRank(postRegionId).isMuted() && !getUserRank(postRegionId).isBanned()) {
                    // Create post with
                    return rapidCreatePost(title, body, photoURL, regionReference, boardReference).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            // Let everyone else know we aren't creating a post anymore
                            creatingPost = false;
                        }
                    });
                } else {
                    // They are banned and/or muted
                    if (getUserRank(postRegionId).isBanned())
                        throw new InsufficientPremissionsException("User is banned from this region");
                    else
                        throw new InsufficientPremissionsException("User is muted in this region");
                }
            }
        });
    }

    /**
     * Creates post without checking any permissions or if currently creating post. Has
     * some post info auto filled based on current user and current date.
     *
     * @param title           title of post
     * @param body            main meat of post
     * @param photoURL        URL of post photo/thumbnail
     * @param regionReference region where the post was created
     * @param boardReference  {@link DocumentReference} of the board that the post is in
     * @return                a {@link Task} that has the new post's
     *                        {@link DocumentReference} as a result
     */
    public Task<DocumentReference> rapidCreatePost(String title, String body, String photoURL, DocumentReference regionReference, DocumentReference boardReference) {
        return rapidCreatePost(title, body,
                new Date(), photoURL, regionReference, boardReference, 0L,
                db.collection("users").document(firebaseUser.getUid()),
                firebaseUser.getDisplayName());
    }

    /**
     * Creates post without checking any permissions or if currently creating post.
     *
     * @param title           title of post
     * @param body            main meat of post
     * @param date            date of post
     * @param photoURL        URL of post photo/thumbnail
     * @param regionReference {@link DocumentReference} of region post is in
     * @param boardReference  {@link DocumentReference} of the board where the post is in
     * @param score           score of post
     * @param userReference   {@link DocumentReference} of user the post is from
     * @param opUsername      {@link String} username of the poster
     * @return                a {@link Task} that has the new post's
     *                        {@link DocumentReference} as a result
     */
    public Task<DocumentReference> rapidCreatePost(String title, String body, Date date,
                                                   String photoURL,
                                                   DocumentReference regionReference,
                                                   DocumentReference boardReference, Long score,
                                                   DocumentReference userReference,
                                                   String opUsername) {
        Post post = new Post(title, body,
                date, photoURL, regionReference,
                boardReference, score,
                userReference,
                opUsername);

        // Add the new post to database
        return postsReference.add(post);
    }

    /**
     * Adds a new user to the database.
     *
     * @param username username of new user
     * @param email    email of user
     * @param photoUrl URL of user's profile picture
     * @param uid      unique id of user
     * @return         {@link Task} that as the new user's
     *                 {@link DocumentReference} as a result
     */
    public Task<DocumentReference> addUserToDB(String username, String email, String photoUrl, final String uid) {
        // Create the new user object
        User user = new User(username, email, photoUrl, 0L, 0L);

        // Add the user to the database (or overwrites an old one)
        return usersReference.document(uid).set(user).continueWith(new Continuation<Void, DocumentReference>() {
            @Override
            public DocumentReference then(@NonNull Task<Void> task) throws Exception {
                // Returns user reference
                return usersReference.document(uid);
            }
        });
    }

    /**
     * Adds (or overwrites) a rank to a user.
     *
     * @param userReference {@link DocumentReference} of user
     * @param rank          {@link Task} to change the user to
     * @return              {@link Task} for the operation that has no result
     */
    public Task addRankToUser(DocumentReference userReference, Rank rank) {
        return userReference.collection("ranks").document(rank.getRegion().toString()).set(rank);
    }

    /**
     * Adds a set score to a user's post score.
     *
     * @param userReference {@link DocumentReference} of user
     * @param score         amount of score to add to user
     * @return              {@link Task} that's result is the users total post score
     */
    public Task<Long> addPostScoreToUser(final DocumentReference userReference, final Long score) {
        // Runs a "transaction" that adds a score to the user
        return db.runTransaction(new Transaction.Function<Long>() {
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                // Gets snapshot of user
                DocumentSnapshot userSnapshot = transaction.get(userReference);

                // Adds the user's old score and adds the provided score to it
                Long newScore = userSnapshot.getLong("postScore") + score;

                // Updates user's score
                transaction.update(userReference, "postScore", newScore);
                return newScore;
            }
        });
    }

    /**
     * Adds a set score to a user's comment score.
     *
     * @param userReference {@link DocumentReference} of user
     * @param score         amount of score to add to user
     * @return              {@link Task} that's result is the user's total comment score
     */
    public Task addCommentScoreToUser(final DocumentReference userReference, final Long score) {
        // See "addPostScoreToUser".
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

    /**
     * Adds a set score to a post.
     *
     * @param postReference {@link DocumentReference} of post
     * @param score         amount of score to add to post
     * @return              {@link Task} that's result is the post's total score
     */
    public Task<Long> addScoreToPost(final DocumentReference postReference, final Long score) {
        // See "addPostScoreToUser".
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

    /**
     * Adds a post to a user's upvoted posts.
     *
     * @param postReference {@link DocumentReference} of post
     * @param userReference {@link DocumentReference} of user
     * @return              {@link Task} of operation with no result
     */
    public Task<Void> addPostToUserUpvoted(final DocumentReference postReference, final DocumentReference userReference) {
        // Gets the provided post from the user's already upvoted posts
        return userReference.collection("upvotedPosts").document(postReference.getId()).get().continueWithTask(new Continuation<DocumentSnapshot, Task<Void>>() {
            @Override
            public Task then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                // Checks if the provided post is there
                boolean alreadyUpvoted = task.getResult().exists();

                if (alreadyUpvoted) {
                    throw new AlreadyUpvotedException();
                } else {
                    // Not currently upvoted

                    // Creates a HashMap containing a new entry to add to the user's upvoted posts
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("post", postReference);
                    data.put("timestamp", new Date());

                    // Add the post to the user's upvoted posts
                    return userReference.collection("upvotedPosts").document(postReference.getId()).set(data);
                }
            }
        });
    }

    /**
     * Removes post from a user's upvoted posts and subtracts score from post and
     * user's post score.
     *
     * @param postReference {@link DocumentReference} to post
     * @param userReference {@link DocumentReference} to user
     * @return              {@link Task} with a result that's true if the post was
     *                      not upvoted (so nothing was done) and false if the post
     *                      was already upvoted (and is now not)
     */

    public Task<Boolean> removePostToUserUpvoted(final DocumentReference postReference, final DocumentReference userReference) {
        // Gets post from user's upvoted posts
        return userReference.collection("upvotedPosts").document(postReference.getId()).get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                // Checks if it was upvoted
                boolean alreadyUpvoted = task.getResult().exists();

                if (!alreadyUpvoted) {
                    // If not, return true

                    // I don't know how to make just a new task with a boolean result
                    // so this is my solution
                    return postReference.get().continueWith(new Continuation<DocumentSnapshot, Boolean>() {
                        @Override
                        public Boolean then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            return true;
                        }
                    });
                } else {
                    // Post was already upvoted

                    // Get post
                    return postReference.get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
                        @Override
                        public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            // Creates post object
                            final Post postObject = task.getResult().toObject(Post.class);

                            // Subtracts 1 score from post
                            return addScoreToPost(postReference, -1L).continueWithTask(new Continuation<Long, Task<Boolean>>() {
                                @Override
                                public Task<Boolean> then(@NonNull Task task) throws Exception {
                                    // Subtracts 1 score from user's post score
                                    return addPostScoreToUser(postObject.getOp(), -1L).continueWithTask(new Continuation<Long, Task<Boolean>>() {
                                        @Override
                                        public Task<Boolean> then(@NonNull Task task) throws Exception {
                                            // Deletes post from user's upvoted posts
                                            return userReference.collection("upvotedPosts").document(postReference.getId()).delete().continueWith(new Continuation<Void, Boolean>() {
                                                @Override
                                                public Boolean then(@NonNull Task<Void> task) throws Exception {
                                                    return false;
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

    /**
     * Toggles a whether the user has upvoted the post. Also handles adding or subtracting score
     * from upvoted post.
     *
     * @param postReference {@link DocumentReference} of post
     * @return              {@link Task} with result that is true if upvoted, false if unupvoted
     */
    public Task<Boolean> upvoteTogglePost(final DocumentReference postReference) throws UpvotingPostException {
        // Checks if currently upvoting post (prevents spamming upvote and breaking things)
        if (upvotingPost)
            throw new UpvotingPostException();

        upvotingPost = true;
        // Get post from user's upvoted posts
        return currentUserReference.collection("upvotedPosts").document(postReference.getId()).get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
            @Override
            public Task then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                boolean alreadyUpvoted = task.getResult().exists();

                // Check if already upvoted
                if (alreadyUpvoted) {
                    // If so, remove it from user's upvoted posts
                    return removePostToUserUpvoted(postReference, currentUserReference);
                } else {
                    // Get post
                    return postReference.get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
                        @Override
                        public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            // Get post as object
                            final Post postObject = task.getResult().toObject(Post.class);
                            // Add one score to post
                            return addScoreToPost(postReference, 1L).continueWithTask(new Continuation<Long, Task<Boolean>>() {
                                @Override
                                public Task<Boolean> then(@NonNull Task task) throws Exception {
                                    // Add one score to user's post score
                                    return addPostScoreToUser(postObject.getOp(), 1L).continueWithTask(new Continuation<Long, Task<Boolean>>() {
                                        @Override
                                        public Task<Boolean> then(@NonNull Task<Long> task) throws Exception {
                                            // Add post to user's upvoted posts
                                            return addPostToUserUpvoted(postReference, postObject.getOp()).continueWith(new Continuation<Void, Boolean>() {
                                                @Override
                                                public Boolean then(@NonNull Task task) throws Exception {
                                                    // Not upvoting post anymore
                                                    upvotingPost = false;
                                                    return true;
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

    /**
     * Deletes post. Does not remove it from user's upvoted posts nor does it
     * delete the score from the poster's post score.
     *
     * @param postReference {@link DocumentReference} of post
     * @return              {@link Task} with result that's true if the post was deleted, false
     *                      if it doesn't exist
     */
    public Task<Boolean> deletePost(final DocumentReference postReference) throws GettingDataException, InsufficientPremissionsException {
        if (!ranksCurrent)
            throw new GettingDataException();

        // Gets post
        return postReference.get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (!task.getResult().exists()) {
                    // Post doesn't exist
                    return postReference.get().continueWith(new Continuation<DocumentSnapshot, Boolean>() {
                        @Override
                        public Boolean then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            return false;
                        }
                    });
                } else {

                    // Gets post object
                    Post postObject = task.getResult().toObject(Post.class);

                    // Get post's region
                    return postObject.getRegion().get().continueWithTask(new Continuation<DocumentSnapshot, Task<Boolean>>() {
                        @Override
                        public Task<Boolean> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                            // Get post's region's uid
                            int postRegionId = Integer.valueOf(task.getResult().get("uid").toString());
                            // Checks if user has the permissions to delete posts in this region
                            if (getUserRank(postRegionId).isCanDelete()) {
                                // Deletes post
                                return postReference.delete().continueWith(new Continuation<Void, Boolean>() {
                                    @Override
                                    public Boolean then(@NonNull Task task) throws Exception {
                                        return true;
                                    }
                                });
                            } else {
                                throw new InsufficientPremissionsException("User does not have " +
                                        "permission to delete posts in this region");
                            }
                        }
                    });
                }
            }
        });

    }

    /**
     * Gets user's {@link Rank}s as {@link SparseArray}
     *
     * @return {@link SparseArray} of {@link Rank}s that the user has with the keys being
     *         the uid of the region the {@link Rank} applies to
     */
    public SparseArray<Rank> getUserRanks() {
        return userRanks;
    }

    /**
     * Gets {@link Rank} from region uid
     *
     * @param regionId uid of region the {@link Rank} is in
     * @return         {@link Rank} the corresponds the region uid provided
     */
    public Rank getUserRank(int regionId) {
        return getUserRanks().get(regionId);
    }
}
