package org.example.week4.enums;

public enum UploadDir {
    UPLOAD_DIR("uploads");

    private final String dir;

    UploadDir(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}
