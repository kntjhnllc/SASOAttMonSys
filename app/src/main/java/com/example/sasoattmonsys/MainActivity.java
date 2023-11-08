package com.example.sasoattmonsys;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FloatingActionButton fab ;
    private TextView meet;
    private String meetingValue = "No Active Meeting";
    private String meetID ="";
    private String id_no="";
    private String Name = ""; // Declare the variable here
    private String Cluster = "";
    private String errorMessage="";
    private List<String> dataList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab=findViewById(R.id.fab);
        meet=findViewById(R.id.meeting);

        CollectionReference meetingCollection = db.collection("meeting");
        meetingCollection.whereEqualTo("active", true).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String meeting = "No Active Meeting";

                if (snapshot != null && !snapshot.isEmpty()) {

                    for (DocumentChange dc : snapshot.getDocumentChanges()) {
                        // Check the type of change (added, modified, or removed)
                        switch (dc.getType()) {
                            case ADDED:
                            case MODIFIED:
                                DocumentSnapshot document = dc.getDocument();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                meetingValue = document.getString("meetName");
                                meetID = document.getString("meetID");
                                break;
                            case REMOVED:
                                // Handle document removal if needed
                                break;
                        }
                    }
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onButtonClick(view);
                        }
                    });
                }
                else {
                    meetingValue="No Active Meeting";
                    fab.setOnClickListener(null);
                }

                meet.setText(meetingValue);
            }
        });



    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    String id=result.getContents();
                    id_no=id;
                    db.collection("users")
                            .whereEqualTo("id_no",id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                    if (task1.isSuccessful()) {
                                        db.collection("attendance")
                                                .whereEqualTo("meetID", meetID)
                                                .whereEqualTo("id_no", id_no)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(Task<QuerySnapshot> task) {
                                                        if (!task.getResult().isEmpty()) {

                                                            Toast.makeText(MainActivity.this, "Student Already Attended", Toast.LENGTH_LONG).show();

                                                        } else {
                                                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                                Name = document.getString("name"); // Assign the value here
                                                                Cluster = document.getString("cluster");
                                                                Map<String, Object> user = new HashMap<>();
                                                                user.put("id_no", id);
                                                                user.put("meetID", meetID);
                                                                user.put("status", "Present");
                                                                user.put("dateTime", FieldValue.serverTimestamp());

                                                                db.collection("attendance")
                                                                        .add(user)
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                                                                Toast.makeText(MainActivity.this, id+Name+Cluster+meetingValue, Toast.LENGTH_LONG).show();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.w(TAG, "Error adding document", e);
                                                                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task1.getException());
                                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                                    }


                                }
                            });

                }
            });


    public void onButtonClick(View view) {
        // Call the barcodeLauncher's launch method when the button is clicked
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }
}