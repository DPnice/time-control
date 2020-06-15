package com.dpnice.control.timecontrol.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dpnice.control.timecontrol.dao.wll.NoteMapper;
import com.dpnice.control.timecontrol.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author DPnice
 * @date 2020-06-11 下午 4:54
 */
@RestController
@Slf4j
@RequestMapping("note")
public class NoteController {

    @Resource
    private NoteMapper mapper;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("save")
    @ResponseBody
    public String save(@RequestBody String json) throws JsonProcessingException, ParseException {

        JsonNode jsonBody = objectMapper.readTree(json);
        String uuid = jsonBody.get("uuid").asText();
        String wxOpenId = jsonBody.get("wxOpenId").asText();

        JsonNode contentList = jsonBody.get("content");
        Iterator<JsonNode> elements = contentList.elements();
        List<JsonNode> jsonNodes = Lists.newArrayList(elements);
        long complete = jsonNodes.stream().filter(n -> "0".equals(n.get("status").asText())).count();

        if (StringUtils.isBlank(uuid)) {
            String date = jsonBody.get("date").asText();
            String uuidNew = UUID.randomUUID().toString();
            mapper.insert(Note.builder()
                    .uuid(uuidNew)
                    .content(objectMapper.writeValueAsString(contentList))
                    .wxOpenId(wxOpenId)
                    .completeCount(0)
                    .undoneCount(contentList.size())
                    .totalCount(contentList.size())
                    .date(df.parse(date))
                    .updateTime(new Date())
                    .build());
            return uuidNew;
        } else {

            Note build = Note.builder()
                    .uuid(uuid)
                    .content(objectMapper.writeValueAsString(contentList))
                    .completeCount((int) complete)
                    .undoneCount(contentList.size() - (int) complete)
                    .totalCount(contentList.size())
                    .updateTime(new Date())
                    .build();
            mapper.updateById(build);
            return uuid;
        }

    }


    @GetMapping("get")
    @ResponseBody
    public Note getNote(@RequestParam("wxOpenId") String wxOpenId,
                        @RequestParam(name = "date") String date
    ) {

        if (StringUtils.isNotBlank(date)) {
            //加日期的检索
            return mapper.selectOne(new QueryWrapper<Note>().lambda()
                    .eq(Note::getDate, date)
                    .eq(Note::getWxOpenId, wxOpenId)
            );
        } else {

            //今日待办
            return mapper.selectOne(new QueryWrapper<Note>().lambda()
                    .eq(Note::getWxOpenId, wxOpenId)
                    .eq(Note::getDate, df.format(new Date()))
            );
        }
    }


    @GetMapping("statistics")
    @ResponseBody
    public Map<String, Object> statistics(@RequestParam("wxOpenId") String wxOpenId,
                                          @RequestParam(name = "year") int year,
                                          @RequestParam(name = "month") int month,
                                          @RequestParam(name = "day") int day
    ) {

        Map<String, Object> map = new HashMap<>(3);
        //总天数
        Integer integer = mapper.selectCount(new QueryWrapper<Note>()
                .lambda()
                .eq(Note::getWxOpenId, wxOpenId));
        map.put("totalDay", integer);

        //区域图
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, year);
        instance.set(Calendar.MONTH, month - 1);
        instance.set(Calendar.DATE, 1);
        String left = df.format(instance.getTime());
        instance.add(Calendar.MONTH, 1);
        String right = df.format(instance.getTime());

        List<Note> notes = mapper.selectList(new QueryWrapper<Note>().lambda()
                .eq(Note::getWxOpenId, wxOpenId)
                .ge(Note::getDate, left)
                .lt(Note::getDate, right)
                .orderByAsc(Note::getDate)
        );

        List<Integer> categories = new ArrayList<>();
        List<Integer> tList = new ArrayList<>();
        List<Integer> cList = new ArrayList<>();
        List<Integer> nList = new ArrayList<>();

        Series t = Series.builder().data(tList).name("待办总数").color("#1890ff").build();
        Series c = Series.builder().data(cList).name("完成").color("#2fc25b").build();
        Series n = Series.builder().data(nList).name("未完成").color("#FFD700").build();

        Calendar instance1 = Calendar.getInstance();

        if (notes.size() > 0) {
            notes.forEach(note -> {
                tList.add(note.getTotalCount());
                cList.add(note.getCompleteCount());
                nList.add(note.getUndoneCount());

                instance1.setTime(note.getDate());
                categories.add(instance1.get(Calendar.DATE));

            });
        } else {
            categories.add(1);
            categories.add(3);
            categories.add(5);
        }


        List<Series> areaList = new ArrayList<>();
        areaList.add(t);
        areaList.add(c);
        areaList.add(n);

        Area area = Area.builder().series(areaList).categories(categories).build();
        map.put("area", area);

        //完成率
        instance.set(Calendar.MONTH, month - 1);
        instance.set(Calendar.DATE, day);
        String date = df.format(instance.getTime());

        Note note = mapper.selectOne(new QueryWrapper<Note>().lambda()
                .eq(Note::getWxOpenId, wxOpenId)
                .eq(Note::getDate, date)
        );

        float i = 0;
        if (note != null) {
            i = (float) note.getCompleteCount() / note.getTotalCount();
        }

        Series arcbar1Series = Series.builder().data(i).color("#2fc25b").build();

        List<Series> arcbar1List = new ArrayList<>();
        arcbar1List.add(arcbar1Series);
        Arcbar1 arcbar1 = Arcbar1.builder().series(arcbar1List).build();
        map.put("arcbar1", arcbar1);

        return map;
    }


}
