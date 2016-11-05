package com.ssl.mavericks.feeder39;

import java.util.Date;

class Assignment {

    int pk;
    int coursepk;
    String title;
    String description;
    Date deadline;

    public Assignment(int pk, int coursepk, String title, String description, Date deadline){
        this.pk = pk;
        this.coursepk = coursepk;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public int getDatabaseCode(){
        return pk;
    }

    public String getTitle(){
        return title;
    }

    public Date getDeadline(){
        return deadline;
    }

    public String getDescription(){
        return description;
    }

    public int getCourseDatabaseCode(){
        return coursepk;
    }

}

class Course{
    int pk;
    String code;
    String name;
    public Course(int pk, String code, String name){
        this.pk = pk;
        this.code = code;
        this.name = name;
    }

    public int getDatabaseCode(){
        return pk;
    }

    public String getCode(){
        return code;
    }

    public String getName(){
        return name;
    }
}

class Feedback {
    int pk;
    int coursepk;
    String feedbackname;
    Date deadline;
    boolean alreadyFilled;

    public Feedback(int pk, int coursepk, String feedbackname, Date deadline){
        this.pk = pk;
        this.coursepk = coursepk;
        this.feedbackname = feedbackname;
        this.deadline = deadline;
        alreadyFilled = false;
    }

    public int getDatabaseCode(){
        return pk;
    }

    public String getFeedbackName() {
        return feedbackname;
    }

    public int getCourseDatabaseCode(){
        return coursepk;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setFilled(boolean fill){
        this.alreadyFilled = fill;
    }
}

class Question{

    int pk;
    String text;
    String answer;
    String questionType;
    int feedbackpk;

    Question(int pk, String text, String questionType, int feedbackpk){
        this.pk = pk;
        this.text = text;
        this.questionType = questionType;
        this.feedbackpk = feedbackpk;

        answer = "";
    }
}
