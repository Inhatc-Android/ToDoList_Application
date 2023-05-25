package com.inhatc.todolist_application;

public class DiaryModel {
    private String title;
    private String Content;

    public DiaryModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public DiaryModel(String title, String content) {
        this.title = title;
        this.Content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        this.Content = content;
    }

}
