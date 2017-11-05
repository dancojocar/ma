package com.example.ma.sm.database.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index("title")})
public class PortfolioEntity {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String title;
  private String description;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "PE{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", description='" + description + '\'' +
        '}';
  }
}