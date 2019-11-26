package com.gatech.update.Controller;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class GroupStructure implements Parcelable {
    // future: images for group
    private String mGroupName, mGroupID;
    private ArrayList<String> mUsers, mStatus, mActivities;

    public GroupStructure(String groupName, String groupID, ArrayList<String> users,
                          ArrayList<String> status, ArrayList<String> activity) {
        mGroupName = groupName;
        mGroupID = groupID;
        mUsers = users;
        mStatus = status;
        mActivities = activity;
    }

    protected GroupStructure(Parcel in) {
        mGroupName = in.readString();
        mGroupID = in.readString();
        mUsers = in.createStringArrayList();
        mStatus = in.createStringArrayList();
        mActivities = in.createStringArrayList();
    }

    public static final Creator<GroupStructure> CREATOR = new Creator<GroupStructure>() {
        @Override
        public GroupStructure createFromParcel(Parcel in) {
            return new GroupStructure(in);
        }

        @Override
        public GroupStructure[] newArray(int size) {
            return new GroupStructure[size];
        }
    };

    public String getGroupName() {
        return mGroupName;
    }

    public String getGroupID() {
        return mGroupID;
    }

    public ArrayList<String> getUsers() {
        return mUsers;
    }

    public ArrayList<String> getStatus() {
        return mStatus;
    }

    public ArrayList<String> getActivity() {
        return mActivities;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mGroupName);
        parcel.writeString(mGroupID);
        parcel.writeStringList(mUsers);
        parcel.writeStringList(mStatus);
        parcel.writeStringList(mActivities);
    }
}
