package br.com.lojacustos.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MarketplaceDefaultsProperties.class)
public class AppConfig {}
