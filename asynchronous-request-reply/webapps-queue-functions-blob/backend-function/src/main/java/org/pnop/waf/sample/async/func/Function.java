package org.pnop.waf.sample.async.func;

import java.util.Date;
import java.util.logging.Logger;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class Function {
    /**
     * This function will be invoked when a new message is received at the specified path. The message contents are provided as input to this function.
     * @throws JsonProcessingException
     * @throws JsonMappingException
     * @throws InterruptedException
     */
    @FunctionName("Function")
    public void run(
        @QueueTrigger(name = "message", queueName = "asyncreqrep", connection = "ConnectionString") String message,
        @BindingName("DequeueCount") int dequeueCount,
        @BindingName("ExpirationTime") Date expirationTime,
        @BindingName("InsertionTime") Date insertionTime,
        final ExecutionContext context)
        throws JsonMappingException, JsonProcessingException, InterruptedException {

        var logger = context.getLogger();
        var conn = System.getenv("ConnectionString");

        logger.info("message :       : " + message);
        logger.info("dequeue count   : " + dequeueCount);
        logger.info("insertion time  : " + insertionTime);
        logger.info("expiration time : " + expirationTime);

        var mapper = new ObjectMapper();
        Payload payload = mapper.readValue(message, Payload.class);

        var blob = new BlobClientBuilder()
            .connectionString(conn)
            .containerName("asyncreqrep")
            .blobName(payload.getId())
            .buildClient();

        // 遅延
        sleep(60, logger);

        // BLOBへファイル作成
        var headers = new BlobHttpHeaders().setContentType("text/plain");
        blob.upload(BinaryData.fromString(payload.getValue()), true);
        blob.setHttpHeaders(headers);
        return;
    }

    private void sleep(int sec, Logger logger) throws InterruptedException {
        for (int i = 0; i < sec; i++) {
            logger.info("processing : " + i);
            Thread.sleep(1000);
        }
    }
}
