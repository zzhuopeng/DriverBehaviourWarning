package com.cqupt.driverbehaviourwarning.model;

public class WarningMessage {

    private String ID;
    private String time;
    private String Value;

    public WarningMessage() {
    }

    public WarningMessage(String ID, String time, String value) {
        this.ID = ID;
        this.time = time;
        Value = value;
    }

    private WarningMessage(Builder builder) {
        setTime(builder.ID);
        setTime(builder.time);
        setValue(builder.Value);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    @Override
    public String toString() {
        return "WarningMessage{" +
                "ID='" + ID + '\'' +
                ", time='" + time + '\'' +
                ", Value='" + Value + '\'' +
                '}';
    }


    public static final class Builder {
        private String ID;
        private String time;
        private String Value;

        public Builder() {
        }

        public Builder setsetID(String val) {
            ID = val;
            return this;
        }

        public Builder settime(String val) {
            time = val;
            return this;
        }

        public Builder setValue(String val) {
            Value = val;
            return this;
        }

        public WarningMessage build() {
            return new WarningMessage(this);
        }
    }
}
