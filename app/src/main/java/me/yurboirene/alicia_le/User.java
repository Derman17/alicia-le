package me.yurboirene.alicia_le;

public class User {

    private String username;
    private String email;
    private String photoUrl;
    private Long postScore;
    private Long commentScore;

    public User() {

    }

    public User(String username, String email, String photoUrl, Long postScore, Long commentScore) {
        this.username = username;
        this.email = email;
        this.photoUrl = photoUrl;
        this.postScore = postScore;
        this.commentScore = commentScore;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getPostScore() {
        return postScore;
    }

    public void setPostScore(Long postScore) {
        this.postScore = postScore;
    }

    public Long getCommentScore() {
        return commentScore;
    }

    public void setCommentScore(Long commentScore) {
        this.commentScore = commentScore;
    }
}
