package com.fixedit.fixitadmin.Services;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.fixedit.fixitadmin.R;
import com.fixedit.fixitadmin.Utils.CommonUtils;
import com.fixedit.fixitadmin.Utils.CompressImage;
import com.fixedit.fixitadmin.Utils.GifSizeFilter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddSubService extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 23;
    Button update;
    EditText serviceHour, serviceMinute, measureUnit, serviceName;
    DatabaseReference mDatabase;


    String id;
    private String parentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sub_services);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.setTitle("Add Sub Service");
        getPermissions();

        id = getIntent().getStringExtra("id");
        parentService = getIntent().getStringExtra("parentService");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        update = findViewById(R.id.update);

        serviceMinute = findViewById(R.id.serviceMinute);
        measureUnit = findViewById(R.id.measureUnit);
        serviceHour = findViewById(R.id.serviceHour);
        serviceName = findViewById(R.id.serviceName);


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceName.getText().toString().trim().isEmpty()) {
                    serviceName.setError("Enter service name");
                } else if (serviceHour.getText().toString().trim().isEmpty()) {
                    serviceHour.setError("Enter service hour");
                } else if (serviceMinute.getText().toString().trim().isEmpty()) {
                    serviceMinute.setError("Enter service minute");
                } else {
                    if (id != null) {
                        updateDataToB();
                    } else {
                        sendDataToDB();
                    }
                }
            }
        });
        if (id != null) {
            getDataFromDB();
        }
    }

    private void updateDataToB() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", serviceName.getText().toString());
        map.put("timeHour", Integer.parseInt(serviceHour.getText().toString()));
        map.put("timeMin", Integer.parseInt(serviceMinute.getText().toString()));
        map.put("measureUnit", measureUnit.getText().toString());


        mDatabase.child("SubServices").child(id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                CommonUtils.showToast("Updated");
            }
        });

    }

    private void getDataFromDB() {
        mDatabase.child("SubServices").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    SubServiceModel model = dataSnapshot.getValue(SubServiceModel.class);
                    if (model != null) {
                        serviceName.setText(model.getName());
                        serviceMinute.setText(model.getTimeMin()+"");
                        serviceHour.setText(model.getTimeHour()+"");
                        measureUnit.setText(model.getMeasureUnit());

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendDataToDB() {


        SubServiceModel model = new SubServiceModel(
                serviceName.getText().toString(),
                serviceName.getText().toString(),
                true,
                Integer.parseInt(serviceHour.getText().toString()),
                Integer.parseInt(serviceMinute.getText().toString()),
                parentService,
                0, 10,
                measureUnit.getText().toString()
        );


        mDatabase.child("SubServices").child(serviceName.getText().toString()).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                CommonUtils.showToast("Updated");
            }
        });


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

    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
//        CommonUtils.showToast(PERMISSION_ALL+"");
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
