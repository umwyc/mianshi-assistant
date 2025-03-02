package com.wyc.mianshi_assistant.model.dto.questionbankquestion;

import lombok.Data;
import java.util.List;

import java.io.Serializable;

/**
 * 批量添加题库题目关系请求
 */
@Data
public class QuestionBankQuestionBatchAddRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id 列表
     */
    private List<Long> questionIdList;

    private static final long serialVersionUID = 1L;
}

