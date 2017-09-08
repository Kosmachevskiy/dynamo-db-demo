package com.example.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DynamoDBTable(tableName = "ProductInfo")
@Getter
@Setter
@NoArgsConstructor
public class ProductInfo {
	@DynamoDBHashKey
	@DynamoDBAutoGeneratedKey
	private String id;

	@DynamoDBAttribute
	private String msrp;

	@DynamoDBAttribute
	private String cost;

	public ProductInfo(String msrp, String cost) {
		this.msrp = msrp;
		this.cost = cost;
	}
}