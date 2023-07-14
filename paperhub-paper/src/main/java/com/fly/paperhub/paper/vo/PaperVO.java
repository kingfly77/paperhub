package com.fly.paperhub.paper.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
public class PaperVO {

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

    @NotNull
    private String uploadDate;

    private String description;

    @URL
    @NotNull
    private String url;

}
