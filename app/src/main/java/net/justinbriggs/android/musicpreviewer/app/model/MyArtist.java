package net.justinbriggs.android.musicpreviewer.app.model;


import android.os.Parcel;
import android.os.Parcelable;

public class MyArtist implements Parcelable {

    String id;
    String imageUrl;
    String name;

    public MyArtist(){}

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected MyArtist(Parcel in) {
        imageUrl = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyArtist> CREATOR = new Parcelable.Creator<MyArtist>() {
        @Override
        public MyArtist createFromParcel(Parcel in) {
            return new MyArtist(in);
        }

        @Override
        public MyArtist[] newArray(int size) {
            return new MyArtist[size];
        }
    };
}