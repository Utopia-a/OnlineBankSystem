package com.banking.report.repository;

import com.banking.report.entity.AccountSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountSummary, Long> {

    Optional<AccountSummary> findByAccountNo(String accountNo);

    List<AccountSummary> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long accountId, Long userId);
}
