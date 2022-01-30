package com.example.bookstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Books extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    BooksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Books");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // class provided by FirebaseUI in order to make a query in the database and fetch wanted data
        FirebaseRecyclerOptions<BookData> options = new FirebaseRecyclerOptions.Builder<BookData>().setQuery(reference, BookData.class).build();
        // connect object of adapter class to the adapter class itself
        adapter = new BooksAdapter(options);
        // connect adapter class with the recycler view
        recyclerView.setAdapter(adapter);
    }

    // function to tell the app to start getting data from the database when the activity starts
    @Override protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    // function to tell the app to stop getting data from the database when the activity stops
    @Override protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    // the following code is used for the menu bar of the application
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutMenu){
            logout();
        }
        if (id == R.id.cartMenu){
            cart();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(Books.this, MainActivity.class));
        Toast.makeText(Books.this,"LOGOUT SUCCESSFUL", Toast.LENGTH_SHORT).show();
        // delete the items of the cart once the user logs out
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart");
        reference.removeValue();
    }

    private void cart() {
        startActivity(new Intent(Books.this, Cart.class));
        Toast.makeText(Books.this,"VIEW CART", Toast.LENGTH_SHORT).show();
    }
}
