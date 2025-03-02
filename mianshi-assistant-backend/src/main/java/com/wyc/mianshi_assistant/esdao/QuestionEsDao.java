package com.wyc.mianshi_assistant.esdao;

import com.wyc.mianshi_assistant.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

}
