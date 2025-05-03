// src/test/java/com/example/eventmanagementsystem/config/RepositoryTestConfig.java
package com.example.eventmanagementsystem.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackages = "com.example.eventmanagementsystem.repository")
@EntityScan(basePackages = "com.example.eventmanagementsystem.model")
public class RepositoryTestConfig {
}