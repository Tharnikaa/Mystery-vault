package com.mysteryvault.util;

import java.io.*;

/**
 * Utility for robust binary object serialization and deserialization.
 * Owned by Member 3 (Module 9: Notes / Storage).
 */
public final class SerializerUtils {

    private SerializerUtils() {
        // utility class
    }

    /**
     * Serializes any Serializable object into a byte array.
     */
    public static byte[] serializeToBytes(Serializable object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("Object to serialize cannot be null");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            oos.flush();
            return baos.toByteArray();
        }
    }

    /**
     * Deserializes an object of specified type from a byte array.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserializeFromBytes(byte[] bytes, Class<T> clazz) throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Byte array cannot be null or empty");
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            if (!clazz.isInstance(obj)) {
                throw new ClassCastException("Deserialized object is not an instance of " + clazz.getName());
            }
            return (T) obj;
        }
    }

    /**
     * Serializes an object directly to a file.
     */
    public static void serializeToFile(Serializable object, File targetFile) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("Target file cannot be null");
        }
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(targetFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            oos.flush();
        }
    }

    /**
     * Deserializes an object from a file.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserializeFromFile(File sourceFile, Class<T> clazz) throws IOException, ClassNotFoundException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + sourceFile);
        }
        try (FileInputStream fis = new FileInputStream(sourceFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (!clazz.isInstance(obj)) {
                throw new ClassCastException("Deserialized object is not an instance of " + clazz.getName());
            }
            return (T) obj;
        }
    }
}
