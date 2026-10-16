package com.resourceshare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "age")
    private Integer age;

    @Column(name = "role", length = 20, nullable = false)
    private String role = "USER";

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
