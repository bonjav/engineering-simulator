package com.datacenterflow.simulation.adapter.out.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fastapi")
public record FastApiProperties(String url) {}
