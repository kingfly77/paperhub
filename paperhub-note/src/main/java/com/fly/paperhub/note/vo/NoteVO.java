package com.fly.paperhub.note.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
public class NoteVO {

    @TableId
    private Long pid;

    @TableId
    private Long uid;

    private String uname;

    private String text;

    private String html;

    @TableLogic(value = "1", delval = "0")
    private Integer exist;

}
