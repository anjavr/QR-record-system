package com.evidencija.evidencija_backend.controller;

import com.evidencija.evidencija_backend.model.Setting;
import com.evidencija.evidencija_backend.repository.SettingRepository;
import com.evidencija.evidencija_backend.dto.SettingUpdateRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingRepository settingRepository;

    public SettingsController(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @GetMapping("/{key}")
    public Setting getSetting(@PathVariable String key) {
        return settingRepository.findById(key)
                .orElse(new Setting(key, null));
    }

    @PutMapping("/{key}")
    public Setting updateSetting(@PathVariable String key, @RequestBody SettingUpdateRequest request) {
        Setting setting = new Setting(key, request.getValue());
        return settingRepository.save(setting);
    }
}