package me.yurboirene.alicia_le;

public class Rank {

    private boolean canEdit, canDelete, canPost, canMute, canPromote, canVote, canComment, canBan;
    private boolean isMuted, isBanned;

    private Integer region;

    public Rank() {

    }

    public Rank(boolean canEdit, boolean canDelete, boolean canPost, boolean canMute,
                boolean canPromote, boolean canVote, boolean canComment, boolean canBan,
                boolean isMuted, boolean isBanned, Integer region) {
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canPost = canPost;
        this.canMute = canMute;
        this.canPromote = canPromote;
        this.canVote = canVote;
        this.canComment = canComment;
        this.canBan = canBan;
        this.isMuted = isMuted;
        this.isBanned = isBanned;
        this.region = region;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanPost() {
        return canPost;
    }

    public void setCanPost(boolean canPost) {
        this.canPost = canPost;
    }

    public boolean isCanMute() {
        return canMute;
    }

    public void setCanMute(boolean canMute) {
        this.canMute = canMute;
    }

    public boolean isCanPromote() {
        return canPromote;
    }

    public void setCanPromote(boolean canPromote) {
        this.canPromote = canPromote;
    }

    public boolean isCanVote() {
        return canVote;
    }

    public void setCanVote(boolean canVote) {
        this.canVote = canVote;
    }

    public boolean isCanComment() {
        return canComment;
    }

    public void setCanComment(boolean canComment) {
        this.canComment = canComment;
    }

    public boolean isCanBan() {
        return canBan;
    }

    public void setCanBan(boolean canBan) {
        this.canBan = canBan;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    public Integer getRegion() {
        return region;
    }

    public void setRegion(Integer region) {
        this.region = region;
    }
}
