package me.yurboirene.alicia_le;

public class Region {

    private String type;
    private Integer timezone;
    private String name;
    private Long uid;

    public Region(){

    }

    public Region(String type, Integer timezone, String name, Long uid) {
        this.type = type;
        this.timezone = timezone;
        this.name = name;
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTimezone() {
        return timezone;
    }

    public void setTimezone(Integer timezone) {
        this.timezone = timezone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}
