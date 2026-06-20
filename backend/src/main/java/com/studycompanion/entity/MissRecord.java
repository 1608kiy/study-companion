package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 断签记录表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("miss_record")
public class MissRecord extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 断签日期
     */
    @TableField("miss_date")
    private LocalDate missDate;

    /**
     * 断签原因
     */
    @TableField("reason")
    private String reason;

    /**
     * AI判断是否允许补签(0不允许 1允许)
     */
    @TableField("ai_allow_replenish")
    private Integer aiAllowReplenish;

    /**
     * 是否已补签(0未补签 1已补签)
     */
    @TableField("is_replenished")
    private Integer isReplenished;
}
