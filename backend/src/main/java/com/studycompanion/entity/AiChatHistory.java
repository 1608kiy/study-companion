package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI聊天历史表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_history")
public class AiChatHistory extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 消息角色 (user/assistant)
     */
    @TableField("role")
    private String role;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;
}
