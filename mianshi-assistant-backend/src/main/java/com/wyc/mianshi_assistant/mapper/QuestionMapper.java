package com.wyc.mianshi_assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wyc.mianshi_assistant.model.entity.Question;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @description 针对表【question(题目)】的数据库操作Mapper
*/
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询最近发生过更新的题目列表（包括已删除的）
     *
     * @param minUpdateTime
     * @return
     */
    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date minUpdateTime);
}




