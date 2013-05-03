
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;



public class Main 
{
	static AmazonDynamoDBClient dynamoDB;
	
    private static final String TO = "barcoe4@hotmail.com";
    private static final String FROM = "barcoe4@hotmail.com";
    private static final String BODY = "Hello World!";
    private static final String SUBJECT = "Hello World!";
	
	private static void init() throws Exception 
	{
    	/*
		 * This credentials provider implementation loads your AWS credentials
		 * from a properties file at the root of your classpath.
		 */
        dynamoDB = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        dynamoDB.setRegion(usEast1);
    }
	
    public static void main(String[] args) throws Exception 
    {
    	init();
    	
		String fileName="src/students22.csv";
		CSVFileReader x = new CSVFileReader(fileName);
		x.ReadFile();
		x.displayArrayList();	    

    	AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider().getCredentials();
 		AmazonSimpleEmailService ses = new AmazonSimpleEmailServiceClient(credentials);
 		Region usWest2 = Region.getRegion(Regions.US_EAST_1);
 		ses.setRegion(usWest2);
 		
 		verifyEmailAddress(ses, FROM);
 		
 		
 		/*
		 * Setup JavaMail to use the Amazon Simple Email Service by specifying
		 * the "aws" protocol.
		 */
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "aws");
		
		props.setProperty("mail.aws.user", credentials.getAWSAccessKeyId());
        props.setProperty("mail.aws.password", credentials.getAWSSecretKey());
        
        Session session = Session.getInstance(props);
        
        try {
            // Create a new Message
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
            msg.setSubject(SUBJECT);
            msg.setText(BODY);
            msg.saveChanges();

            // Reuse one Transport object for sending all your messages
            // for better performance
            Transport t = new AWSJavaMailTransport(session, null);
            t.connect();
            t.sendMessage(msg, null);

            // Close your transport when you're completely done sending
            // all your messages
            t.close();
        } catch (AddressException e) {
            e.printStackTrace();
            System.out.println("Caught an AddressException, which means one or more of your "
                    + "addresses are improperly formatted.");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Caught a MessagingException, which means that there was a "
                    + "problem sending your message to Amazon's E-mail Service check the "
                    + "stack trace for more information.");
        }


    	try {
            String tableName = "TestTable3";
            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L));
                TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

            // Wait for it to become active
            waitForTableToBecomeAvailable(tableName);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // Add Contents of students.csv to Database. here
            
            // Add an item
            Map<String, AttributeValue> item = newItem("name1","c76000001", "i.smith", "ian.smith@nodit.ie", "smith", "ian", "J", "DT228", 2, "EL");
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
        

            // Add another item
            item = newItem("name2","c76000002", "j.kennedy", "john.kennedy@nodit.ie", "kennedy", "john", "f", "DT228", 2, "RE");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
         // Add another item
            item = newItem("name3","c76000003", "l.johnson", "lyndon.johnston@nodit.ie", "johnston", "lyndon", "b", "DT228", 2, "RE");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name4","c76000004", "m.thatcher", "margaret.thatcher@nodit.ie", "thatcher", "margaret", " ", "DT211", 2, "EL");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name5","c76000005", "n.machiavelli", "nicolo.machiavelli@nodit.ie", "machiavelli", "nicolo", " ", "DT211", 4, "RE");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name6","c76000006", "s.beckett", "samuel.beckett@nodit.ie", "beckett", "samuel", " ", "DT228", 3, "RE");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name7","c76000007", "j.joyce", "james.joyce@nodit.ie", "joyce", "james", " ", "DT211", 2, "RE");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name8","c76000008", "j.prufrock", "jonathan.prufock@nodit.ie", "prufrock", "jonathan", "a", "DT228", 2, "RE");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name9","c76000009", "e.bronte", "emily.bronte@nodit.ie", "bronte", "emily", " ", "DT228", 4, "EL");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            
            // Add another item
            item = newItem("name10","c76000010", "s.connor", "sarah.connor@nodit.ie", "connor", "sarah", "c", "DT211", 2, "EL");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);

    	} catch (AmazonServiceException ase)
    	{
    		 System.out.println("Caught an AmazonServiceException, which means your request made it "
                     + "to AWS, but was rejected with an error response for some reason.");
             System.out.println("Error Message:    " + ase.getMessage());
             System.out.println("HTTP Status Code: " + ase.getStatusCode());
             System.out.println("AWS Error Code:   " + ase.getErrorCode());
             System.out.println("Error Type:       " + ase.getErrorType());
             System.out.println("Request ID:       " + ase.getRequestId());
    	}catch (AmazonClientException ace) 
    	{
    		   System.out.println("Caught an AmazonClientException, which means the client encountered "
                       + "a serious internal problem while trying to communicate with AWS, "
                       + "such as not being able to access the network.");
               System.out.println("Error Message: " + ace.getMessage());
    	}
    	
       
        /**
         * Open DB, pass in students.csv.
         * Check DB 
         * For each user in DBstudents.csv, create an instance.
         * 
         */
        createInstance();   		
        	    	

    }
    static void createInstance()
    {
    	AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
		AmazonEC2 ec2 = new AmazonEC2Client(credentialsProvider);
		ec2.setEndpoint("ec2.us-east-1.amazonaws.com");
		
    	// CREATE EC2 INSTANCES
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
		    .withInstanceType("t1.micro")
		    .withImageId("ami-3bec7952")
		    .withMinCount(1)
		    .withMaxCount(1)
		    .withSecurityGroupIds("dt228-3-cloud")
		    .withKeyName("test.key")
		    .withAdditionalInfo("TEST!!");
		
		RunInstancesResult runInstances = ec2.runInstances(runInstancesRequest);

		// TAG EC2 INSTANCES
		List<Instance> instances = runInstances.getReservation().getInstances();
		int idx = 1;
		for (Instance instance : instances) 
		{
		  CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		  createTagsRequest.withResources(instance.getInstanceId()) //
		      .withTags(new Tag("Student 1", "TEST!!!!!!!!!!!" + idx));
		  ec2.createTags(createTagsRequest);

		  idx++;
		}
		
		System.out.println("EC2 Instances: " +runInstancesRequest); 
		
    }
    private static Map<String, AttributeValue> newItem(String name, String studentid, String username, String email, String surname, String firstname, String mi, String programme, int stage, String rstat) 
    {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        
        item.put("name", new AttributeValue(name));
        item.put("studentid", new AttributeValue(studentid));
        item.put("username", new AttributeValue(username));
        item.put("email", new AttributeValue(email));
        item.put("surname", new AttributeValue(surname));
        item.put("firstname", new AttributeValue(firstname));
        item.put("mi", new AttributeValue(mi));
        item.put("programme", new AttributeValue(programme));
        item.put("stage", new AttributeValue().withN(Integer.toString(stage)));
        item.put("rstat", new AttributeValue(rstat));

        return item;
    }

    private static void waitForTableToBecomeAvailable(String tableName) 
    {
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {Thread.sleep(1000 * 20);} catch (Exception e) {}
            try {
                DescribeTableRequest request = new DescribeTableRequest().withTableName(tableName);
                TableDescription tableDescription = dynamoDB.describeTable(request).getTable();
                String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
            } catch (AmazonServiceException ase) {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) throw ase;
            }
        }

        throw new RuntimeException("Table " + tableName + " never went active");
    }
    private static void verifyEmailAddress(AmazonSimpleEmailService ses, String address) {
        ListVerifiedEmailAddressesResult verifiedEmails = ses.listVerifiedEmailAddresses();
        if (verifiedEmails.getVerifiedEmailAddresses().contains(address)) return;

        ses.verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(address));
        System.out.println("Please check the email address " + address + " to verify it");
        System.exit(0);
    }
}
