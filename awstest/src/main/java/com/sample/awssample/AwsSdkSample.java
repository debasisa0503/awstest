package com.sample.awssample;
/*
 * Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * This class is a starting point for working with the AWS SDK for Java, and
 * shows how to make a few simple requests to Amazon EC2 and Amazon S3.
 *
 * Before you run this code, be sure to fill in your AWS security credentials in
 * the .aws/credentials file under your home directory.
 *
 * If you don't have an Amazon Web Services account, you can get started for
 * free: http://aws.amazon.com/free
 *
 * For lots more information on using the AWS SDK for Java, including
 * information on high-level APIs and advanced features, check out the AWS SDK
 * for Java Developer Guide:
 * http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/welcome.html
 *
 * Stay up to date with new features in the AWS SDK for Java by following the
 * AWS Java Developer Blog: https://java.awsblog.com
 */
public class AwsSdkSample {

	/*
	 * Important: Be sure to fill in your AWS access credentials in the
	 * .aws/credentials file under your home directory before you run this
	 * sample. http://aws.amazon.com/security-credentials
	 */
	static AmazonEC2 ec2;
	static AmazonS3 s3;

	/**
	 * The only information needed to create a client are security credentials -
	 * your AWS Access Key ID and Secret Access Key. All other configuration,
	 * such as the service endpoints have defaults provided.
	 *
	 * Additional client parameters, such as proxy configuration, can be
	 * specified in an optional ClientConfiguration object when constructing a
	 * client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.PropertiesCredentials
	 * @see com.amazonaws.ClientConfiguration
	 */
	private static void init() throws Exception {
		/*
		 * ProfileCredentialsProvider loads AWS security credentials from a
		 * .aws/config file in your home directory.
		 * 
		 * These same credentials are used when working with other AWS SDKs and
		 * the AWS CLI.
		 * 
		 * You can find more information on the AWS profiles config file here:
		 * http
		 * ://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started
		 * .html
		 */
		File configFile = new File(System.getProperty("user.home.1"),
				".aws/credentials");
		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(
				new ProfilesConfigFile(configFile), "default");

		if (credentialsProvider.getCredentials() == null) {
			throw new RuntimeException(
					"No AWS security credentials found:\n"
							+ "Make sure you've configured your credentials in: "
							+ configFile.getAbsolutePath()
							+ "\n"
							+ "For more information on configuring your credentials, see "
							+ "http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html");
		}
		//Constructor AmazonEC2Client(AWSCredentialsProvider) is deprecated.
		//ec2 = new AmazonEC2Client(credentialsProvider);
		ec2 = AmazonEC2ClientBuilder.standard()
				.withCredentials(credentialsProvider).build();
		// s3 = new AmazonS3Client(credentialsProvider);
		s3 = AmazonS3ClientBuilder.standard()
				.withCredentials(credentialsProvider).build();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Reading AWS Objects");
		readAWSObjects();
		System.out.println("Reading Objects in S3 Bucket");
		readS3BucketObjects();
	}

	public static void readAWSObjects() {
		System.out.println("===========================================");
		System.out.println("Welcome to the AWS Java SDK!");
		System.out.println("===========================================");

		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			/*
			 * The Amazon EC2 client allows you to easily launch and configure
			 * computing capacity in AWS datacenters.
			 * 
			 * In this sample, we use the EC2 client to list the availability
			 * zones in a region, and then list the instances running in those
			 * zones.
			 */
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2
					.describeAvailabilityZones();
			List<AvailabilityZone> availabilityZones = availabilityZonesResult
					.getAvailabilityZones();
			System.out.println("You have access to " + availabilityZones.size()
					+ " availability zones:");
			for (AvailabilityZone zone : availabilityZones) {
				System.out.println(" - " + zone.getZoneName() + " ("
						+ zone.getRegionName() + ")");
			}

			DescribeInstancesResult describeInstancesResult = ec2
					.describeInstances();
			Set<Instance> instances = new HashSet<Instance>();
			for (Reservation reservation : describeInstancesResult
					.getReservations()) {
				instances.addAll(reservation.getInstances());
			}

			System.out.println("You have " + instances.size()
					+ " Amazon EC2 instance(s) running.");

			/*
			 * The Amazon S3 client allows you to manage and configure buckets
			 * and to upload and download data.
			 * 
			 * In this sample, we use the S3 client to list all the buckets in
			 * your account, and then iterate over the object metadata for all
			 * objects in one bucket to calculate the total object count and
			 * space usage for that one bucket. Note that this sample only
			 * retrieves the object's metadata and doesn't actually download the
			 * object's content.
			 * 
			 * In addition to the low-level Amazon S3 client in the SDK, there
			 * is also a high-level TransferManager API that provides
			 * asynchronous management of uploads and downloads with an easy to
			 * use API:
			 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/
			 * amazonaws/services/s3/transfer/TransferManager.html
			 */
			List<Bucket> buckets = s3.listBuckets();
			System.out.println("You have " + buckets.size()
					+ " Amazon S3 bucket(s).");

			if (buckets.size() > 0) {
				Bucket bucket = buckets.get(0);

				long totalSize = 0;
				long totalItems = 0;
				/*
				 * The S3Objects and S3Versions classes provide convenient APIs
				 * for iterating over the contents of your buckets, without
				 * having to manually deal with response pagination.
				 */
				for (S3ObjectSummary objectSummary : S3Objects.inBucket(s3,
						bucket.getName())) {
					totalSize += objectSummary.getSize();
					totalItems++;
					System.out.println();
				}
				System.out.println("The bucket '" + bucket.getName()
						+ "' contains " + totalItems + " objects "
						+ "with a total size of " + totalSize + " bytes.");
				/*
				 * Finding total objects inside the bucket
				 */

			}
		} catch (AmazonServiceException ase) {
			/*
			 * AmazonServiceExceptions represent an error response from an AWS
			 * services, i.e. your request made it to AWS, but the AWS service
			 * either found it invalid or encountered an error trying to execute
			 * it.
			 */
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			/*
			 * AmazonClientExceptions represent an error that occurred inside
			 * the client on the local host, either while trying to send the
			 * request to AWS or interpret the response. For example, if no
			 * network connection is available, the client won't be able to
			 * connect to AWS to execute a request and will throw an
			 * AmazonClientException.
			 */
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	/*
	 * This method will list contents in an S3 buckets
	 */
	public static void readS3BucketObjects() {

		try {
			//List all buckets in s3
			List<Bucket> buckets = s3.listBuckets();
			String objKey;
			if (buckets.size() > 0) {
				//get into each individual buckets and read files inside the bucket
				for (int i = 0; i < buckets.size(); i++) {
					String bucketName = buckets.get(i).getName();
					System.out.println("Bucket Name: " + bucketName);
					ObjectListing object_listing = s3.listObjects(bucketName);
					for (Iterator<?> iterator = object_listing
							.getObjectSummaries().iterator(); iterator
							.hasNext();) {
						S3ObjectSummary summary = (S3ObjectSummary) iterator
								.next();
						//get keys for each file and read file content
						objKey = summary.getKey();
						S3Object s3object = s3.getObject(new GetObjectRequest(
								bucketName, objKey));
						System.out.println(s3object.getObjectMetadata()
								.getContentType());
						System.out.println(s3object.getObjectMetadata()
								.getContentLength());
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(
										s3object.getObjectContent()));
						String line;
						while ((line = reader.readLine()) != null) {
							// Printing the content
							System.out.println("File Content is : " + line);
						}
						reader.close();
					}
				}
			}

		} catch (AmazonServiceException e) {
			System.out.println("Error Message:    " + e.getMessage());
			System.out.println("HTTP Status Code: " + e.getStatusCode());
			System.out.println("AWS Error Code:   " + e.getErrorCode());
			System.out.println("Error Type:       " + e.getErrorType());
			System.out.println("Request ID:       " + e.getRequestId());
		} catch (AmazonClientException e) {
			System.out.println("Error Message:    " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error Message:    " + e.getMessage());
			e.printStackTrace();
		}

	}
}
