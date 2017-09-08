package com.example.dynamodb.config;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@Profile("embedded-dynamo-db")
public class EmbeddedDynamoDBConfig {

	private DynamoDBProxyServer server;

	@PostConstruct
	@SneakyThrows
	private void init(){
		System.setProperty("sqlite4java.library.path", "native-libs");
		server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", "8000"});
		server.start();
	}

	@PreDestroy
	@SneakyThrows
	private void destroy(){
		server.stop();
	}
}
