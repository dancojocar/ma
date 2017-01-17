package ro.ubbcluj.cs.books;

import ro.ubbcluj.cs.books.domain.Book;

public interface MyCallback {
  void add(Book book);
  void showError(String message);
  void clear();
}
