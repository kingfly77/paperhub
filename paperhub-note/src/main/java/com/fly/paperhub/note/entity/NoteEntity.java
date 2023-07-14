package com.fly.paperhub.note.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;

@Data
@TableName("note")
public class NoteEntity {

    @TableId
    private Long pid;

    @TableId
    private Long uid;

    private String text;

    private String html;

    @TableLogic(value = "1", delval = "0")
    private Integer exist;

    private transient int count;

}
