package com.wyc.mianshi_assistant.satoken;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.wyc.mianshi_assistant.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.wyc.mianshi_assistant.constant.UserConstant.USER_LOGIN_STATE;

@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号的权限吗列表
     *
     * @param o
     * @param s
     * @return
     */
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return new ArrayList<>();
    }

    /**
     * 返回一个账号的角色标识列表
     *
     * @param loginId
     * @param s
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String s) {
        // 从当前登录用户中获取信息
        User user = (User)StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        return Collections.singletonList(user.getUserRole());
    }
}
