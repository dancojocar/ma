package ro.ubbcluj.cs.books;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ro.ubbcluj.cs.books.books.R;
import ro.ubbcluj.cs.books.domain.Book;

public class NewBook extends AppCompatActivity {

  @BindView(R.id.bookTitle)
  EditText bookTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_book);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.save)
  public void save(View view) {
    Manager manager = new Manager();
    manager.save(new Book(0, bookTitle.getText().toString(),new Date()));
    finish();
  }
}
