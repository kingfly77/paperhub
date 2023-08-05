package com.fly.paperhub.note.controller;

import com.alibaba.fastjson.JSON;
import com.fly.paperhub.common.constants.RedisKeys;
import com.fly.paperhub.common.utils.StringUtil;
import com.fly.paperhub.note.entity.NoteEntity;
import com.fly.paperhub.note.service.NoteService;
import com.fly.paperhub.note.vo.NoteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/note/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @RequestMapping("/getNote")
    public Map<String, Object> getNote(@RequestParam("pid") Long pid, @RequestParam("uid") Long uid) {

        NoteEntity note = noteService.getNoteById(pid, uid);

        if (note == null) {
            System.out.println("note is null!");
        }
        Map<String, Object> data = new HashMap<>();
        if (note != null) data.put("text", note.getText());
        else data.put("text", "");
        return data;
    }

    @PostMapping("/saveOrUpdate")
    public Map<String, Object> saveOrUpdate(@RequestBody Map<String, Object> params) {
        NoteEntity note = new NoteEntity();
        note.setPid(Long.valueOf((String) params.get("pid")));
        note.setUid(Long.valueOf((String) params.get("uid")));
        note.setText((String) params.get("text"));
        note.setHtml((String) params.get("html"));
        boolean flag = noteService.saveOrUpdateById(note);
        Map<String, Object> data = new HashMap<>();
        data.put("success", flag);
        if (!flag) {
            // TODO
        }
        return data;
    }

    @GetMapping("/getNoteListByPid/{pid}")
    public Map<String, Object> getNoteListByPid(@PathVariable("pid") Long pid) {
        log.debug(String.valueOf(pid));
        List<NoteVO> noteList = noteService.getNoteListByPid(pid);
        Map<String, Object> data = new HashMap<>();
        // TODO: false status
        data.put("success", true);
        data.put("note_list", noteList);
        return data;
    }

}
