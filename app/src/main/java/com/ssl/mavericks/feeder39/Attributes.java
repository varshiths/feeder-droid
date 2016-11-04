package com.ssl.mavericks.feeder39;

import java.util.Date;

class Assignment {

    int pk;
    int coursecode;
    String title;
    String description;
    Date deadline;

    public Assignment(int pk, int coursecode, String title, String description, Date deadline){
        this.pk = pk;
        this.coursecode = coursecode;
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

}
