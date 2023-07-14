package com.fly.paperhub.paper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.paperhub.paper.entity.PaperEntity;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;

import java.util.List;

public interface PaperService extends IService<PaperEntity> {

    Pair<Boolean, String> uploadPaper(PaperEntity paper);

    List<PaperEntity> getPaperListFromES(List<String> items, List<String> conditions, List<String> contents, int page, int pageSize);
}
