package me.yurboirene.alicia_le;


import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class Post {
    private String title;
    private String body;
    private Date timestamp;
    private String photoURL;
    private DocumentReference region;
    private Long boardid;
    private Long score;
    private DocumentReference op;
    private String opUsername;
    private String uid;

    public Post(String title, String body, Date timestamp, String photoURL, DocumentReference region, Long boardid, Long score, DocumentReference op, String opUsername) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.photoURL = photoURL;
        this.region = region;
        this.boardid = boardid;
        this.score = score;
        this.op = op;
        this.opUsername = opUsername;
        this.uid = uid;
    }

    public Post() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public DocumentReference getRegion() {
        return region;
    }

    public void setRegion(DocumentReference region) {
        this.region = region;
    }

    public Long getBoardid() {
        return boardid;
    }

    public void setBoardid(Long boardid) {
        this.boardid = boardid;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public DocumentReference getOp() {
        return op;
    }

    public void setOp(DocumentReference op) {
        this.op = op;
    }

    public String getOpUsername() {
        return opUsername;
    }

    public void setOpUsername(String opUsername) {
        this.opUsername = opUsername;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
