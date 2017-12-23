package com.example.yuetung55.adventurebook;

import android.content.Context;

/**
 * Created by Yuetung55 on 21/12/2017.
 */

public class User {

    private int currentStoryNode;
    private static User user;

    /* constructor */
    private User (Context context){
        //TODO: use a database to save and retrieve user information
        currentStoryNode=0;
    }

    /* Singleton implementation */
    public static User getUser(Context context) {
        if (user==null) {
            // System.out.println("no instance, creating new instance");      //debug
            user=new User(context);
        }
        return user;
    }

    /* getters and setters */

    public int getCurrentStoryNode() {
        return currentStoryNode;
    }

    public void setCurrentStoryNode(int currentStoryNode) {
        this.currentStoryNode = currentStoryNode;
    }
}
