package com.wyc.mianshi_assistant.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.mianshi_assistant.common.ErrorCode;
import com.wyc.mianshi_assistant.constant.CommonConstant;
import com.wyc.mianshi_assistant.exception.BusinessException;
import com.wyc.mianshi_assistant.exception.ThrowUtils;
import com.wyc.mianshi_assistant.mapper.QuestionBankQuestionMapper;
import com.wyc.mianshi_assistant.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.wyc.mianshi_assistant.model.entity.Question;
import com.wyc.mianshi_assistant.model.entity.QuestionBank;
import com.wyc.mianshi_assistant.model.entity.QuestionBankQuestion;
import com.wyc.mianshi_assistant.model.entity.User;
import com.wyc.mianshi_assistant.model.vo.QuestionBankQuestionVO;
import com.wyc.mianshi_assistant.service.QuestionBankQuestionService;
import com.wyc.mianshi_assistant.service.QuestionBankService;
import com.wyc.mianshi_assistant.service.QuestionService;
import com.wyc.mianshi_assistant.service.UserService;
import com.wyc.mianshi_assistant.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;

    private static final int BATCH_SIZE = 1000;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        Long questionId = questionBankQuestion.getQuestionId();
        Long userId = questionBankQuestion.getUserId();

        // 创建数据时，参数不能为空
        if (add) {
            if(ObjectUtil.isAllNotEmpty(questionBankId, questionId, userId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 数据库校验
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR);
        }

    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();

        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        questionBankQuestionVO.setUserVO(userService.getUserVO(user));
        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 关联用户
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream()
                .map(questionBankQuestion -> getQuestionBankQuestionVO(questionBankQuestion, request))
                .collect(Collectors.toList());
        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    /**
     * 向题库中批量添加题目
     *
     * @param questionIdList
     * @param questionBankId
     * @param loginUser
     */
    @Override
    public void batchAddQuestionsToBank(List<Long> questionIdList, Long questionBankId, User loginUser) {
        // 简单校验
        ThrowUtils.throwIf(questionIdList == null || questionIdList.isEmpty(), ErrorCode.PARAMS_ERROR, "题目 id 列表错误");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库 id 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "用户未登录");

        // 数据库校验
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)    // 只查询特定字段，减少内存占用
                .in(Question::getId, questionIdList);
        // 第一次过滤（过滤出所有待添加的题目 id 得到 validQuestionIdList）
        List<Long> validQuestionIdList = questionService.listObjs(questionLambdaQueryWrapper, object -> (Long) object); // 使用 listObjs 方法直接将查询结果转换成 id 列表
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "题目不存在");
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR, "题库不存在");
        User user = userService.getById(loginUser.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");

        // 查询已经添加到题库中的题目得到 existQuestionIdSet
        LambdaQueryWrapper<QuestionBankQuestion> questionBankQuestionLambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)  // 使用 listObjs 方法直接将查询结果转换成 id 列表
                .select(QuestionBankQuestion::getQuestionId)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<Long> existQuestionIdList = this.listObjs(questionBankQuestionLambdaQueryWrapper, object -> (Long) object);
        Set<Long> existQuestionIdSet = existQuestionIdList.stream()
                .collect(Collectors.toSet());

        // 第二次过滤（过滤掉已经添加到题库中的题目）
        validQuestionIdList = validQuestionIdList.stream()
                .filter(questionId -> !existQuestionIdSet.contains(questionId))
                .collect(Collectors.toList());

        // 执行插入
        int total = validQuestionIdList.size();
        log.info("batch add questions to bank start, total: {}", total);
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor( // 自定义线程池
                12,
                48,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        List<CompletableFuture<Void>> futures = new LinkedList<>();  // 定义一个列表用于保存所有批次的 CompletableFuture
        for (int i = 0; i < total; i += BATCH_SIZE) {   // 避免长事务，一次添加 1000 条数据
            // 添加的数据不能越界
            int end = Math.min(i + BATCH_SIZE, total);
            List<Long> subList = validQuestionIdList.subList(i, end);
            List<QuestionBankQuestion> questionBankQuestionList = subList.stream()
                    .map(questionId -> {
                        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                        questionBankQuestion.setQuestionBankId(questionBankId);
                        questionBankQuestion.setQuestionId(questionId);
                        questionBankQuestion.setUserId(loginUser.getId());
                        return questionBankQuestion;
                    })
                    .collect(Collectors.toList());
            // 批量添加
            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl)AopContext.currentProxy();   // 获取接口的代理对象才能使用事务的回滚操作
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> { // 异步执行内部方法
                questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestionList);
            }, customExecutor).exceptionally(ex -> {    // 捕获异常
                log.error("向题库添加题目失败, 错误信息: {}", ex.getMessage());
                return null;
            });
            futures.add(future);
        }

        // 等待所有批次操作完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 关闭线程池
        customExecutor.shutdown();
    }

    /**
     * 向题库中批量添加题目（仅内部调用）
     *
     * @param questionBankQuestionList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestionList) {
        try {
            boolean result = this.saveBatch(questionBankQuestionList);  // 使用批处理减少网络请求
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
            }
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }

    /**
     * 从题库中批量删除题目
     *
     * @param questionIdList
     * @param questionBankId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId) {
        // 简单校验
        ThrowUtils.throwIf(questionIdList == null || questionIdList.isEmpty(), ErrorCode.PARAMS_ERROR, "题目 id 列表错误");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库 id 错误");

        // 数据库校验
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIdList);
        // 第一次过滤（过滤出所有待移除的题目 id 得到 validQuestionIdList）
        List<Long> validQuestionIdList = questionService.listObjs(questionLambdaQueryWrapper, object -> (Long) object);
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "题目不存在");
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR, "题库不存在");

        // 第二次过滤（过滤出所有在题库中的题目 id 得到 validQuestionBankQuestionIdList）
        LambdaQueryWrapper<QuestionBankQuestion> questionBankQuestionLambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .select(QuestionBankQuestion::getId)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<Long> validQuestionBankQuestionIdList = this.listObjs(questionBankQuestionLambdaQueryWrapper, object -> (Long) object);

        // 所有待删除的题目都不在指定的题库中
        if (CollUtil.isEmpty(validQuestionBankQuestionIdList)) {
            return;
        }

        // 执行删除操作
        boolean removed = this.removeBatchByIds(validQuestionBankQuestionIdList);
        ThrowUtils.throwIf(!removed, ErrorCode.OPERATION_ERROR, "数据库异常，删除题目失败");
    }

}
