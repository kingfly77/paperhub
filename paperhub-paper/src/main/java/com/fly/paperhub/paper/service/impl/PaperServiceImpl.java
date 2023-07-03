package com.fly.paperhub.paper.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.paperhub.paper.config.ElasticSearchConfig;
import com.fly.paperhub.paper.dao.PaperDao;
import com.fly.paperhub.paper.entity.PaperEntity;
import com.fly.paperhub.paper.feign.OssFeignService;
import com.fly.paperhub.paper.service.PaperService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ldap.HasControls;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("PaperService")
public class PaperServiceImpl extends ServiceImpl<PaperDao, PaperEntity> implements PaperService {

    @Autowired
    private OssFeignService ossFeignService;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private RestHighLevelClient esClient;

    @Override
    public Pair<Boolean, String> uploadPaper(PaperEntity paper) {
        log.debug("paper service: upload paper");

//        // notify oss service
//        Map<String, Object> params = new HashMap<>();
//        params.put("url", paper.getUrl());
//        ossFeignService.uploadPaperToOss(params);

        // save in mysql
        int insertRet = paperDao.insert(paper);
        if (insertRet != 1) return new Pair<>(false, "存入 mysql 失败");

        // save in es
        IndexRequest indexRequest = new IndexRequest("paper_index");
        // auto id
        // indexRequest.id("1");
        String jsonStr = JSON.toJSONString(paper);
        indexRequest.source(jsonStr, XContentType.JSON);
        try {
            IndexResponse index = esClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
            log.debug(index.toString());
        } catch (IOException e) {
            log.warn(e.getMessage());
            return new Pair<>(false, e.getMessage());
        }

        return new Pair<>(true, "");
    }
}
