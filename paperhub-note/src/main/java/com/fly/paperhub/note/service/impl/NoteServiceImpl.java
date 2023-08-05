package com.fly.paperhub.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fly.paperhub.common.constants.RedisKeys;
import com.fly.paperhub.common.utils.StringUtil;
import com.fly.paperhub.note.annotation.Cache;
import com.fly.paperhub.note.dao.NoteDao;
import com.fly.paperhub.note.entity.NoteEntity;
import com.fly.paperhub.note.feign.UserFeignService;
import com.fly.paperhub.note.service.NoteService;
import com.fly.paperhub.note.vo.NoteVO;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("NoteService")
public class NoteServiceImpl extends ServiceImpl<NoteDao, NoteEntity> implements NoteService {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private UserFeignService userFeignService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Cache(option="query", prefix=RedisKeys.NOTE_PREFIX, key="${pid}:${uid}", keyType="Object:Hash", expire=10, timeUnit=TimeUnit.MINUTES)
    public NoteEntity getNoteById(Long pid, Long uid) {

        // 在mysql中查询
        QueryWrapper<NoteEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid).eq("uid", uid);
        NoteEntity note = noteDao.selectOne(wrapper);

        return note;
    }

    @Override
    @Cache(option="update", prefix=RedisKeys.NOTE_PREFIX, key="${note.pid}:${note.uid}")
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
