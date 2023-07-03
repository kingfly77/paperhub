package com.fly.paperhub.paper.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.paperhub.paper.entity.PaperEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaperDao extends BaseMapper<PaperEntity> {

}
