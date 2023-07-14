package com.fly.paperhub.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.paperhub.note.dao.NoteDao;
import com.fly.paperhub.note.entity.NoteEntity;
import com.fly.paperhub.note.feign.UserFeignService;
import com.fly.paperhub.note.service.NoteService;
import com.fly.paperhub.note.vo.NoteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("NoteService")
public class NoteServiceImpl extends ServiceImpl<NoteDao, NoteEntity> implements NoteService {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserFeignService userFeignService;

    @Override
    public NoteEntity getNoteById(Long pid, Long uid) {
        QueryWrapper<NoteEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid).eq("uid", uid);
        NoteEntity note = noteDao.selectOne(wrapper);
        return note;
    }

    @Override
    public Boolean saveOrUpdateById(NoteEntity note) {
        if (note == null || note.getPid() == null || note.getUid() == null) return false;
        return noteDao.saveOrUpdateById(note) == 1;
    }

    @Override
    public List<NoteVO> getNoteListByPid(Long pid) {
        QueryWrapper<NoteEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid);
        List<NoteEntity> noteList = noteDao.selectList(wrapper);
        List<NoteVO> noteVOList = noteList.stream().map(note -> {
            NoteVO noteVO = new NoteVO();
            BeanUtils.copyProperties(note, noteVO);
            return noteVO;
        }).collect(Collectors.toList());
        List<Long> idList = noteList.stream().map(NoteEntity::getUid).collect(Collectors.toList());
        Map<String, Object> params = new HashMap<>();
        params.put("idList", idList);
        // get username by user id
        Map<String, Object> data = userFeignService.getUserIdNameMapByIds(params);
        Map<String, String> idNameMap = (Map<String, String>) data.get("idNameMap");
        idNameMap.forEach((k, v) -> {
            log.debug("idNameMap: " + k + " -> " + v);
        });
        noteVOList.forEach(noteVO -> {
            noteVO.setUname(idNameMap.get(String.valueOf(noteVO.getUid())));
        });
        return noteVOList;
    }


}
