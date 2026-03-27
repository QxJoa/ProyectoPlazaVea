package com.plazavea.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.plazavea.api.model.LogLogin;

@Repository
public interface LogLoginRepository extends JpaRepository<LogLogin, Integer> {
}