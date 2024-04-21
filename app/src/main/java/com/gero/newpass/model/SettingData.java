package com.gero.newpass.model;

public class SettingData {
    private int image;
    private String name;
    private Boolean isSwitchPresent;
    private Boolean isSwitchEnabled; // This will track the switch state.
    private Boolean isImagePresent;

    public SettingData(int image, String name, Boolean isSwitchPresent, Boolean isSwitchEnabled, boolean isImagePresent) {
        this.image = image;
        this.name = name;
        this.isSwitchPresent = isSwitchPresent;
        this.isSwitchEnabled = isSwitchEnabled;
        this.isImagePresent = isImagePresent;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSwitchPresence() {
        return isSwitchPresent;
    }

    public void setSwitchPresent(Boolean switchPresent) {
        isSwitchPresent = switchPresent;
    }

    public Boolean getSwitchEnabled() {
        return isSwitchEnabled;
    }

    public void setSwitchEnabled(Boolean switchEnabled) {
        isSwitchEnabled = switchEnabled;
    }

    public Boolean getImagePresence() {
        return isImagePresent;
    }

    public void setImagePresent(Boolean imagePresent) {
        isImagePresent = imagePresent;
    }
}
