package com.wyc.mianshi_assistant.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.mianshi_assistant.annotation.AuthCheck;
import com.wyc.mianshi_assistant.common.BaseResponse;
import com.wyc.mianshi_assistant.common.DeleteRequest;
import com.wyc.mianshi_assistant.common.ErrorCode;
import com.wyc.mianshi_assistant.common.ResultUtils;
import com.wyc.mianshi_assistant.constant.UserConstant;
import com.wyc.mianshi_assistant.exception.BusinessException;
import com.wyc.mianshi_assistant.exception.ThrowUtils;
import com.wyc.mianshi_assistant.model.dto.question.QuestionQueryRequest;
import com.wyc.mianshi_assistant.model.dto.questionbank.QuestionBankAddRequest;
import com.wyc.mianshi_assistant.model.dto.questionbank.QuestionBankQueryRequest;
import com.wyc.mianshi_assistant.model.dto.questionbank.QuestionBankUpdateRequest;
import com.wyc.mianshi_assistant.model.entity.Question;
import com.wyc.mianshi_assistant.model.entity.QuestionBank;
import com.wyc.mianshi_assistant.model.entity.User;
import com.wyc.mianshi_assistant.model.vo.QuestionBankVO;
import com.wyc.mianshi_assistant.service.QuestionBankService;
import com.wyc.mianshi_assistant.service.QuestionService;
import com.wyc.mianshi_assistant.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题库接口
 */
@RestController
@RequestMapping("/questionBank")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题库
     *
     * @param questionBankAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionBank(@RequestBody QuestionBankAddRequest questionBankAddRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankAddRequest == null, ErrorCode.PARAMS_ERROR);
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankAddRequest, questionBank);
        // 数据校验
        questionBankService.validQuestionBank(questionBank, true);
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        questionBank.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionBankService.save(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionBankId = questionBank.getId();
        return ResultUtils.success(newQuestionBankId);
    }

    /**
     * 删除题库
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBank.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionBankService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题库（仅管理员可用）
     *
     * @param questionBankUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody QuestionBankUpdateRequest questionBankUpdateRequest) {
        if (questionBankUpdateRequest == null || questionBankUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankUpdateRequest, questionBank);
        // 数据校验
        questionBankService.validQuestionBank(questionBank, false);
        // 判断是否存在
        long id = questionBankUpdateRequest.getId();
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionBankService.updateById(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题库（封装类）
     *
     * @param questionBankQueryRequest
     * @return
     */
    @GetMapping("/get/vo")
    @SentinelResource(value = "getQuestionBankVOById"
            , blockHandler = "getQuestionBankVOByIdBlockHandler"
            , fallback = "getQuestionBankVOByIdFallback")
    public BaseResponse<QuestionBankVO> getQuestionBankVOById(QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request) {
        if (questionBankQueryRequest == null || questionBankQueryRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = questionBankQueryRequest.getId();

        // 查询数据库
        QuestionBank questionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        QuestionBankVO questionBankVO = questionBankService.getQuestionBankVO(questionBank, request);
        // 是否查询当前题库下的关联题目信息
        if (questionBankQueryRequest.getNeedQueryQuestionList() != null
                && questionBankQueryRequest.getNeedQueryQuestionList()) {
            // 调用 questionService 服务获取对应的题目分页
            QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
            questionQueryRequest.setQuestionBankId(id);
            Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
            questionBankVO.setQuestionPage(questionPage);
        }

        return ResultUtils.success(questionBankVO);
    }

    // getQuestionBankVOById 限流逻辑
    public BaseResponse<QuestionBankVO> getQuestionBankVOByIdBlockHandler(QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request
            , BlockException ex) {
        if (ex instanceof DegradeException) {
            return getQuestionBankVOByIdFallback(questionBankQueryRequest, request, ex);
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器繁忙，请稍后再试");
    }

    // getQuestionBankVOById 异常处理逻辑
    public BaseResponse<QuestionBankVO> getQuestionBankVOByIdFallback(QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request
            , Throwable ex) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "接口出现异常，马上恢复");
    }


    /**
     * 分页获取题库列表（仅管理员可用）
     *
     * @param questionBankQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBank>> listQuestionBankByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest) {
        long current = questionBankQueryRequest.getCurrent();
        long size = questionBankQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, size),
                questionBankService.getQueryWrapper(questionBankQueryRequest));
        return ResultUtils.success(questionBankPage);
    }

    /**
     * 分页获取题库列表（封装类）
     *
     * @param questionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @SentinelResource(value = "listQuestionBankVOByPage"
            , blockHandler = "listQuestionBankVOByPageBlockHandler"
            , fallback = "listQuestionBankVOByPageFallback")
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request) {
        long current = questionBankQueryRequest.getCurrent();
        long size = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, size),
                questionBankService.getQueryWrapper(questionBankQueryRequest));
        // 获取封装类
         return ResultUtils.success(questionBankService.getQuestionBankVOPage(questionBankPage, request));
    }

    // listQuestionBankVOByPage 限流逻辑
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPageBlockHandler(@RequestBody QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request
            , BlockException ex) {
        // 降级操作
        if (ex instanceof DegradeException) {
            return listQuestionBankVOByPageFallback(questionBankQueryRequest, request, ex);
        }
        // 流控操作
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务器繁忙，请稍后再试");
    }

    // listQuestionBankVOByPage 异常处理逻辑
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPageFallback(@RequestBody QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request
            , Throwable ex) {
        // 返回空数据
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "接口出现异常，马上恢复");
    }

    /**
     * 分页获取当前登录用户创建的题库列表
     *
     * @param questionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankVO>> listMyQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionBankQueryRequest.setUserId(loginUser.getId());
        long current = questionBankQueryRequest.getCurrent();
        long size = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, size),
                questionBankService.getQueryWrapper(questionBankQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionBankService.getQuestionBankVOPage(questionBankPage, request));
    }

    // endregion
}
