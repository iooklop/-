package com.resourceshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "resource_id", nullable = false)
    private Integer resourceId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "parent_id")
    private Integer parentId = 0;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Transient
    private String username;
}
