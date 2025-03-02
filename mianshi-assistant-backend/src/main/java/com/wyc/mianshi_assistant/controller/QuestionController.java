package com.wyc.mianshi_assistant.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.wyc.mianshi_assistant.annotation.AuthCheck;
import com.wyc.mianshi_assistant.common.BaseResponse;
import com.wyc.mianshi_assistant.common.DeleteRequest;
import com.wyc.mianshi_assistant.common.ErrorCode;
import com.wyc.mianshi_assistant.common.ResultUtils;
import com.wyc.mianshi_assistant.constant.UserConstant;
import com.wyc.mianshi_assistant.exception.BusinessException;
import com.wyc.mianshi_assistant.exception.ThrowUtils;
import com.wyc.mianshi_assistant.model.dto.question.QuestionAddRequest;
import com.wyc.mianshi_assistant.model.dto.question.QuestionBatchDeleteRequest;
import com.wyc.mianshi_assistant.model.dto.question.QuestionQueryRequest;
import com.wyc.mianshi_assistant.model.dto.question.QuestionUpdateRequest;
import com.wyc.mianshi_assistant.model.entity.Question;
import com.wyc.mianshi_assistant.model.entity.QuestionBankQuestion;
import com.wyc.mianshi_assistant.model.entity.User;
import com.wyc.mianshi_assistant.model.vo.QuestionVO;
import com.wyc.mianshi_assistant.service.QuestionService;
import com.wyc.mianshi_assistant.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    // hotkey 的题目缓存前缀
    private static final String HOTKEY_QUESTION_CACHE_PREFIX = "question_detail_";

    // region 增删改查

    /**
     * 创建题目（仅管理员可用）
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 封装类和实体类转换
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        question.setTags(JSONUtil.toJsonStr(questionAddRequest.getTags()));
        // 数据校验
        questionService.validQuestion(question, true);
        // 关联用户
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目（仅管理员可用）
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/remove")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> removeQuestion(@RequestBody DeleteRequest deleteRequest
        , HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Boolean result = questionService.removeQuestion(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 批量删除题目（仅管理员可用）
     *
     * @param questionBatchDeleteRequest
     * @return
     */
    @PostMapping("/delete/batch")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchDeleteQuestions(@RequestBody QuestionBatchDeleteRequest questionBatchDeleteRequest) {
        ThrowUtils.throwIf(questionBatchDeleteRequest == null, ErrorCode.PARAMS_ERROR, "传入参数为空");
        questionService.batchDeleteQuestions(questionBatchDeleteRequest.getQuestionIdList());
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 封装类和实体类类型转换
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        question.setTags(JSONUtil.toJsonStr(questionUpdateRequest.getTags()));
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    // endregion


    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @SentinelResource(value = "getQuestionVOById"
            ,blockHandler = "getQuestionVOByIdBlockHandler"
            ,fallback = "getQuestionVOByIdFallback")
    public BaseResponse<QuestionVO> getQuestionVOById(long id
            , HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        // 获取 key
        String key = HOTKEY_QUESTION_CACHE_PREFIX + id;
        // 判断是否是 hotkey
        if(JdHotKeyStore.isHotKey(key)) {
            // 获取缓存
            Object cachedQuestion = JdHotKeyStore.get(key);
            // 如果已经缓存了，直接返回结果
            if(cachedQuestion != null) {
                return ResultUtils.success((QuestionVO) cachedQuestion);
            }
        }

        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);

        // 设置 hotkey 缓存
        JdHotKeyStore.smartSet(key, questionService.getQuestionVO(question, request));

        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    // getQuestionVOById 限流逻辑
    public BaseResponse<QuestionVO> getQuestionVOByIdBlockHandler(long id
            , HttpServletRequest request
            , BlockException ex) {
        // 降级操作
        if (ex instanceof DegradeException) {
            getQuestionVOByIdFallback(id, request, ex);
        }
        // 限流操作
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器繁忙，请稍后再试");
    }

    // getQuestionVOById 异常处理逻辑
    public BaseResponse<QuestionVO> getQuestionVOByIdFallback(long id
            , HttpServletRequest request
            , BlockException ex) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "接口出现异常，马上恢复");
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        // 查询数据
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @SentinelResource(value = "listQuestionVOByPage"
            ,blockHandler = "listQuestionVOByPageBlockHandler"
            ,fallback = "listQuestionVOByPageFallback")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest
            , HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    // listQuestionVOByPage 限流逻辑
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPageBlockHandler(@RequestBody QuestionQueryRequest questionQueryRequest
            , HttpServletRequest request
            , BlockException ex) {
        // 降级操作
        if (ex instanceof DegradeException) {
            listQuestionVOByPageFallback(questionQueryRequest, request, ex);
        }
        // 限流操作
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器繁忙，请稍后再试");
    }

    // listQuestionVOByPage 异常处理逻辑
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPageFallback(@RequestBody QuestionQueryRequest questionQueryRequest
            , HttpServletRequest request
            , Throwable ex) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "接口出现异常，马上恢复");
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 使用 es 搜索题目 (基于 IP 限流)
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<QuestionVO>> searchQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest
            , HttpServletRequest request) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        Entry entry = null;
        // 获取用户 IP 地址
        String remoteAddr = request.getRemoteAddr();
        try {
            entry = SphU.entry("searchQuestionVOByPage", EntryType.IN, 1, remoteAddr);
            // 被保护的业务逻辑
            Page<Question> questionPage = questionService.searchFromEs(questionQueryRequest);
            // 实体类转包装类
            return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
        } catch (Throwable ex) {
            // 上报业务异常
            if (!(ex instanceof BlockException)) {
                Tracer.trace(ex);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, ex.getMessage());
            }
            // 异常处理逻辑
            if (ex instanceof DegradeException) {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "接口出现异常，马上恢复");
            }
            // 限流逻辑
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器繁忙，请稍后再试");
        } finally {
            if (entry != null) {
                entry.exit(1, remoteAddr);
            }
        }
    }

}
