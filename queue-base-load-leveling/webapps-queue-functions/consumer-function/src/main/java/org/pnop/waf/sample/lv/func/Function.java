package org.pnop.waf.sample.lv.func;

import java.util.Date;

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
        @QueueTrigger(name = "message", queueName = "loadleveling", connection = "ConnectionString") String message,
        @BindingName("DequeueCount") int dequeueCount,
        @BindingName("ExpirationTime") Date expirationTime,
        @BindingName("InsertionTime") Date insertionTime,
        final ExecutionContext context)
        throws JsonMappingException, JsonProcessingException, InterruptedException {

        var logger = context.getLogger();
        logger.info("message :       : " + message);
        logger.info("dequeue count   : " + dequeueCount);
        logger.info("insertion time  : " + insertionTime);
        logger.info("expiration time : " + expirationTime);

        var mapper = new ObjectMapper();
        Payload payload = mapper.readValue(message, Payload.class);

        for (int i = 0; i < payload.getValue(); i++) {
            logger.info("processing : " + i);
            Thread.sleep(1000);
        }
    }
}
