package com.evidencija.evidencija_backend.service;

import com.evidencija.evidencija_backend.model.Setting;
import com.evidencija.evidencija_backend.repository.SettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DomainValidator {

    private static final String KEY = "institution.domain";
    private static final String DEFAULT_DOMAIN = "test.local";

    private final SettingRepository settingRepository;

    public DomainValidator(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public void validate(String email) {
        String domain = getCurrentDomain();

        if (email == null || !email.toLowerCase().endsWith("@" + domain.toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Email does not belong to the allowed institutional domain: " + domain
            );
        }
    }

    // reading domain settings from database
    public String getCurrentDomain() {
        return settingRepository.findById(KEY)
                .map(Setting::getValue)
                .orElse(DEFAULT_DOMAIN);
    }
}