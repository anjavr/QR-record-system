package com.evidencija.evidencija_backend.exception;

import com.evidencija.evidencija_backend.dto.UploadConflictDto;

public class UploadConflictException extends RuntimeException {
    private final UploadConflictDto conflict;

    public UploadConflictException(UploadConflictDto conflict) {
        this.conflict = conflict;
    }

    public UploadConflictDto getConflict() { return conflict; }
}