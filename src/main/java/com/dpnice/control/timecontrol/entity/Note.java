package com.dpnice.control.timecontrol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * @author DPnice
 * @date 2020-06-11 下午 5:21
 */
@Data
@Builder
@Accessors(chain = true)
public class Note {
    @TableId(value = "uuid")
    private String uuid;
    private String wxOpenId;
    private String content;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date date;
    private int completeCount;
    private int undoneCount;
    private int totalCount;
    private Date updateTime;

    @Tolerate
    public Note(){
    }

}
