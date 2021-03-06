package com.fixedit.fixitadmin.Services;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.fixedit.fixitadmin.R;
import com.fixedit.fixitadmin.Utils.CommonUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListOfSubServices extends AppCompatActivity {
    ImageView addService;
    RecyclerView recyclerview;
    SubServiceListAdapter adapter;
    private ArrayList<SubServiceModel> itemList = new ArrayList<>();
    DatabaseReference mDatabase;
    String parentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_services);
        addService = findViewById(R.id.addService);
        recyclerview = findViewById(R.id.recyclerview);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        parentService = getIntent().getStringExtra("parentService");
        this.setTitle(parentService);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        addService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ListOfSubServices.this,AddSubService.class);
                i.putExtra("parentService",parentService);
                startActivity(i);
            }

        });


        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new SubServiceListAdapter(this, itemList, new SubServiceListAdapter.SubServiceListAdapterCallbacks() {
            @Override
            public void onServiceStatusChanged(SubServiceModel model, final boolean value) {
                mDatabase.child("SubServices").child(model.getName()).child("active").setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (value) {
                            CommonUtils.showToast("Service activated");
                        } else {
                            CommonUtils.showToast("Service Deactivated");
                        }
                    }
                });
            }

            @Override
            public void onServiceDeleted(SubServiceModel model) {
                showAlert(model);
            }
        });

        recyclerview.setAdapter(adapter);

        getDataFromDB();

    }

    private void getDataFromDB() {
        mDatabase.child("SubServices").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    itemList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        SubServiceModel model = snapshot.getValue(SubServiceModel.class);
                        if (model != null) {
                            if (model.getParentService().contains(parentService)) {
                                itemList.add(model);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    itemList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showAlert(final SubServiceModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Do you want to delete this service? ");

        // add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child("SubServices").child(model.getName()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonUtils.showToast("Services Deleted");
                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {


            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
