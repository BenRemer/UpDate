package com.gatech.update.Controller;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class GroupStructure implements Parcelable {
    // future: images for group
    private String mGroupName;
    private ArrayList<String> mUsers;
    private ArrayList<String> mStatus;

    public GroupStructure(String groupName, ArrayList<String> users, ArrayList<String> status) {
        mGroupName = groupName;
        mUsers = users;
        mStatus = status;
    }

    protected GroupStructure(Parcel in) {
        mGroupName = in.readString();
        mUsers = in.createStringArrayList();
        mStatus = in.createStringArrayList();
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

    public ArrayList<String> getUsers() {
        return mUsers;
    }

    public ArrayList<String> getStatus() {
        return mStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mGroupName);
        parcel.writeStringList(mUsers);
        parcel.writeStringList(mStatus);
    }
}
