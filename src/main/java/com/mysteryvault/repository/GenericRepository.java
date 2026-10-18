package com.mysteryvault.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository abstraction for domain entities (Module 1).
 *
 * @param <T> Entity type
 * @param <ID> Primary identifier type
 */
public interface GenericRepository<T, ID> {

    /**
     * Saves or updates the entity in the store.
     *
     * @param entity Entity to save
     * @return The persisted entity
     */
    T save(T entity);

    /**
     * Finds an entity by its unique ID.
     *
     * @param id Identifier
     * @return Optional containing entity if found, empty Optional otherwise
     */
    Optional<T> findById(ID id);

    /**
     * Returns all entities in the store.
     *
     * @return List of all stored entities
     */
    List<T> findAll();

    /**
     * Deletes the entity matching the given ID.
     *
     * @param id Identifier
     * @return true if deleted, false if entity was not present
     */
    boolean deleteById(ID id);

    /**
     * Checks if an entity exists for the given ID.
     *
     * @param id Identifier
     * @return true if entity exists, false otherwise
     */
    boolean existsById(ID id);

    /**
     * Returns the total count of entities in the store.
     *
     * @return Total count
     */
    long count();
}
