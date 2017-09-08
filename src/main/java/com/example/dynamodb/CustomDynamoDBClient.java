package com.example.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomDynamoDBClient {

	public static void main(String[] args) throws Exception {
		new DynamoDBClient();
	}

	@Slf4j
	public static class DynamoDBClient {
		private static final String TABLE_NAME = "Movies";
		private DynamoDB dynamoDB;
		private Table table;

		public DynamoDBClient() {
			initDB();
			deleteTable();
			createTable();
//			loadData();
			addItem();
			getItem();
			updateItem();
			getItem();
		}

		private void updateItem() {
			UpdateItemSpec spec = new UpdateItemSpec()
					.withPrimaryKey("year", 2015, "title", "Some Title")
					.withUpdateExpression("set info.description = :d, info.actors = :a")
					.withValueMap(new ValueMap()
							.withString(":d", "Some Description")
							.withList(":a", Arrays.asList("A", "B", "C"))
					).withReturnValues(ReturnValue.UPDATED_NEW);
			UpdateItemOutcome outcome = table.updateItem(spec);
			log.debug("=====================\nItem updated: \n{}", outcome.getItem().toJSONPretty());
		}

		private void getItem() {
			GetItemSpec itemSpec = new GetItemSpec().withPrimaryKey("year", 2015, "title", "Some Title");
			Item item = table.getItem(itemSpec);
			log.debug("=====================\nItem:\n", item.toJSONPretty());
		}

		private void addItem() {
			Map<String, String> info = new HashMap<>();
			info.put("key", "value");
			PutItemOutcome outcome = table.putItem(new Item()
					.withPrimaryKey("year", 2015, "title", "Some Title")
					.withMap("info", info));
			log.debug("Item added.");
		}

		private void initDB() {
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
					.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
					.build();
			dynamoDB = new DynamoDB(client);
		}

		private void deleteTable() {
			try {
				dynamoDB.getTable(TABLE_NAME).delete();
			} catch (Exception e) {
				System.out.println("Error during table deleting. " + e.getMessage());
				e.printStackTrace();
			}
		}

		private void createTable() {
			try {
				table = dynamoDB.createTable(
						TABLE_NAME,
						Arrays.asList(
								new KeySchemaElement("year", KeyType.HASH),
								new KeySchemaElement("title", KeyType.RANGE)),
						Arrays.asList(
								new AttributeDefinition("year", ScalarAttributeType.N),
								new AttributeDefinition("title", ScalarAttributeType.S)),
						new ProvisionedThroughput(10L, 10L)
				);
				table.waitForActive();
			} catch (InterruptedException e) {
				log.error("Unable to create table: ", e);
				throw new RuntimeException(e);
			}
		}

		private void loadData() {
			try {
				JsonParser parser = new JsonFactory().createParser(new File("moviedata.json"));
				JsonNode rootNode = new ObjectMapper().readTree(parser);
				Iterator<JsonNode> iter = rootNode.iterator();
				while (iter.hasNext()) {
					ObjectNode currentNode = (ObjectNode) iter.next();
					int year = currentNode.path("year").asInt();
					String title = currentNode.path("title").asText();
					table.putItem(new Item()
							.withPrimaryKey("year", year, "title", title)
							.withJSON("info", currentNode.path("info").toString()));
				}
				parser.close();
			} catch (IOException e) {
				log.error("Unable to load data to table: ", e);
				throw new RuntimeException(e);
			}
		}
	}
}
