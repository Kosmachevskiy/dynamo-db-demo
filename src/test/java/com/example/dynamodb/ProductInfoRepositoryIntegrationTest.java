package com.example.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.example.dynamodb.model.ProductInfo;
import com.example.dynamodb.persistence.ProductInfoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("embedded-dynamo-db")
@TestPropertySource(properties = {
		"amazon.dynamodb.endpoint=http://localhost:8000/",
		"amazon.aws.accesskey=test1",
		"amazon.aws.secretkey=test231" })
public class ProductInfoRepositoryIntegrationTest {

	private DynamoDBMapper dynamoDBMapper;

	@Autowired
	private AmazonDynamoDB amazonDynamoDB;

	@Autowired
	private ProductInfoRepository repository;

	private static final String EXPECTED_COST = "20";
	private static final String EXPECTED_PRICE = "50";

	@Before
	public void setup() throws Exception {
		dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

		CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(ProductInfo.class);
		tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

		try {
			amazonDynamoDB.createTable(tableRequest);
		} catch (Exception e) {
			// Table already exists //
		}

		dynamoDBMapper.batchDelete(repository.findAll());
	}

	@Test
	public void sampleTestCase() {
		ProductInfo dave = new ProductInfo(EXPECTED_PRICE, EXPECTED_COST);
		repository.save(dave);

		List<ProductInfo> result = (List<ProductInfo>) repository.findAll();

		assertTrue("Not empty", result.size() > 0);
		assertTrue("Contains item with expected cost", result.get(0).getCost().equals(EXPECTED_COST));
	}
}