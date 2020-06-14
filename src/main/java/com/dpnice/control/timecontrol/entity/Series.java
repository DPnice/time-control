package com.dpnice.control.timecontrol.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author DPnice
 * @date 2020-06-14 下午 12:37
 */
@Data
@Builder
public class Series {
    private String name;
    private Object data;
    private String color;

}
