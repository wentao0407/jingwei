package com.jingwei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 经纬服装生产管理系统主启动类
 * <p>
 * 覆盖从客户下单到生产制造再到仓储物流的全链路业务。
 * </p>
 *
 * @author JingWei
 */
@SpringBootApplication
@MapperScan("com.jingwei.**.infrastructure.persistence")
public class JingWeiApplication {

    /**
     * 应用入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(JingWeiApplication.class, args);
    }
}
