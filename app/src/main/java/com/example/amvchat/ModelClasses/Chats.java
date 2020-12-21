package com.example.amvchat.ModelClasses;

public class Chats {

  private String sender;
  private String massage;
  private String receiver;
  private Boolean issen;
  private String url;
  private String massageId;

    public Chats() {
    }

    public Chats(String sender, String massage, String receiver, Boolean issen, String url, String massageId) {
        this.sender = sender;
        this.massage = massage;
        this.receiver = receiver;
        this.issen = issen;
        this.url = url;
        this.massageId = massageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Boolean getIssen() {
        return issen;
    }

    public void setIssen(Boolean issen) {
        this.issen = issen;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMassageId() {
        return massageId;
    }

    public void setMassageId(String massageId) {
        this.massageId = massageId;
    }
}
