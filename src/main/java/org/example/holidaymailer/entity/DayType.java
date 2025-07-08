package org.example.holidaymailer.entity;

public enum DayType {
    Birthday("生日快乐！🎉"),

    Holiday("节日快乐！🎉");

    private String content;

    DayType(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
