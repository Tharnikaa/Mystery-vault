package com.mysteryvault.service;

import com.mysteryvault.model.InvestigationNote;
import com.mysteryvault.util.SerializerUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Service managing persistent storage for text notes, reports, and serializable states.
 * Owned by Member 3 (Module 9: Notes / Storage).
 * Uses configurable paths and try-with-resources.
 */
public class FileStorageService {

    private final Path baseStorageDirectory;

    public FileStorageService() {
        this(Paths.get(System.getProperty("user.home"), ".mysteryvault", "storage"));
    }

    public FileStorageService(Path baseStorageDirectory) {
        this.baseStorageDirectory = Objects.requireNonNull(baseStorageDirectory, "baseStorageDirectory cannot be null");
        initDirectory(this.baseStorageDirectory);
    }

    private void initDirectory(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage directory: " + path, e);
        }
    }

    public Path getBaseStorageDirectory() {
        return baseStorageDirectory;
    }

    /**
     * Saves a text note in UTF-8 format.
     */
    public Path saveTextNote(InvestigationNote note) throws IOException {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }
        Path notesDir = baseStorageDirectory.resolve("notes");
        initDirectory(notesDir);

        Path noteFile = notesDir.resolve(note.getCaseId() + "_" + note.getId() + ".txt");
        String content = "TITLE: " + note.getTitle() + "\n" +
                         "AUTHOR: " + note.getAuthor() + "\n" +
                         "CASE_ID: " + note.getCaseId() + "\n" +
                         "CREATED: " + note.getCreatedAt() + "\n" +
                         "UPDATED: " + note.getUpdatedAt() + "\n" +
                         "--------------------------------------------------\n" +
                         note.getContent();

        Files.writeString(noteFile, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return noteFile;
    }

    /**
     * Saves an investigation report as formatted UTF-8 text.
     */
    public Path saveReportText(String caseId, String reportId, String formattedReportText) throws IOException {
        Path reportsDir = baseStorageDirectory.resolve("reports");
        initDirectory(reportsDir);

        Path reportFile = reportsDir.resolve(caseId + "_" + reportId + ".txt");
        Files.writeString(reportFile, formattedReportText, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return reportFile;
    }

    /**
     * Reads a text file safely in UTF-8.
     */
    public String readTextFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * Serializes any serializable object to a named file.
     */
    public Path saveObject(String filename, Serializable object) throws IOException {
        Path dataDir = baseStorageDirectory.resolve("data");
        initDirectory(dataDir);

        Path target = dataDir.resolve(filename);
        SerializerUtils.serializeToFile(object, target.toFile());
        return target;
    }

    /**
     * Deserializes an object of specified class from a named file.
     */
    public <T extends Serializable> T loadObject(String filename, Class<T> clazz) throws IOException, ClassNotFoundException {
        Path target = baseStorageDirectory.resolve("data").resolve(filename);
        return SerializerUtils.deserializeFromFile(target.toFile(), clazz);
    }

    /**
     * Lists all saved note files for a case.
     */
    public List<Path> listNoteFiles(String caseId) throws IOException {
        Path notesDir = baseStorageDirectory.resolve("notes");
        if (!Files.exists(notesDir)) {
            return Collections.emptyList();
        }
        try (var stream = Files.list(notesDir)) {
            return stream.filter(p -> p.getFileName().toString().startsWith(caseId + "_")).toList();
        }
    }
}
