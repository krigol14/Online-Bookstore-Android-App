package com.example.bookstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Cart extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference, reference2;
    CartAdapter adapter;
    MyTTS tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Cart");
        reference2 = FirebaseDatabase.getInstance().getReference().child("Books");
        tts = new MyTTS(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // class provided by FirebaseUI in order to make a query in the database and fetch wanted data
        FirebaseRecyclerOptions<BookData> options = new FirebaseRecyclerOptions.Builder<BookData>().setQuery(reference, BookData.class).build();
        // connect object of adapter class to the adapter class itself
        adapter = new CartAdapter(options);
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

    // when the user presses the back button redirect user to the books activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(Cart.this, Books.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // the following code is used for the menu bar of the application
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutMenu) {logout();}
        if (id == R.id.checkout) {checkout();}
        if (id == R.id.homeButton) {startActivity(new Intent(Cart.this, Books.class));}

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(Cart.this, MainActivity.class));
        Toast.makeText(Cart.this,"LOGOUT SUCCESSFUL", Toast.LENGTH_SHORT).show();
        // delete the items of the cart once the user logs out
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart");
        reference.removeValue();
    }

    private void checkout() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // find the title of the book the user bought
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    BookData data = snapshot1.getValue(BookData.class);
                    String title_bought = data.getTitle();

                    // find the quantity the user bought
                    reference.child(title_bought).child("quantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int quantity_bought = snapshot.getValue(Integer.class);

                            // find that specific book's availability and reduce its quantity by the number of the books the user bought
                            reference2.child(title_bought).child("quantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int books_available = snapshot.getValue(Integer.class);

                                    int books_left = books_available - quantity_bought;
                                    reference2.child(title_bought).child("quantity").setValue(books_left);

                                    // redirect user to the initial page of the application
                                    startActivity(new Intent(Cart.this, Books.class));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        Toast.makeText(Cart.this, "Thank you!", Toast.LENGTH_SHORT).show();
        tts.speak("Thanks for buying from us, hope to see you again.");
    }
}
