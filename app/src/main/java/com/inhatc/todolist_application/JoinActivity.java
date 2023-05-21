package com.inhatc.todolist_application;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class JoinActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText passwordCheckText;
    private Button emailCheckButton;
    private Button cancleButton;
    private Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        auth = FirebaseAuth.getInstance();

        nameText = (EditText) findViewById(R.id.joinNameText);
        emailText = (EditText) findViewById(R.id.joinEmailText);
        passwordText = (EditText) findViewById(R.id.joinPassword);
        passwordCheckText = (EditText) findViewById(R.id.joinPasswordCheck);

        emailCheckButton = (Button) findViewById(R.id.emailCheckButton);
        cancleButton = (Button) findViewById(R.id.cancelButton);
        joinButton = (Button) findViewById(R.id.joinButton);

        emailCheckButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = emailText.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(JoinActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    checkEmailExistsInFirebase(email);
                }
            }
        });
        cancleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = emailText.getText().toString().trim();
                String name = nameText.getText().toString().trim();
                String pw = passwordText.getText().toString().trim();
                String pwChk = passwordCheckText.getText().toString().trim();
                if(pw.equals(pwChk)){
                    auth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(JoinActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = auth.getCurrentUser();
                                String email = user.getEmail();
                                String uid = user.getUid();

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("email", email);
                                hashMap.put("name", name);

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(uid).setValue(hashMap);
                                Intent intent = new Intent(JoinActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(JoinActivity.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(JoinActivity.this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    });
                }else{
                    Toast.makeText(JoinActivity.this, "비밀 번호가 틀렸습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void checkEmailExistsInFirebase(String email) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods != null && !signInMethods.isEmpty()) {
                                // 이메일이 이미 등록되어 있음
                                Toast.makeText(JoinActivity.this, "이미 등록된 이메일입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                // 이메일이 등록되어 있지 않음
                                Toast.makeText(JoinActivity.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 작업 실패
                            Toast.makeText(JoinActivity.this, "이메일 중복 확인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}