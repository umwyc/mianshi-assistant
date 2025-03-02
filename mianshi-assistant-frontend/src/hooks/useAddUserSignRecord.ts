import {addUserSignInUsingPost} from "@/api/userController";
import {message} from "antd";
import {useEffect, useState} from "react";

/**
 * 添加用户签到记录钩子
 */
const useAddUserSignRecord = () => {

    // 签到日期列表（[1, 200] 表示第 1 天和第 200 天有签到记录）
    const [loading, setLoading] = useState<boolean>(true);

    // 请求后端执行签到
    const doFetch = async () => {
        setLoading(true);
        try {
            await addUserSignInUsingPost({});
        } catch (e) {
            message.error("签到失败, " + e.message);
        }
        setLoading(false);
    }

    // 保证只会调用一次
    useEffect(() => {
        doFetch()
    }, []);

    return {loading};
}

export default useAddUserSignRecord;