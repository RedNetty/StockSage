package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Image entity
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Find images by entity type and entity ID
     */
    List<Image> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find images by entity type and entity ID, ordered by sort order
     */
    List<Image> findByEntityTypeAndEntityIdOrderBySortOrderAsc(String entityType, Long entityId);

    /**
     * Find active images by entity type and entity ID
     */
    List<Image> findByEntityTypeAndEntityIdAndActiveTrue(String entityType, Long entityId);

    /**
     * Delete images by entity type and entity ID
     */
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find images by filename
     */
    Image findByFilename(String filename);

    /**
     * Check if an image exists by entity type, entity ID, and filename
     */
    boolean existsByEntityTypeAndEntityIdAndFilename(String entityType, Long entityId, String filename);

    /**
     * Find images by path containing the specified string
     */
    @Query("SELECT i FROM Image i WHERE i.path LIKE %:pathPart%")
    List<Image> findByPathContaining(@Param("pathPart") String pathPart);
}