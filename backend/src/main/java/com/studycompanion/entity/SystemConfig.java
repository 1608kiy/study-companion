package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_config")
public class SystemConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    private String description;
}
