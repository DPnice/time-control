package com.dpnice.control.timecontrol.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dpnice.control.timecontrol.dao.wll.GroupMapper;
import com.dpnice.control.timecontrol.dao.wll.LoginMapper;
import com.dpnice.control.timecontrol.entity.TreeGroup;

import com.dpnice.control.timecontrol.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DPnice
 * @date 2020-06-09 下午 10:46
 */
@RestController
@Slf4j
@RequestMapping("group")
public class GroupController {

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private LoginMapper loginMapper;


    @GetMapping("save")
    @ResponseBody
    public String save(@RequestParam("wxOpenId") String wxOpenId,
                       @RequestParam("groupName") String groupName) {
        String uuidNew = UUID.randomUUID().toString();
        int insert = groupMapper.insert(TreeGroup.builder()
                .uuid(uuidNew)
                .createrWxOpenId(wxOpenId)
                .slogan(groupName).build());

        int i = loginMapper.updateById(
                User.builder()
                        .wxOpenId(wxOpenId)
                        .groupUuid(uuidNew).build());
        return (insert == 1 && i == 1) ? uuidNew : "";
    }

    @GetMapping("join")
    @ResponseBody
    public String join(@RequestParam("wxOpenId") String wxOpenId,
                       @RequestParam("groupUuid") String groupUuid) {

        TreeGroup treeGroup = groupMapper.selectById(groupUuid);

        if (treeGroup != null) {
            int i = loginMapper.updateById(
                    User.builder()
                            .wxOpenId(wxOpenId)
                            .groupUuid(treeGroup.getUuid()).build());
            return i == 1 ? groupUuid : "";
        } else {
            return "";
        }

    }

    @GetMapping("list")
    @ResponseBody
    public Map<String, Object> list(@RequestParam("groupUuid") String groupUuid) {

        List<User> userList = loginMapper.selectList(new QueryWrapper<User>().lambda()
                .eq(User::getGroupUuid, groupUuid)
        );

        TreeGroup treeGroup = groupMapper.selectById(groupUuid);
        List<User> collect = userList.stream()
                .sorted(Comparator.comparing(User::getGroupIntegral).reversed())
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>(2);
        map.put("swipeList", collect);
        map.put("treeGroup", treeGroup);
        return map;

    }


}
