package com.dpnice.control.timecontrol.configuration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    private static Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            //开始设置连接池
            PoolingHttpClientConnectionManager poolingHttpClientConnectionManager
                    = new PoolingHttpClientConnectionManager();
            //最大连接数
            poolingHttpClientConnectionManager.setMaxTotal(100);
            //同路由并发数
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(20);
            httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);

            HttpClient httpClient = httpClientBuilder.build();
            // httpClient连接配置
            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                    = new HttpComponentsClientHttpRequestFactory(httpClient);
            //连接超时
            clientHttpRequestFactory.setConnectTimeout(30 * 1000);
            //数据读取超时时间
            clientHttpRequestFactory.setReadTimeout(60 * 1000);
            //连接不够用的等待时间
            clientHttpRequestFactory.setConnectionRequestTimeout(30 * 1000);
            return clientHttpRequestFactory;
        } catch (Exception e) {
            logger.error("初始化clientHttpRequestFactory出错", e);
        }
        return null;
    }
}
