package com.bank.admin.service;

import com.bank.admin.dto.request.CreateAdminRequest;
import com.bank.admin.dto.request.UpdateUserStatusRequest;
import com.bank.admin.dto.request.UserQueryRequest;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.UserVO;

public interface AdminUserService {

    PageResult<UserVO> listUsers(UserQueryRequest request);

    UserVO getUserById(Long userId);

    void updateUserStatus(Long userId, UpdateUserStatusRequest request);

    String resetUserPassword(Long userId);

    void setUserPassword(Long userId, String newPassword);

    UserVO createAdmin(CreateAdminRequest request);

    void disableUser(Long userId);
}
