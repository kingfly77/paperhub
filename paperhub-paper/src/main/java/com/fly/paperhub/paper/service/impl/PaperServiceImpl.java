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
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import sun.awt.Mutex;

import javax.naming.ldap.HasControls;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service("PaperService")
public class PaperServiceImpl extends ServiceImpl<PaperDao, PaperEntity> implements PaperService {

    private static final String INDEX_NAME = "paper_index";

    @Autowired
    private OssFeignService ossFeignService;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private RestHighLevelClient esClient;

    @Override
    public Pair<Boolean, String> uploadPaper(PaperEntity paper) {
        log.debug("paper service: upload paper");

        // notify oss service
        Map<String, Object> params = new HashMap<>();
        params.put("url", paper.getUrl());
        ossFeignService.uploadPaperToOss(params);

        // save in mysql
        int insertRet = paperDao.insert(paper);
        if (insertRet != 1) return new Pair<>(false, "存入 mysql 失败");

        // save in es
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
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

    @Override
    public List<PaperEntity> getPaperListFromES(List<String> items, List<String> conditions, List<String> contents, int page, int pageSize) {
        SearchRequest request = new SearchRequest(INDEX_NAME);
        List<String> highlightKeys = new ArrayList<>();
        // query
        for (int i = 0; i < items.size(); ++i) {
            String item = items.get(i);
            String condition = conditions.get(i);
            String content = contents.get(i);
            if ("all".equals(condition)) {
                request.source().query(QueryBuilders.matchAllQuery());
            } else if ("like".equals(condition)) {
                if (content == null || "".equals(content)) {
                    request.source().query(QueryBuilders.matchAllQuery());
                } else {
                    request.source().query(QueryBuilders.matchQuery(item, content));
                }
            } else if ("equals".equals(condition)) {
                request.source().query(QueryBuilders.termQuery(item, content));
            }
            if ("all".equals(item)) {
                highlightKeys.add("title");
                highlightKeys.add("authors");
                highlightKeys.add("description");
                highlightKeys.add("year");
                highlightKeys.add("publishedIn");
            } else highlightKeys.add(item);
        }
        // TODO: sort
//        request.source().sort(items.get(0), SortOrder.ASC);
        // page
        request.source().from((page - 1) * pageSize).size(pageSize);

        // TODO: highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String k : highlightKeys) {
            HighlightBuilder.Field field = new HighlightBuilder.Field(k)
                    .preTags("<span class=\"highlight\">").postTags("</span>");
            highlightBuilder.field(field);
        }
        highlightBuilder.requireFieldMatch(false);
        request.source().highlighter(highlightBuilder);
        // get response
        List<PaperEntity> paperList = new ArrayList<>();
        try {
            SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.getHits();
            for (SearchHit hit: hits) {
                String json = hit.getSourceAsString();
                log.debug("hit: " + json);
                PaperEntity paper = JSON.parseObject(json, PaperEntity.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (!CollectionUtils.isEmpty(highlightFields)) {
                    for (String k: highlightKeys) {
                        HighlightField highlightField = highlightFields.get(k);
                        if (highlightField != null) {
                            String highlightValue = highlightField.getFragments()[0].toString();
                            log.debug("high light: " + highlightValue);
                            String methodName = "set" + k.substring(0, 1).toUpperCase() + k.substring(1);
                            paper.getClass().getMethod(methodName, String.class).invoke(paper, highlightValue);
                        }
                    }
                }
                paperList.add(paper);
            }
        } catch (Exception e) {
            return paperList;
        }
        return paperList;
    }
}
