
import java.io.InputStream;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;



public class Main 
{
	
	private static void init() throws Exception {
    	/*
		 * This credentials provider implementation loads your AWS credentials
		 * from a properties file at the root of your classpath.
		 */
        dynamoDB = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        dynamoDB.setRegion(usEast1);
    }
	
	static AmazonDynamoDBClient dynamoDB;
	
    public static void main(String[] args) throws Exception 
    {
    	try {
            String tableName = "students3";
    	} catch (AmazonServiceException ase)
    	{
    		
    	}catch (AmazonClientException ace) 
    	{
    		
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
}
