package com.gatech.update.Controller;

import java.util.ArrayList;

public class GroupStructure {
    // future: images for group
    private String mGroupName;
    private ArrayList<String> mUsers;
    private ArrayList<String> mStatus;

    public GroupStructure(String groupName, ArrayList<String> users, ArrayList<String> status) {
        mGroupName = groupName;
        mUsers = users;
        mStatus = status;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public ArrayList<String> getUsers() {
        return mUsers;
    }

    public ArrayList<String> getStatus() {
        return mStatus;
    }
}
