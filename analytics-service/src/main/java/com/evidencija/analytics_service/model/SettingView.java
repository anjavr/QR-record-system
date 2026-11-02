package com.evidencija.analytics_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class SettingView {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value")
    private String value;

    public String getKey() { return key; }
    public String getValue() { return value; }
}