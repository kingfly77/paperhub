package com.fly.paperhub.note.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.paperhub.note.entity.NoteEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteDao extends BaseMapper<NoteEntity> {

    int saveOrUpdateById(NoteEntity note);

}
