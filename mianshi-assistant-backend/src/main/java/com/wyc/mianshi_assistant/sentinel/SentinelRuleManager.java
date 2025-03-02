package com.wyc.mianshi_assistant.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class SentinelRuleManager {

    @PostConstruct
    public void init() {
        initParamFlowRule();
        initFlowRule();
        initDegradeRule();
    }

    /**
     * 根据 IP 限流
     */
    public void initParamFlowRule() {
        List<ParamFlowRule> rules = new ArrayList<>();

        // 初始化用户登录接口的相关规则
        ParamFlowRule userLoginParamFlowRule = new ParamFlowRule();
        userLoginParamFlowRule.setResource("userLogin");    // 设置资源名
        userLoginParamFlowRule.setParamIdx(0);  // 设置根据第一个参数（IP）进行限流
        userLoginParamFlowRule.setCount(60);    // 设置阈值（一分钟只能调用 60 次）
        userLoginParamFlowRule.setDurationInSec(60);    //  设置统计时长
        rules.add(userLoginParamFlowRule);

        // 初始化用户注册接口的相关规则
        ParamFlowRule userRegisterParamFlowRule = new ParamFlowRule();
        userRegisterParamFlowRule.setResource("userRegister");    // 设置资源名
        userRegisterParamFlowRule.setParamIdx(0);  // 设置根据第一个参数（IP）进行限流
        userRegisterParamFlowRule.setCount(6);    // 设置阈值（一分钟只能调用 6 次）
        userRegisterParamFlowRule.setDurationInSec(60);    //  设置统计时长
        rules.add(userRegisterParamFlowRule);

        // 初始化用户登录接口的相关规则
        ParamFlowRule searchQuestionVOByPageFlowRule = new ParamFlowRule();
        searchQuestionVOByPageFlowRule.setResource("searchQuestionVOByPage");    // 设置资源名
        searchQuestionVOByPageFlowRule.setParamIdx(0);  // 设置根据第一个参数（IP）进行限流
        searchQuestionVOByPageFlowRule.setCount(60);    // 设置阈值（一分钟只能调用 60 次）
        searchQuestionVOByPageFlowRule.setDurationInSec(60);    //  设置统计时长
        rules.add(searchQuestionVOByPageFlowRule);

        ParamFlowRuleManager.loadRules(rules);
    }

    /**
     * 根据 QPS 限流
     */
    public void initFlowRule() {
        List<FlowRule> list = new ArrayList<>();

        // 初始化获取题目详情接口的策略
        FlowRule getQuestionVOByIdFlowRule = new FlowRule();
        getQuestionVOByIdFlowRule.setResource("getQuestionVOById"); // 设置资源名
        getQuestionVOByIdFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);    // 设置根据 QPS 流控
        getQuestionVOByIdFlowRule.setCount(20); // 设置阈值
        getQuestionVOByIdFlowRule.setControlBehavior(0);  // 设置流控效果为直接失败
        list.add(getQuestionVOByIdFlowRule);

        // 初始化获取题目分页接口的策略
        FlowRule listQuestionVOByPage = new FlowRule();
        listQuestionVOByPage.setResource("listQuestionVOByPage"); // 设置资源名
        listQuestionVOByPage.setGrade(RuleConstant.FLOW_GRADE_QPS);    // 设置根据 QPS 流控
        listQuestionVOByPage.setCount(20); // 设置阈值
        listQuestionVOByPage.setControlBehavior(0);  // 设置流控效果为直接失败
        list.add(listQuestionVOByPage);

        // 初始化获取题库详情接口的策略
        FlowRule getQuestionBankVOByIdFlowRule = new FlowRule();
        getQuestionBankVOByIdFlowRule.setResource("getQuestionBankVOById"); // 设置资源名
        getQuestionBankVOByIdFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);    // 设置根据 QPS 流控
        getQuestionBankVOByIdFlowRule.setCount(20); // 设置阈值
        getQuestionBankVOByIdFlowRule.setControlBehavior(0);  // 设置流控效果为直接失败
        list.add(getQuestionBankVOByIdFlowRule);

        // 初始化获取题库分页接口的策略
        FlowRule listQuestionBankVOByPage = new FlowRule();
        listQuestionBankVOByPage.setResource("listQuestionBankVOByPage"); // 设置资源名
        listQuestionBankVOByPage.setGrade(RuleConstant.FLOW_GRADE_QPS);    // 设置根据 QPS 流控
        listQuestionBankVOByPage.setCount(20); // 设置阈值
        listQuestionBankVOByPage.setControlBehavior(0);  // 设置流控效果为直接失败
        list.add(listQuestionBankVOByPage);

        FlowRuleManager.loadRules(list);
    }

    /**
     * 降级策略初始化
     */
    public void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();

        // 初始化获取题目详情接口的策略
        DegradeRule getQuestionVOByIdErrorRatioRule = new DegradeRule();
        getQuestionVOByIdErrorRatioRule.setResource("getQuestionVOById");  // 设置资源名
        getQuestionVOByIdErrorRatioRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());  // 设置根据异常比例降级
        getQuestionVOByIdErrorRatioRule.setCount(0.1); // 设置阈值
        getQuestionVOByIdErrorRatioRule.setMinRequestAmount(10);  // 设置最小请求数
        getQuestionVOByIdErrorRatioRule.setTimeWindow(5);  // 设置熔断时间
        getQuestionVOByIdErrorRatioRule.setStatIntervalMs(10 * 1000);   // 设置统计时长
        rules.add(getQuestionVOByIdErrorRatioRule);

        DegradeRule getQuestionVOByIdSlowCallRule =  new DegradeRule();
        getQuestionVOByIdSlowCallRule.setResource("getQuestionVOById");  // 设置资源名
        getQuestionVOByIdSlowCallRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());  // 设置根据满调用比例降级
        getQuestionVOByIdSlowCallRule.setSlowRatioThreshold(3); // 设置最大 RT
        getQuestionVOByIdSlowCallRule.setCount(0.2); // 设置阈值
        getQuestionVOByIdSlowCallRule.setMinRequestAmount(10);  // 设置最小请求数
        getQuestionVOByIdSlowCallRule.setTimeWindow(12);  // 设置熔断时间
        getQuestionVOByIdSlowCallRule.setStatIntervalMs(20 * 1000);   // 设置统计时长
        rules.add(getQuestionVOByIdSlowCallRule);

        // 初始化获取题目分页接口的策略
        DegradeRule listQuestionVOByPageErrorRatioRule = new DegradeRule();
        listQuestionVOByPageErrorRatioRule.setResource("listQuestionVOByPage");  // 设置资源名
        listQuestionVOByPageErrorRatioRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());  // 设置根据异常比例降级
        listQuestionVOByPageErrorRatioRule.setCount(0.1); // 设置阈值
        listQuestionVOByPageErrorRatioRule.setMinRequestAmount(10);  // 设置最小请求数
        listQuestionVOByPageErrorRatioRule.setTimeWindow(5);  // 设置熔断时间
        listQuestionVOByPageErrorRatioRule.setStatIntervalMs(10 * 1000);   // 设置统计时长
        rules.add(listQuestionVOByPageErrorRatioRule);

        DegradeRule listQuestionVOByPageSlowCallRule =  new DegradeRule();
        listQuestionVOByPageSlowCallRule.setResource("listQuestionVOByPage");  // 设置资源名
        listQuestionVOByPageSlowCallRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());  // 设置根据满调用比例降级
        listQuestionVOByPageSlowCallRule.setSlowRatioThreshold(3); // 设置最大 RT
        listQuestionVOByPageSlowCallRule.setCount(0.2); // 设置阈值
        listQuestionVOByPageSlowCallRule.setMinRequestAmount(10);  // 设置最小请求数
        listQuestionVOByPageSlowCallRule.setTimeWindow(12);  // 设置熔断时间
        listQuestionVOByPageSlowCallRule.setStatIntervalMs(20 * 1000);   // 设置统计时长
        rules.add(listQuestionVOByPageSlowCallRule);

        // 初始化获取题库详情接口的策略
        DegradeRule getQuestionBankVOByIdErrorRatioRule = new DegradeRule();
        getQuestionBankVOByIdErrorRatioRule.setResource("getQuestionBankVOById");  // 设置资源名
        getQuestionBankVOByIdErrorRatioRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());  // 设置根据异常比例降级
        getQuestionBankVOByIdErrorRatioRule.setCount(0.1); // 设置阈值
        getQuestionBankVOByIdErrorRatioRule.setMinRequestAmount(10);  // 设置最小请求数
        getQuestionBankVOByIdErrorRatioRule.setTimeWindow(5);  // 设置熔断时间
        getQuestionBankVOByIdErrorRatioRule.setStatIntervalMs(10 * 1000);   // 设置统计时长
        rules.add(getQuestionBankVOByIdErrorRatioRule);

        DegradeRule getQuestionBankVOByIdSlowCallRule =  new DegradeRule();
        getQuestionBankVOByIdSlowCallRule.setResource("getQuestionBankVOById");  // 设置资源名
        getQuestionBankVOByIdSlowCallRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());  // 设置根据满调用比例降级
        getQuestionBankVOByIdSlowCallRule.setSlowRatioThreshold(3); // 设置最大 RT
        getQuestionBankVOByIdSlowCallRule.setCount(0.2); // 设置阈值
        getQuestionBankVOByIdSlowCallRule.setMinRequestAmount(10);  // 设置最小请求数
        getQuestionBankVOByIdSlowCallRule.setTimeWindow(12);  // 设置熔断时间
        getQuestionBankVOByIdSlowCallRule.setStatIntervalMs(20 * 1000);   // 设置统计时长
        rules.add(getQuestionBankVOByIdSlowCallRule);

        // 初始化获取题库分页接口的策略
        DegradeRule listQuestionBankVOByPageErrorRatioRule = new DegradeRule();
        listQuestionBankVOByPageErrorRatioRule.setResource("listQuestionBankVOByPage");  // 设置资源名
        listQuestionBankVOByPageErrorRatioRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());  // 设置根据异常比例降级
        listQuestionBankVOByPageErrorRatioRule.setCount(0.1); // 设置阈值
        listQuestionBankVOByPageErrorRatioRule.setMinRequestAmount(10);  // 设置最小请求数
        listQuestionBankVOByPageErrorRatioRule.setTimeWindow(5);  // 设置熔断时间
        listQuestionBankVOByPageErrorRatioRule.setStatIntervalMs(10 * 1000);   // 设置统计时长
        rules.add(listQuestionBankVOByPageErrorRatioRule);

        DegradeRule listQuestionBankVOByPageSlowCallRule =  new DegradeRule();
        listQuestionBankVOByPageSlowCallRule.setResource("listQuestionBankVOByPage");  // 设置资源名
        listQuestionBankVOByPageSlowCallRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());  // 设置根据满调用比例降级
        listQuestionBankVOByPageSlowCallRule.setSlowRatioThreshold(3); // 设置最大 RT
        listQuestionBankVOByPageSlowCallRule.setCount(0.2); // 设置阈值
        listQuestionBankVOByPageSlowCallRule.setMinRequestAmount(10);  // 设置最小请求数
        listQuestionBankVOByPageSlowCallRule.setTimeWindow(12);  // 设置熔断时间
        listQuestionBankVOByPageSlowCallRule.setStatIntervalMs(20 * 1000);   // 设置统计时长
        rules.add(listQuestionBankVOByPageSlowCallRule);

        DegradeRuleManager.loadRules(rules);
    }

}
