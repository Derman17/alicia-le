package me.yurboirene.alicia_le.common;


import java.util.List;

import me.yurboirene.alicia_le.Post;

public class Common {


    public static int getIdFromPostUid(List<Post> posts, String uid) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getUid().equals(uid))
                return i;
        }

        return -1;
    }
}
