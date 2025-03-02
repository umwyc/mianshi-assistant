package com.wyc.mianshi_assistant.model.dto.question;

import lombok.Data;
import java.util.List;

import java.io.Serializable;

/**
 * 批量删除题目请求
 */
@Data
public class QuestionBatchDeleteRequest implements Serializable {

    /**
     * 题目 id 列表
     */
    private List<Long> questionIdList;

    private final static long serialVersionUID = 1L;
}
