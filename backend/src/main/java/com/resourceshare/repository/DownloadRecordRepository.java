package com.resourceshare.repository;

import com.resourceshare.entity.DownloadRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadRecordRepository extends JpaRepository<DownloadRecord, Integer> {
    Page<DownloadRecord> findByUserId(Integer userId, Pageable pageable);

    List<DownloadRecord> findByUserIdOrderByIdDesc(Integer userId);

    boolean existsByUserIdAndResourceId(Integer userId, Integer resourceId);
}
