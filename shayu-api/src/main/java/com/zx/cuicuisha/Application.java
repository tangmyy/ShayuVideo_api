package com.zx.cuicuisha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;




@SpringBootApplication
@MapperScan(basePackages = "com.zx.cuicuisha.mapper")
@ComponentScan(basePackages = {"com.zx.cuicuisha", "org.n3r.idworker"})
@EnableMongoRepositories
public class Application {
   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }

}
