package com.example.webczar.chat_navigate.Structure;

/**
 * Created by webczar on 12/29/2017.
 */

public class Configuration {
    public String value;
    public String label;
    public int icon;

    public Configuration(String value, String label, int icon) {
        this.value = value;
        this.label = label;
        this.icon = icon;
    }

    public String getValue() {
        return this.value;
    }

    public String getLabel() {return this.label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getIcon() {
        return this.icon;
    }
}
