package com.androidsalad.popcorntvapp.Util;

import android.text.TextUtils;

public class Constants {


    //duration of wait of splash screen:
    public final static int SPLASH_DISPLAY_LENGTH = 2000;


    //return trimmed string:
    public static String trim(String name) {

        return name.replace(" ", "");
    }

    //return trimmed string:
    public static int getPostViews(String postViews) {

        if (TextUtils.isEmpty(postViews)) {
            return 0;
        } else {
            return Integer.parseInt(postViews);
        }
    }


    //firebase database & storage constants:
    public final static String FIREBASE_CELEB_DATABASE = "celebs";
    public final static String FIREBASE_POST_DATABASE = "posts";
    public final static String FIREBASE_PHOTO_DATABASE = "photos";
    public final static String FIREBASE_CELEB_POSTS_DATABASE = "celeb_posts";
    public final static String FIREBASE_POST_PHOTOS_DATABASE = "post_photos";
    public final static String FIREBASE_IMAGE_STORAGE = "images";

    //int for picking image from gallery:
    public final static int INT_ACTION_PICK = 1;

    //maximum image that can be picked:
    public final static int MAX_IMAGE_PICK = 10;

    //celeb firebase nodes:
    public final static String CELEB_ID = "celebId";
    public final static String CELEB_NAME = "celebName";
    public final static String CELEB_THUMB_URL = "celebThumbUrl";
    public final static String CELEB_FULL_URL = "celebFullUrl";

    //post firebase nodes:
    public final static String POST_ID = "postId";
    public final static String POST_DESC = "postDesc";
    public final static String POST_THUMB_URL = "postThumbUrl";
    public final static String POST_VIEWS = "postViews";
    public final static String POST_IMAGES = "postImages";

    //photo firebase nodes:
    public final static String PHOTO_ID = "photoId";
    public final static String PHOTO_FULL_URL = "photoFullUrl";

    //position int
    public final static String INT_POSITION = "position";

}
