package com.fly.paperhub.paper.controller;

import com.fly.paperhub.paper.entity.PaperEntity;
import com.fly.paperhub.paper.service.PaperService;
import com.fly.paperhub.paper.vo.PaperVO;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fly.paperhub.common.utils.ListUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/paper/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    private final DateFormat uploadDateFormat = new SimpleDateFormat("yyyy年MM月dd日");

    @RequestMapping("/upload")
    public Map<String, Object> upload(@RequestBody PaperVO paperVO) {
        PaperEntity paper = new PaperEntity();
        BeanUtils.copyProperties(paperVO, paper);
        if (paper.getUploadDate() == null) {
            paper.setUploadDate(new Date());
        }
        log.debug("upload paper:");
        log.debug(paper.toString());
        Pair<Boolean, String> insertRet = paperService.uploadPaper(paper);
        Map<String, Object> data = new HashMap<>();
        data.put("success", insertRet.getKey());
        if (!insertRet.getKey()) {
            data.put("error_msg", "上传失败，" + insertRet.getValue());
        }
        return data;
    }

    @Deprecated
    @RequestMapping("/list/mysql")
    public Map<String, Object> getPaperListFromMysql() {
        List<PaperEntity> paperList = paperService.list();
        Map<String, Object> data = new HashMap<>();
        data.put("list", paperList);
        return data;
    }

    @RequestMapping("/list")
    public Map<String, Object> getPaperList(@RequestBody Map<String, Object> params) {
        // search items
        List<String> items = (List<String>) params.get("items");
        // query conditions: equals / like / all
        List<String> conditions = (List<String>) params.get("conditions");
        // query contents
        List<String> contents = (List<String>) params.get("contents");
        // page
        int page = (int) params.get("page");
        // page size
        int pageSize = (int) params.get("pageSize");

        log.debug("query:");
        log.debug("items: " + ListUtil.toString(items));
        log.debug("conditions: " + ListUtil.toString(conditions));
        log.debug("contents: " + ListUtil.toString(contents));
        log.debug("page: " + page);
        log.debug("pageSize: " + pageSize);

        assert items.size() == conditions.size() && conditions.size() == contents.size();

        List<PaperEntity> paperList = paperService.getPaperListFromES(items, conditions, contents, page, pageSize);
        List<PaperVO> paperVOList = paperList.stream().map(paper -> {
            PaperVO paperVO = new PaperVO();
            BeanUtils.copyProperties(paper, paperVO);
            paperVO.setUploadDate(uploadDateFormat.format(paper.getUploadDate()));
            return paperVO;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("list", paperVOList);
        data.put("total_num", paperVOList.size());
        return data;
    }
}
