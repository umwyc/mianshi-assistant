package com.wyc.mianshi_assistant.job.once;

import cn.hutool.core.collection.CollUtil;
import com.wyc.mianshi_assistant.esdao.QuestionEsDao;
import com.wyc.mianshi_assistant.model.dto.question.QuestionEsDTO;
import com.wyc.mianshi_assistant.model.entity.Question;
import com.wyc.mianshi_assistant.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目定时分页全量同步
 */
@Component
@Slf4j
public class FullSyncQuestionToEs implements CommandLineRunner {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    private static final int PAGE_SIZE = 500;

    @Override
    public void run(String... args) throws Exception {
        // 全量查询
        List<Question> questionList = questionService.list();
        if(CollUtil.isEmpty(questionList)) {
            return;
        }
        // 实体类转包装类
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());

        int total = questionEsDTOList.size();
        // 全量同步开始
        log.info("fullSyncQuestionToEs start, total: {}", total);
        for(int i = 0; i < total; i += PAGE_SIZE) { // 使用批处理减少网络请求
            // 同步的数据下标不能超过总数据量
            int end = Math.min(i + PAGE_SIZE, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));
        }
        // 全量同步结束
        log.info("fullSyncQuestionToEs end, total: {}", total);
    }
}
