package io.mds.hty.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;

@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
       ApplicationContext context =  SpringApplication.run(TaskManagerApplication.class, args);


    }






}
