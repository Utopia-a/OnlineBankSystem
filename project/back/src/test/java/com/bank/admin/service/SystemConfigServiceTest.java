package com.bank.admin.service;

import com.bank.admin.dto.request.SystemConfigRequest;
import com.bank.admin.entity.SystemConfig;
import com.bank.admin.repository.SystemConfigRepository;
import com.bank.admin.service.impl.SystemConfigServiceImpl;
import com.bank.transaction.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("系统配置服务 - 单元测试")
class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;

    @Test
    @DisplayName("新增配置 - key不存在时成功创建")
    void create_whenKeyNotExists_success() {
        SystemConfigRequest request = new SystemConfigRequest();
        request.setConfigKey("test_key");
        request.setConfigValue("test_value");
        request.setDescription("测试配置");

        when(systemConfigRepository.existsByConfigKey("test_key")).thenReturn(false);
        SystemConfig saved = new SystemConfig();
        saved.setId(1L);
        saved.setConfigKey("test_key");
        saved.setConfigValue("test_value");
        when(systemConfigRepository.save(any())).thenReturn(saved);

        SystemConfig result = systemConfigService.create(request, 1L);

        assertThat(result.getConfigKey()).isEqualTo("test_key");
        verify(systemConfigRepository).save(any());
    }

    @Test
    @DisplayName("新增配置 - key已存在时抛出异常")
    void create_whenKeyExists_throwsException() {
        SystemConfigRequest request = new SystemConfigRequest();
        request.setConfigKey("existing_key");
        request.setConfigValue("value");

        when(systemConfigRepository.existsByConfigKey("existing_key")).thenReturn(true);

        assertThatThrownBy(() -> systemConfigService.create(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("配置键已存在");

        verify(systemConfigRepository, never()).save(any());
    }

    @Test
    @DisplayName("根据key获取配置值")
    void getValueByKey_whenExists_returnsValue() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("max_transfer_amount");
        config.setConfigValue("50000.00");

        when(systemConfigRepository.findByConfigKey("max_transfer_amount"))
                .thenReturn(Optional.of(config));

        String value = systemConfigService.getValueByKey("max_transfer_amount");

        assertThat(value).isEqualTo("50000.00");
    }

    @Test
    @DisplayName("根据key获取配置 - key不存在时返回null")
    void getValueByKey_whenNotExists_returnsNull() {
        when(systemConfigRepository.findByConfigKey("unknown_key"))
                .thenReturn(Optional.empty());

        String value = systemConfigService.getValueByKey("unknown_key");

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("删除配置 - ID不存在时抛出异常")
    void delete_whenNotExists_throwsException() {
        when(systemConfigRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> systemConfigService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("配置项不存在");

        verify(systemConfigRepository, never()).deleteById(any());
    }
}
