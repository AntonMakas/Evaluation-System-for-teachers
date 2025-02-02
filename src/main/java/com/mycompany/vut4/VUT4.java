/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vut4;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
/**
 *
 * @author megab
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.mycompany.vut4.repository")
public class VUT4 {
    public static void main(String[] args) {
        new SpringApplicationBuilder(VUT4.class)
            .properties(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/fakevut",
                "spring.datasource.username=postgres",
                "spring.datasource.password=123456",
                "spring.jpa.hibernate.ddl-auto=update",
                "spring.jpa.show-sql=true",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
                "server.port=8081"
            )
            .run(args);
        System.out.println("Welcome to fakeVUT");
    }
}
