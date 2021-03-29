/*
 * Copyright (c) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.ese.tv.model;

import android.media.MediaDescription;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Video is an immutable object that holds the various metadata associated with a single video.
 */
public final class Video implements Parcelable {
    public final long id;
    public final String category;
    public final String title;
    public final String description;
    public final String bgImageUrl;
    public final String cardImageUrl;
    public final String videoUrl;
    public final String authorization;
    public final boolean isLive;
    public final boolean isRecording;
    public final String filename;
    public final String length;

    private Video(
            final long id,
            final String category,
            final String title,
            final String desc,
            final String videoUrl,
            final String bgImageUrl,
            final String cardImageUrl,
            final String authorization,
            final boolean isLive,
            final boolean isRecording,
            final String filename,
            final String length) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.description = desc;
        this.videoUrl = videoUrl;
        this.bgImageUrl = bgImageUrl;
        this.cardImageUrl = cardImageUrl;
        this.authorization = authorization;
        this.isLive = isLive;
        this.isRecording = isRecording;
        this.filename = filename;
        this.length = length;
    }

    protected Video(Parcel in) {
        id = in.readLong();
        category = in.readString();
        title = in.readString();
        description = in.readString();
        bgImageUrl = in.readString();
        cardImageUrl = in.readString();
        videoUrl = in.readString();
        authorization = in.readString();
        isLive = in.readInt() == 1 ? true : false;
        isRecording = in.readInt() == 1 ? true : false;
        filename = in.readString();
        length = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public boolean equals(Object m) {
        return m instanceof Video && id == ((Video) m).id;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(category);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(bgImageUrl);
        dest.writeString(cardImageUrl);
        dest.writeString(videoUrl);
        dest.writeString(authorization);
        dest.writeInt(isLive ? 1 : 0);
        dest.writeInt(isRecording ? 1 : 0);
        dest.writeString(filename);
        dest.writeString(length);
    }

    @Override
    public String toString() {
        String s = "Video{";
        s += "id=" + id;
        s += ", category='" + category + "'";
        s += ", title='" + title + "'";
        s += ", videoUrl='" + videoUrl + "'";
        s += ", bgImageUrl='" + bgImageUrl + "'";
        s += ", cardImageUrl='" + cardImageUrl + "'";
        s += ", authorization='" + authorization + "'";
        s += ", isLive='" + isLive + "'";
        s += ", isRecording='" + isRecording + "'";
        s += ", filename ='" + filename + "'";
        s += ", length='" + length + "'";
        s += "}";
        return s;
    }

    // Builder for Video object.
    public static class VideoBuilder {
        private long id;
        private String category;
        private String title;
        private String desc;
        private String bgImageUrl;
        private String cardImageUrl;
        private String videoUrl;
        private String authorization;
        private Boolean isLive;
        private Boolean isRecording;
        private String filename;
        private String length;

        public VideoBuilder id(long id) {
            this.id = id;
            return this;
        }

        public VideoBuilder category(String category) {
            this.category = category;
            return this;
        }

        public VideoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public VideoBuilder description(String desc) {
            this.desc = desc;
            return this;
        }

        public VideoBuilder authorization(String authorization) {
            this.authorization = authorization;
            return this;
        }

        public VideoBuilder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public VideoBuilder bgImageUrl(String bgImageUrl) {
            this.bgImageUrl = bgImageUrl;
            return this;
        }

        public VideoBuilder cardImageUrl(String cardImageUrl) {
            this.cardImageUrl = cardImageUrl;
            return this;
        }

        public VideoBuilder isLive(Boolean isLive) {
            this.isLive = isLive;
            return this;
        }

        public VideoBuilder isRecording(Boolean isRecording) {
            this.isRecording = isRecording;
            return this;
        }

        public VideoBuilder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public VideoBuilder length(String length) {
            this.length = length;
            return this;
        }

        public Video build() {
            return new Video(
                    id,
                    category,
                    title,
                    desc,
                    videoUrl,
                    bgImageUrl,
                    cardImageUrl,
                    authorization,
                    isLive,
                    isRecording,
                    filename,
                    length
            );
        }
    }
}
