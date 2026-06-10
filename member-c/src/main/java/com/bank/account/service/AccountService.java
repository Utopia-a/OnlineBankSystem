package com.bank.account.service;

import com.bank.account.dto.*;
import com.bank.account.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户管理服务接口
 */
public interface AccountService {

    /**
     * 创建账户
     *
     * @param userId  当前登录用户ID
     * @param request 创建账户请求
     * @return 账户信息
     */
    AccountResponse createAccount(Long userId, CreateAccountRequest request);

    /**
     * 查询账户详情（按账户ID）
     *
     * @param userId    当前登录用户ID（用于权限校验）
     * @param accountId 账户ID
     * @return 账户信息
     */
    AccountResponse getAccountById(Long userId, Long accountId);

    /**
     * 查询账户详情（按账户号码）
     *
     * @param userId        当前登录用户ID（用于权限校验）
     * @param accountNumber 账户号码
     * @return 账户信息
     */
    AccountResponse getAccountByNumber(Long userId, String accountNumber);

    /**
     * 查询当前用户所有账户
     *
     * @param userId 当前登录用户ID
     * @return 账户列表
     */
    List<AccountResponse> getAccountsByUserId(Long userId);

    /**
     * 分页查询当前用户账户
     *
     * @param userId   当前登录用户ID
     * @param pageable 分页参数
     * @return 分页账户列表
     */
    Page<AccountResponse> getAccountsByUserIdPaged(Long userId, Pageable pageable);

    /**
     * 更新账户信息（别名、限额等）
     *
     * @param userId    当前登录用户ID
     * @param accountId 账户ID
     * @param request   更新请求
     * @return 更新后的账户信息
     */
    AccountResponse updateAccount(Long userId, Long accountId, UpdateAccountRequest request);

    /**
     * 变更账户状态（冻结/激活/注销）
     *
     * @param userId    当前登录用户ID（普通用户只能操作自己的账户，管理员可操作任意）
     * @param accountId 账户ID
     * @param request   状态变更请求
     * @return 更新后的账户信息
     */
    AccountResponse changeAccountStatus(Long userId, Long accountId, AccountStatusRequest request);

    /**
     * 管理员变更账户状态（不受用户ID限制）
     *
     * @param accountId 账户ID
     * @param request   状态变更请求
     * @return 更新后的账户信息
     */
    AccountResponse adminChangeAccountStatus(Long accountId, AccountStatusRequest request);

    /**
     * 查询账户余额
     *
     * @param userId        当前登录用户ID
     * @param accountNumber 账户号码
     * @return 余额信息
     */
    BalanceResponse getBalance(Long userId, String accountNumber);

    /**
     * 内部余额更新接口（供成员 D 交易服务调用）
     *
     * @param request 余额更新请求
     */
    void updateBalanceInternal(InternalBalanceUpdateRequest request);

    /**
     * 验证账户是否可以进行交易（供成员 D 调用）
     *
     * @param accountNumber 账户号码
     * @param amount        交易金额
     * @return 是否可以交易
     */
    boolean validateAccountForTransaction(String accountNumber, BigDecimal amount);

    /**
     * 管理员查询所有账户（分页）
     *
     * @param pageable 分页参数
     * @return 分页账户列表
     */
    Page<AccountResponse> adminGetAllAccounts(Pageable pageable);

    /**
     * 管理员根据用户ID查询账户
     *
     * @param userId   目标用户ID
     * @param pageable 分页参数
     * @return 分页账户列表
     */
    Page<AccountResponse> adminGetAccountsByUserId(Long userId, Pageable pageable);
}
