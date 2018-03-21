package me.yurboirene.alicia_le;

public class Board {

    private Long regionuid;
    private Long uid;
    private String name;
    private String type;

    public Board() {

    }

    public Board(Long regionuid, Long uid, String name, String type) {
        this.regionuid = regionuid;
        this.uid = uid;
        this.name = name;
        this.type = type;
    }

    public Long getRegionuid() {
        return regionuid;
    }

    public void setRegionuid(Long regionuid) {
        this.regionuid = regionuid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
