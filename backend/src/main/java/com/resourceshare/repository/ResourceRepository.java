package com.resourceshare.repository;

import com.resourceshare.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {
    Page<Resource> findByStatus(String status, Pageable pageable);

    Page<Resource> findByUploaderId(Integer uploaderId, Pageable pageable);

    Page<Resource> findByUploaderIdAndStatus(Integer uploaderId, String status, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT r FROM Resource r WHERE r.status = 'APPROVED' " +
            "AND (:categoryId IS NULL OR r.categoryId = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "     OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Resource> searchApproved(@Param("keyword") String keyword,
                                  @Param("categoryId") Integer categoryId,
                                  Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.downloadCount), 0) FROM Resource r")
    long sumDownloadCount();

    @Query("SELECT COALESCE(SUM(r.viewCount), 0) FROM Resource r")
    long sumViewCount();
}
