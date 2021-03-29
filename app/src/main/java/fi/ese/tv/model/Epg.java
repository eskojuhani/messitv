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
 * Epg is an immutable object that holds the various metadata associated with a single epg data.
 */
public final class Epg implements Parcelable {
    public final long id;
    public final String sname;
    public final String title;
    public final long begin_timestamp;
    public final long now_timestamp;
    public final String sref;
    public final String genre;
    public final long duration_sec;
    public final String shortdesc;
    public final long genreid;
    public final String longdesc;

    private Epg(
            final long id,
            final String sname,
            final String title,
            final long begin_timestamp,
            final long now_timestamp,
            final String sref,
            final String genre,
            final long duration_sec,
            final String shortdesc,
            final long genreid,
            final String longdesc) {
        this.id = id;
        this.sname = sname;
        this.title = title;
        this.begin_timestamp = begin_timestamp;
        this.now_timestamp = now_timestamp;
        this.sref = sref;
        this.genre = genre;
        this.duration_sec = duration_sec;
        this.shortdesc = shortdesc;
        this.genreid = genreid;
        this.longdesc = longdesc;
    }

    protected Epg(Parcel in) {
        id = in.readLong();
        sname = in.readString();
        title = in.readString();
        begin_timestamp = in.readLong();
        now_timestamp = in.readLong();
        sref = in.readString();
        genre = in.readString();
        duration_sec = in.readLong();
        shortdesc = in.readString();
        genreid = in.readLong();
        longdesc = in.readString();
    }

    public static final Creator<Epg> CREATOR = new Creator<Epg>() {
        @Override
        public Epg createFromParcel(Parcel in) {
            return new Epg(in);
        }

        @Override
        public Epg[] newArray(int size) {
            return new Epg[size];
        }
    };

    @Override
    public boolean equals(Object m) {
        return m instanceof Epg && id == ((Epg) m).id;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(sname);
        dest.writeString(title);
        dest.writeLong(begin_timestamp);
        dest.writeLong(now_timestamp);
        dest.writeString(sref);
        dest.writeString(genre);
        dest.writeLong(duration_sec);
        dest.writeString(shortdesc);
        dest.writeLong(genreid);
        dest.writeString(longdesc);
    }

    @Override
    public String toString() {
        String s = "Epg{";
        s += "id=" + id;
        s += ", sname='" + sname + "'";
        s += ", title='" + title + "'";
        s += ", begin_timestamp='" + begin_timestamp + "'";
        s += ", now_timestamp='" + now_timestamp + "'";
        s += ", sref='" + sref + "'";
        s += ", genre='" + genre + "'";
        s += ", duration='" + duration_sec + "'";
        s += ", shortdesc='" + shortdesc + "'";
        s += ", genreid='" + genreid + "'";
        s += ", longdesc='" + longdesc + "'";
        s += "}";
        return s;
    }

    // Builder for Epg object.
    public static class EpgBuilder {
        private long id;
        private String sname;
        private String title;
        private long begin_timestamp;
        private long now_timestamp;
        private String sref;
        private String genre;
        private long duration_sec;
        private String shortdesc;
        private long genreid;
        private String longdesc;

        public EpgBuilder id(long id) {
            this.id = id;
            return this;
        }

        public EpgBuilder sname(String sname) {
            this.sname = sname;
            return this;
        }

        public EpgBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EpgBuilder begin_timestamp(Long begin_timestamp) {
            this.begin_timestamp = begin_timestamp;
            return this;
        }

        public EpgBuilder now_timestamp(Long now_timestamp) {
            this.now_timestamp = now_timestamp;
            return this;
        }

        public EpgBuilder sref(String sref) {
            this.sref = sref;
            return this;
        }

        public EpgBuilder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public EpgBuilder duration_sec(Long duration_sec) {
            this.duration_sec = duration_sec;
            return this;
        }

        public EpgBuilder shortdesc(String shortdesc) {
            this.shortdesc = shortdesc;
            return this;
        }

        public EpgBuilder genreid(Long genreid) {
            this.genreid = genreid;
            return this;
        }

        public EpgBuilder longdesc(String longdesc) {
            this.longdesc = longdesc;
            return this;
        }

        public Epg build() {
            return new Epg(
                    id,
                    sname,
                    title,
                    begin_timestamp,
                    now_timestamp,
                    sref,
                    genre,
                    duration_sec,
                    shortdesc,
                    genreid,
                    longdesc
            );
        }
    }
}
