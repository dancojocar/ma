package ro.ubbcluj.cs.books.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "books")
public class Book {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String title;
  private Date date;

  public Book(int id, String title, Date date) {
    this.id = id;
    this.title = title;
    this.date = date;
  }

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

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
