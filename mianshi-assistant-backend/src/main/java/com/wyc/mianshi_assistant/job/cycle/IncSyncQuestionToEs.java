package com.wyc.mianshi_assistant.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.wyc.mianshi_assistant.esdao.QuestionEsDao;
import com.wyc.mianshi_assistant.mapper.QuestionMapper;
import com.wyc.mianshi_assistant.model.dto.question.QuestionEsDTO;
import com.wyc.mianshi_assistant.model.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * 题目定时分页增量同步
 */
@Component
@Slf4j
public class IncSyncQuestionToEs {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionEsDao questionEsDao;

    private static final int PAGE_SIZE = 500;

    private static final long FIVE_MINUTES = 5 * 60 * 1000L;

    /**
     * 每两分钟执行一次
     */
    @Scheduled(fixedRate = 120 * 1000)
    public void run() {
        // 查询近五分钟更新过的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - FIVE_MINUTES);
        List<Question> questionList = questionMapper.listQuestionWithDelete(fiveMinutesAgoDate);
        if(CollUtil.isEmpty(questionList)) {
            log.info("no inc question to sync");
            return;
        }
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());

        // 增量同步开始
        int total = questionEsDTOList.size();
        log.info("incSyncQuestionToEs start, total = {}", total);
        for(int i = 0; i < total; i += PAGE_SIZE) { // 使用批处理减少网络请求
            // 同步的数据下标不能超过总数据量
            int end = Math.min(i + PAGE_SIZE, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));
        }
        // 增量同步结束
        log.info("incSyncQuestionToEs end, total = {}", total);
    }

}
