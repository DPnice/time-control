package com.dpnice.control.timecontrol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

/**
 * @author DPnice
 * @date 2020-06-13 下午 7:51
 */
@Data
@Builder
@Accessors(chain = true)
public class TreeGroup {

    @TableId(value = "uuid")
    private String uuid;

    private String createrWxOpenId;

    private String slogan;

    @Tolerate
    public TreeGroup() {
    }
}
