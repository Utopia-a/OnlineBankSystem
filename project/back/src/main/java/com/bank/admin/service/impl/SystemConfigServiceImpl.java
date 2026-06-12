package com.bank.admin.service.impl;

import com.bank.admin.dto.request.SystemConfigRequest;
import com.bank.admin.entity.SystemConfig;
import com.bank.admin.repository.SystemConfigRepository;
import com.bank.admin.service.SystemConfigService;
import com.bank.admin.support.AdminAuditHelper;
import com.bank.transaction.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final AdminAuditHelper adminAuditHelper;

    @Override
    public List<SystemConfig> listAll() {
        return systemConfigRepository.findAll();
    }

    @Override
    public SystemConfig getByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException(400, "配置项不存在: " + configKey));
    }

    @Override
    public String getValueByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    @Override
    @Transactional
    public SystemConfig create(SystemConfigRequest request, Long operatorId) {
        if (systemConfigRepository.existsByConfigKey(request.getConfigKey())) {
            throw new BusinessException(400, "配置键已存在: " + request.getConfigKey());
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(operatorId);

        SystemConfig saved = systemConfigRepository.save(config);
        adminAuditHelper.log("系统配置", "新增配置",
                "CONFIG", saved.getConfigKey(), saved.getConfigValue());
        log.info("管理员新增系统配置: key={}, operatorId={}", request.getConfigKey(), operatorId);
        return saved;
    }

    @Override
    @Transactional
    public SystemConfig update(Long id, SystemConfigRequest request, Long operatorId) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "配置项不存在，ID: " + id));

        if (!config.getConfigKey().equals(request.getConfigKey())) {
            if (systemConfigRepository.existsByConfigKey(request.getConfigKey())) {
                throw new BusinessException(400, "配置键已存在: " + request.getConfigKey());
            }
            config.setConfigKey(request.getConfigKey());
        }

        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(operatorId);

        SystemConfig saved = systemConfigRepository.save(config);
        adminAuditHelper.log("系统配置", "更新配置",
                "CONFIG", String.valueOf(id), request.getConfigKey() + "=" + request.getConfigValue());
        log.info("管理员更新系统配置: id={}, key={}, operatorId={}", id, request.getConfigKey(), operatorId);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!systemConfigRepository.existsById(id)) {
            throw new BusinessException(400, "配置项不存在，ID: " + id);
        }
        systemConfigRepository.deleteById(id);
        adminAuditHelper.log("系统配置", "删除配置", "CONFIG", String.valueOf(id), null);
        log.info("管理员删除系统配置: id={}", id);
    }
}
