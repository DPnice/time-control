package com.dpnice.control.timecontrol.controller;

import com.dpnice.control.timecontrol.dao.wll.LoginMapper;
import com.dpnice.control.timecontrol.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

/**
 * @author DPnice
 * @date 2020-06-09 下午 10:46
 */
@RestController
@Slf4j
@RequestMapping("login")
public class LoginController {

    @Resource
    private LoginMapper loginMapper;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Value("${appid}")
    private String appid;

    @Value("${secret}")
    private String secret;

    @PostMapping("wx")
    @ResponseBody
    public User login(@RequestBody String json) throws JsonProcessingException {

        JsonNode jsonBody = objectMapper.readValue(json, JsonNode.class);
        JsonNode codeNode = jsonBody.get("code");
        String code = codeNode.asText();

        String urlTemplate = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret" +
                "={1}&js_code={2}&grant_type=authorization_code";
        String url = MessageFormat.format(urlTemplate, appid, secret, code);
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        //{"session_key":"Fhw6mD0BqlAFH7YKaemlwQ==","openid":"oQman5DbKMLk_GRyYcfcolEdIN40"}
        JsonNode jsonNode = objectMapper.readValue(Objects.requireNonNull(forEntity.getBody()),
                JsonNode.class);
        JsonNode brandNode = jsonNode.get("openid");
        String openid = brandNode.asText();
        log.info("openid :{}", openid);

        User user = loginMapper.selectById(openid);
        if (user == null) {
            String avatarUrl = jsonBody.get("avatarUrl").asText();
            String nickName = jsonBody.get("nickName").asText();
            User build = User.builder()
                    .avatarUrl(avatarUrl)
                    .nickName(nickName)
                    .wxOpenId(openid)
                    .groupIntegral(0f)
                    .look("0")
                    .build();
            loginMapper.insert(build);
            return build;
        }
        return user;
    }


    @PostMapping("updateUser")
    @ResponseBody
    public int updateUser(@RequestBody String json) throws JsonProcessingException {
        User user = objectMapper.readValue(json, User.class);
        user.setGroupIntegral(loginMapper.selectById(user.getWxOpenId()).getGroupIntegral());
        return loginMapper.updateById(user);
    }


    @GetMapping("plus")
    @ResponseBody
    public int plus(@RequestParam("wxOpenId") String wxOpenId) {
        User user = loginMapper.selectById(wxOpenId);
        user.setGroupIntegral(user.getGroupIntegral() + 0.1f);
        return loginMapper.updateById(user);
    }

    @GetMapping("reduce")
    @ResponseBody
    public int reduce(@RequestParam("wxOpenId") String wxOpenId) {
        User user = loginMapper.selectById(wxOpenId);
        user.setGroupIntegral(user.getGroupIntegral() - 0.1f);
        return loginMapper.updateById(user);
    }




}
