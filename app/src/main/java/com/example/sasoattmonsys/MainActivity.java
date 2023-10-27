package com.example.sasoattmonsys;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    String id=result.getContents();
                    db.collection("users")
                            .whereEqualTo("id",id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    String Name = ""; // Declare the variable here
                                    String Cluster = "";
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            Name = document.getString("name"); // Assign the value here
                                            Cluster = document.getString("cluster");

                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                        Toast.makeText(MainActivity.this, "PERMISSION DENIED", Toast.LENGTH_LONG).show();

                                    }
                                    Toast.makeText(MainActivity.this, id+Name+Cluster, Toast.LENGTH_LONG).show();

                                }
                            });

                }
            });

    private FloatingActionButton fab ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab=findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(view);
            }
        });

    }

    public void onButtonClick(View view) {
        // Call the barcodeLauncher's launch method when the button is clicked
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }
}