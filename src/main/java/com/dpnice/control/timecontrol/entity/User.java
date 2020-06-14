package com.dpnice.control.timecontrol.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @author DPnice
 * @date 2020-06-09 下午 10:40
 */
@Data
@Builder
public class User {

    @TableId(value = "wx_open_id")
    private String wxOpenId;

    private String nickName;

    private String avatarUrl;

    /**
     * 0 开启
     * 1 关闭
     */
    private String look;

    private String groupUuid;

    /**
     * 树的积分
     */
    private float groupIntegral;

    @Tolerate
    public User() {
    }

}
