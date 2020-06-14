package com.dpnice.control.timecontrol.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author DPnice
 * @date 2020-06-14 下午 12:43
 */
@Data
@Builder
public class Area {
    private List<Integer> categories;
    private List<Series> series;

}
