package com.inhatc.todolist_application;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<DiaryModel> diaryList = new ArrayList<>();
    private RecycleViewAdapter adapter;

    private Dialog writeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 초기 빈 데이터를 추가합니다.
        diaryList.add(new DiaryModel("Empty", "There is no data"));

        recyclerView = findViewById(R.id.rvTodo);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecycleViewAdapter(HomeActivity.this, diaryList);
        recyclerView.setAdapter(adapter);

        // 로그아웃 버튼을 클릭하면 Firebase 사용자 인증에서 로그아웃하고 현재 액티비티를 종료합니다.
        Button logBtn = findViewById(R.id.logout);
        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        // "Add To Do" 버튼을 클릭하면 작성 다이얼로그를 표시합니다.
        ImageButton addToDoButton = findViewById(R.id.addTodo);
        addToDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWriteDialog(HomeActivity.this);
            }
        });

        // 현재 사용자의 UID를 가져와서 Firebase에서 해당 사용자의 다이어리 데이터를 가져옵니다.
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot diarySnapshot = dataSnapshot.child("Diary");
                    if (diarySnapshot.exists()) {
                        diaryList.clear();
                        // 다이어리 데이터를 반복하면서 제목과 내용을 가져와서 diaryList에 추가합니다.
                        for (DataSnapshot titleSnapshot : diarySnapshot.getChildren()) {
                            String title = titleSnapshot.getKey();
                            if (title != null) {
                                String content = titleSnapshot.child("Content").getValue(String.class);
                                if (content != null) {
                                    DiaryModel diary = new DiaryModel(title, content);
                                    diaryList.add(diary);
                                }
                            }
                        }
                        // 어댑터에 변경 사항을 알리고 리사이클뷰를 업데이트합니다.
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("HomeActivity", "Firebase Database Error: " + databaseError.getMessage());
            }
        });
    }

    // 데이터를 Firebase에 저장하는 메서드입니다.
    private void saveData(String title, String contents) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

        // Diary 폴더를 확인하고 없으면 생성합니다.
        DatabaseReference diaryRef = usersRef.child("Diary");
        diaryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Diary 폴더가 없으면 생성합니다.
                    diaryRef.setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Diary 폴더 생성 후 제목과 내용을 저장합니다.
                                    saveTitleAndContent(diaryRef, title, contents);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(HomeActivity.this, "Failed to create Diary folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Diary 폴더가 이미 존재하면 제목과 내용을 저장합니다.
                    saveTitleAndContent(diaryRef, title, contents);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Firebase Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 제목과 내용을 Firebase에 저장하는 메서드입니다.
    private void saveTitleAndContent(DatabaseReference diaryRef, String title, String contents) {
        // 제목을 생성한 뒤 해당 제목에 내용을 저장합니다.
        DatabaseReference titleRef = diaryRef.child(title);
        titleRef.child("Content").setValue(contents)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 데이터 저장에 성공하면 diaryList에 항목을 추가하고 어댑터에 변경 사항을 알립니다.
                        DiaryModel diary = new DiaryModel(title, contents);
                        diaryList.add(diary);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(HomeActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 작성 다이얼로그를 표시하는 메서드입니다.
    private void showWriteDialog(Context context) {
        writeDialog.show();

        EditText writeTitle = writeDialog.findViewById(R.id.writeTitle);
        EditText writeContents = writeDialog.findViewById(R.id.writeContents);

        Button saveButton = writeDialog.findViewById(R.id.saveBtn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                String title = writeTitle.getText().toString();
                String contents = writeContents.getText().toString();

                writeTitle.setText("");
                writeContents.setText("");

                // 작성한 데이터를 저장합니다.
                saveData(title, contents);

                writeDialog.dismiss();
            }
        });
        Button cancelButton = writeDialog.findViewById(R.id.cancelBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
                writeTitle.setText("");
                writeContents.setText("");
                writeDialog.dismiss();
            }
        });
    }
}
