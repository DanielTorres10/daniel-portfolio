package com.google.sps.data;

/** A comment on the comment section */
public final class Comment {

  private final long id;
  private final String text;
  private final long timestamp;
  private final double sentiment_score;
  private final String user;

  public Comment(long id, String text, long timestamp, double sentiment_score, String user) {
    this.id = id;
    this.text = text;
    this.timestamp = timestamp;
    this.sentiment_score = sentiment_score;
    this.user = user;
  }
}