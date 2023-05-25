package com.inhatc.todolist_application;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    private static Context context;
    private List<DiaryModel> diaryList;

    public RecycleViewAdapter(Context context, List<DiaryModel> diaryList) {
        this.context = context;
        this.diaryList = diaryList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private Dialog editDialog;
        private EditText editTitle;
        private EditText editContents;
        private Button updateButton;
        private Button cancelButton;
        ConstraintLayout itemView;
        ConstraintLayout layout;
        TextView title;
        TextView contents;
        ImageView done;
        ImageView cancel;

        int position;

        @SuppressLint("ResourceType")
        public ViewHolder(View view) {
            super(view);
            itemView = view.findViewById(R.layout.item_list);
            layout = view.findViewById(R.id.itemView);
            title = view.findViewById(R.id.title);
            contents = view.findViewById(R.id.contents);
            done = view.findViewById(R.id.done);
            cancel = view.findViewById(R.id.delete);

            editDialog = new Dialog(context);
            editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            editDialog.setContentView(R.layout.dialog_write);

            editTitle = editDialog.findViewById(R.id.writeTitle);
            editContents = editDialog.findViewById(R.id.writeContents);
            updateButton = editDialog.findViewById(R.id.saveBtn);
            cancelButton = editDialog.findViewById(R.id.cancelBtn);
            position = getAdapterPosition();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.title.setText(diaryList.get(position).getTitle());
        holder.contents.setText(diaryList.get(position).getContent());

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.title.setText(diaryList.get(position).getTitle());
                holder.contents.setText(diaryList.get(position).getContent());

                holder.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = diaryList.get(position).getTitle();
                        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        DatabaseReference diaryRef = currentUserRef.child("Diary").child(title);
                        diaryRef.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        diaryList.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to delete data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });

        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldTitle = diaryList.get(position).getTitle();
                String oldContents = diaryList.get(position).getContent();

                holder.editTitle.setText(oldTitle);
                holder.editContents.setText(oldContents);

                holder.editDialog.show();

                holder.updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newTitle = holder.editTitle.getText().toString();
                        String newContents = holder.editContents.getText().toString();

                        final int position = holder.getAdapterPosition();

                        // 데이터베이스 업데이트 메서드 호출
                        updateData(oldTitle, newTitle, newContents, position);

                        holder.editDialog.dismiss();
                    }
                });

                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.editDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    private void updateData(String oldTitle, String newTitle, String newContents, int position) {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        DatabaseReference diaryRef = currentUserRef.child("Diary");

        // 기존의 oldTitle을 찾아서 해당 노드를 삭제하고, 새로운 노드를 추가하여 업데이트
        diaryRef.child(oldTitle).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference newDiaryRef = diaryRef.child(newTitle);
                        newDiaryRef.child("Content").setValue(newContents)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Data updated successfully", Toast.LENGTH_SHORT).show();
                                        diaryList.get(position).setTitle(newTitle);
                                        diaryList.get(position).setContent(newContents);
                                        notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to update data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to update data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
