/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.boko.vimusic.loaders;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.boko.vimusic.model.HostType;
import com.boko.vimusic.model.Song;
import com.boko.vimusic.model.SongFactory;
import com.boko.vimusic.utils.Lists;
import com.boko.vimusic.utils.PreferenceUtils;

/**
 * Used to query {@link MediaStore.Audio.Media.EXTERNAL_CONTENT_URI} and return
 * the Song for a particular album.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class AlbumSongLoader extends WrappedAsyncTaskLoader<List<Song>> {

	/**
	 * The result
	 */
	private final ArrayList<Song> mSongList = Lists.newArrayList();

	/**
	 * The {@link Cursor} used to run the query.
	 */
	private Cursor mCursor;

	/**
	 * The Id of the album the songs belong to.
	 */
	private final String mAlbumID;

	/**
	 * Constructor of <code>AlbumSongHandler</code>
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 * @param albumId
	 *            The Id of the album the songs belong to.
	 */
	public AlbumSongLoader(final Context context, final String albumId) {
		super(context);
		mAlbumID = albumId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Song> loadInBackground() {
		// Create the Cursor
		mCursor = makeAlbumSongCursor(getContext(), mAlbumID);
		// Gather the data
		if (mCursor != null && mCursor.moveToFirst()) {
			do {
				// Copy the song Id
				final String id = mCursor.getString(0);
				
				// Create a new song
				final Song song = SongFactory.newSong(HostType.LOCAL, id);
				song.setName(mCursor.getString(1));
				song.setArtistName(mCursor.getString(2));
				song.setAlbumName(mCursor.getString(3));
				song.setDuration((int) (mCursor.getLong(4) / 1000));

				// Add everything up
				mSongList.add(song);
			} while (mCursor.moveToNext());
		}
		// Close the cursor
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		return mSongList;
	}

	/**
	 * @param context
	 *            The {@link Context} to use.
	 * @param albumId
	 *            The Id of the album the songs belong to.
	 * @return The {@link Cursor} used to run the query.
	 */
	public static final Cursor makeAlbumSongCursor(final Context context,
			final String albumId) {
		// Match the songs up with the artist
		final StringBuilder selection = new StringBuilder();
		selection.append(AudioColumns.IS_MUSIC + "=1");
		selection.append(" AND " + AudioColumns.TITLE + " != ''");
		selection.append(" AND " + AudioColumns.ALBUM_ID + "=" + albumId);
		return context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
				/* 0 */
				BaseColumns._ID,
				/* 1 */
				AudioColumns.TITLE,
				/* 2 */
				AudioColumns.ARTIST,
				/* 3 */
				AudioColumns.ALBUM,
				/* 4 */
				AudioColumns.DURATION }, selection.toString(), null,
				PreferenceUtils.getInstance(context).getAlbumSongSortOrder());
	}

}
