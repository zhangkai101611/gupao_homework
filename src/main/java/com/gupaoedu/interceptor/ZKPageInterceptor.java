package com.gupaoedu.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @program: gupaoedu-vip-mybatis-standalone
 * @description:
 * @author: zk
 * @create: 2019-05-01 14:26
 **/
@Intercepts({@Signature(type = Executor.class,method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Slf4j
public class ZKPageInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("mybatis 物理分页");
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];

        //1：获取到rowBound的两个参数
        RowBounds rb = (RowBounds) args[2];
        if (rb == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        //2：获取到执行的sql语句
        BoundSql boundSql = ms.getBoundSql(args[1]);
        String sql=boundSql.getSql();

        //3：拼接sql语句
        String limit = String.format("LIMIT %d,%d", rb.getOffset(), rb.getLimit());
        sql = sql + " " + limit;

        //4：自定义sqlSource:负责根据用户传递的parameterObject，动态地生成SQL语句，将信息封装到BoundSql对象中，并返回
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql, boundSql.getParameterMappings());

        //5：修改原来的sqlSource
        Field field = MappedStatement.class.getDeclaredField("sqlSource");
        field.setAccessible(true);
        field.set(ms, sqlSource);

        //6：执行被拦截方法
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
