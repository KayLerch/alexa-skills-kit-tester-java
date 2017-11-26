package io.klerch.alexa.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.client.AlexaClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Lambda implements RequestStreamHandler {
    private final static String S3_BUCKET_PROPERTY = "s3Bucket";
    private final static String S3_KEY_PROPERTY = "s3Key";
    private final static String S3_REGION_PROPERTY = "s3Region";

    private final static Logger log = Logger.getLogger(Lambda.class);
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {
        final String inputS = IOUtils.toString(Optional.ofNullable(input).orElse(new ByteArrayInputStream("{}".getBytes())));
        final JsonNode root = om.readTree(inputS);

        final String bucket = Optional.ofNullable(root.get(S3_BUCKET_PROPERTY)).map(JsonNode::textValue).filter(StringUtils::isNotBlank).orElse(System.getenv(S3_BUCKET_PROPERTY));
        Validate.notBlank(bucket, S3_BUCKET_PROPERTY + " hasn't been set in the request payload nor as an environment variable.");

        final String key = Optional.ofNullable(root.get(S3_KEY_PROPERTY)).map(JsonNode::textValue).filter(StringUtils::isNotBlank)
                .orElse(System.getenv(S3_KEY_PROPERTY));
        final String region = Optional.ofNullable(root.get(S3_REGION_PROPERTY)).map(JsonNode::textValue).filter(StringUtils::isNotBlank)
                .orElse(System.getenv(S3_REGION_PROPERTY));

        final AmazonS3 s3client = StringUtils.isNotBlank(region) ? AmazonS3ClientBuilder.standard().withRegion(region).build() : AmazonS3ClientBuilder.defaultClient();

        final ListObjectsRequest listRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(Optional.ofNullable(key).map(k -> k + (k.endsWith("/") ? "" : "/")).orElse(""));

        log.info("[INFO] Reading out *.yml conversation script files in folder '" + listRequest.getPrefix() + "' in bucket '" + listRequest.getBucketName() + "'");

        final List<S3ObjectSummary> conversationScripts = s3client.listObjects(listRequest).getObjectSummaries().stream()
                .filter(os -> os.getKey().toLowerCase().endsWith(".yml")).collect(Collectors.toList());

        log.info("[INFO] Found " + conversationScripts.size() + " conversation script files in bucket '" + bucket + "'");

        for (final S3ObjectSummary conversationScript : conversationScripts) {
            log.info("[INFO] Load conversation script file " + conversationScript.getKey() + " from S3 bucket " + bucket);

            AlexaClient.create(s3client.getObject(bucket, conversationScript.getKey()).getObjectContent())
                    .build()
                    .startScript();
        }
        output.write("{ \"OK\" }".getBytes());
    }
}
