package com.dpnice.control.timecontrol.configuration.database;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@EnableTransactionManagement
@Configuration
@MapperScan(basePackages = MasterDataSourceConfig.PACKAGE, sqlSessionFactoryRef = MasterDataSourceConfig.SQL_SESSION_FACTORY_BEAN_NAME)
public class MasterDataSourceConfig implements TransactionManagementConfigurer {

    static final String SQL_SESSION_FACTORY_BEAN_NAME = "masterSqlSessionFactory";
    //多数据源时需要指定事务管理器
    private static final String TRANSACTION_MANAGER_BEAN_NAME = "masterTransactionManager";
    /**
     * mapper扫描路径
     */
    static final String PACKAGE = "com.dpnice.control.timecontrol.dao.wll";
    /**
     * 数据源配置项前缀
     */
    private static final String DB_PREFIX = "spring.datasource";
    /**
     * 常量
     */
    private static final String MAPPER_LOCATION = "classpath:com/dpnice/control/timecontrol/dao/wll/*.xml";

    private static final String DATA_SOURCE_BEAN_NAME = "masterDataSource";

    @Bean(name = DATA_SOURCE_BEAN_NAME)
    @Qualifier(DATA_SOURCE_BEAN_NAME)
    @ConfigurationProperties(prefix = DB_PREFIX)
    @Primary
    public DataSource firstDataSource() {
        log.info("主数据库连接池创建中......");
        return DataSourceBuilder.create().build();
    }

    @Bean(name = SQL_SESSION_FACTORY_BEAN_NAME)
    public SqlSessionFactory clusterSqlSessionFactory(@Qualifier(DATA_SOURCE_BEAN_NAME) DataSource dataSource) throws Exception {
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        Interceptor[] plugins = new Interceptor[]{
                paginationInterceptor()
        };
        sessionFactory.setPlugins(plugins);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = Lists.newArrayList();
        resources.addAll(Arrays.asList(resolver.getResources(MAPPER_LOCATION)));

        sessionFactory.setMapperLocations(resources.toArray(new Resource[resources.size()]));
        return sessionFactory.getObject();
    }

    @Bean
    public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier(SQL_SESSION_FACTORY_BEAN_NAME) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = TRANSACTION_MANAGER_BEAN_NAME)
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(firstDataSource());
    }

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

}
