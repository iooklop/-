package com.resourceshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reward")
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_no", length = 64, unique = true, nullable = false)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "resource_id", nullable = false)
    private Integer resourceId;

    @Column(name = "uploader_id", nullable = false)
    private Integer uploaderId;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;
}
