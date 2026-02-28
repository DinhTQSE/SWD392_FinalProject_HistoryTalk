package com.historyTalk.entity;

/**
 * Document file format enum
 * Supported formats for historical context documents: PDF, TXT, DOCX
 */
public enum DocumentFileFormat {
    PDF("application/pdf"),
    TXT("text/plain"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final String mimeType;

    DocumentFileFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static DocumentFileFormat fromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
        try {
            return DocumentFileFormat.valueOf(extension);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }
}
