package com.fly.paperhub.paper.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@TableName("paper")
public class PaperEntity {

    @TableId
    private Long pid;

    @NotNull
    private String title;

    private String authors;

    private String year;

    private String publishedIn;

    @NotNull
    private Long uploadUid;

    @NotNull
    private String uploadUname;

    private String uploadDate;

    private String description;

    @URL
    @NotNull
    private String url;

    @TableLogic(value = "1", delval = "0")
    private Integer exist;

    @Override
    public String toString() {
        return "PaperEntity{" +
                "pid=" + pid +
                ", title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", year='" + year + '\'' +
                ", publishedIn='" + publishedIn + '\'' +
                ", uploadUid=" + uploadUid +
                ", uploadUname='" + uploadUname + '\'' +
                ", uploadDate='" + uploadDate + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", exist=" + exist +
                '}';
    }
}
