package com.fly.paperhub.paper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.paperhub.paper.entity.PaperEntity;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;

public interface PaperService extends IService<PaperEntity> {

    Pair<Boolean, String> uploadPaper(PaperEntity paper);

}
