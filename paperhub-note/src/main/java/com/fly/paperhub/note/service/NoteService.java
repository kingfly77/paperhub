package com.fly.paperhub.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.paperhub.note.entity.NoteEntity;
import com.fly.paperhub.note.vo.NoteVO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface NoteService extends IService<NoteEntity> {

    NoteEntity getNoteById(Long pid, Long uid);

    Boolean saveOrUpdateById(NoteEntity note);

    List<NoteVO> getNoteListByPid(Long pid);

}
