package com.resourceshare.repository;

import com.resourceshare.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {
    Optional<Reward> findByOrderNo(String orderNo);

    List<Reward> findByUserIdOrderByIdDesc(Integer userId);

    List<Reward> findByUploaderIdOrderByIdDesc(Integer uploaderId);
}
