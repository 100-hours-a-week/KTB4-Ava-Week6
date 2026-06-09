package org.example.week4.enums;

public enum ApiResultStatus {
    SUCCESS(true),
    FAIL(false);

    private final boolean success;

    ApiResultStatus(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
