package com.fly.paperhub.paper.controller;

import com.fly.paperhub.paper.entity.PaperEntity;
import com.fly.paperhub.paper.feign.OssFeignService;
import com.fly.paperhub.paper.service.PaperService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/paper/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @RequestMapping("/upload")
    public Map<String, Object> upload(@RequestBody PaperEntity paper) {
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

    @RequestMapping("/list_all")
    public Map<String, Object> getAllPaperList() {
        List<PaperEntity> paperList = paperService.list();
        Map<String, Object> data = new HashMap<>();
        data.put("list", paperList);
        return data;
    }
}
