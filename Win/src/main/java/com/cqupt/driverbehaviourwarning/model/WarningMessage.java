package com.cqupt.driverbehaviourwarning.model;

public class WarningMessage {

    private String ID;
    private String Time;
    private String Body;//为三位字符，身体姿势Body+疲劳程度Fatigue+头部姿势Head

    public WarningMessage() {
    }

    public WarningMessage(String ID, String time, String body) {
        this.ID = ID;
        Time = time;
        Body = body;
    }

    private WarningMessage(Builder builder) {
        ID = builder.ID;
        Time = builder.Time;
        Body = builder.Body;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }

    @Override
    public String toString() {
        return "WarningMessage{" +
                "ID='" + ID + '\'' +
                ", Time='" + Time + '\'' +
                ", Body='" + Body + '\'' +
                '}';
    }

    public static final class Builder {
        private String ID;
        private String Time;
        private String Body;

        public Builder() {
        }

        public Builder ID(String val) {
            ID = val;
            return this;
        }

        public Builder Time(String val) {
            Time = val;
            return this;
        }

        public Builder Body(String val) {
            Body = val;
            return this;
        }

        public WarningMessage build() {
            return new WarningMessage(this);
        }
    }
}
