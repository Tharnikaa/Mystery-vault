package com.mysteryvault.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base domain model for an individual involved in a criminal investigation.
 * Free of UI, SQL, or database dependencies.
 */
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String role; // e.g. "Witness", "Suspect", "Victim", "Person of Interest"
    private String notes;

    public Person() {
    }

    public Person(String id, String name, String role) {
        this(id, name, role, "");
    }

    public Person(String id, String name, String role, String notes) {
        this.id = Objects.requireNonNull(id, "Person id cannot be null").trim();
        this.name = Objects.requireNonNull(name, "Person name cannot be null").trim();
        this.role = role != null ? role.trim() : "Person of Interest";
        this.notes = notes != null ? notes.trim() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "Person id cannot be null").trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Person name cannot be null").trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.trim() : "";
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Person{id='%s', name='%s', role='%s'}", id, name, role);
    }
}
