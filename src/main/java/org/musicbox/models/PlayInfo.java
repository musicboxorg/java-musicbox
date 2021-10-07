package org.musicbox.models;

import org.musicbox.utils.Utils;

public final class PlayInfo {

  private final String title;
  private final long length;
  private final boolean isPlaylist;

  public PlayInfo(String title, long length, boolean isPlaylist) {
	this.title = title;
	this.length = length;
	this.isPlaylist = isPlaylist;
  }

  public String getTitle() {
	return title;
  }

  /** audioTrack = timestamp, AudioPlaylist = size */
  public long getLength() {
	return length;
  }

  public boolean isPlaylist() {
	return isPlaylist;
  }

  public String getTiming() {
	if (isPlaylist()) {
	  return Long.toString(getLength());
	} else {
	  return Utils.getTimestamp(getLength());
	}
  }

}
