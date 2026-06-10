package com.bank.admin.service;

import com.bank.admin.dto.request.SystemConfigRequest;
import com.bank.admin.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService {

    List<SystemConfig> listAll();

    SystemConfig getByKey(String configKey);

    String getValueByKey(String configKey);

    SystemConfig create(SystemConfigRequest request, Long operatorId);

    SystemConfig update(Long id, SystemConfigRequest request, Long operatorId);

    void delete(Long id);
}
