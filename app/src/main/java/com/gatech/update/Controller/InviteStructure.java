package com.gatech.update.Controller;

import android.os.Parcel;
import android.os.Parcelable;


public class InviteStructure implements Parcelable {
    // Define instance variables for invitations
    private String mGroupName, mGroupID, mHostName;

    public InviteStructure (String groupName, String groupID, String hostName) {
        mGroupName = groupName;
        mGroupID = groupID;
        mHostName = hostName;
    }

    protected InviteStructure(Parcel in) {
        mGroupName = in.readString();
        mGroupID = in.readString();
        mHostName = in.readString();
    }

    public static final Creator<InviteStructure> CREATOR = new Creator<InviteStructure>() {
        @Override
        public InviteStructure createFromParcel(Parcel in) {
            return new InviteStructure(in);
        }

        @Override
        public InviteStructure[] newArray(int size) {
            return new InviteStructure[size];
        }
    };

    public String getGroupName() {
        return mGroupName;
    }

    public String getGroupID() {
        return mGroupID;
    }

    public String getHostName() {
        return mHostName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mGroupName);
        parcel.writeString(mGroupID);
        parcel.writeString(mHostName);
    }
}
