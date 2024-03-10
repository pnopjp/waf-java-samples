package org.pnop.waf.sample.async.sb.services;

import java.net.URI;
import java.time.OffsetDateTime;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BlobService {

    private BlobContainerClient blobContainerClient;

    @Value("${my.application.connection-string}")
    private String connectionString;

    @Value("${my.application.containerName}")
    private String containerName;

    public BlobService() {
    }

    @PostConstruct
    private void postConstruct() {
        log.info("Connection string :{}", connectionString);
        log.info("Container name    :{}", containerName);

        this.blobContainerClient = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(containerName)
            .buildClient();
        if (this.blobContainerClient.exists() == false) {
            this.blobContainerClient.create();
        }
    }

    public boolean exists(String id) {
        BlobClient blob = this.blobContainerClient.getBlobClient(id);
        return blob.exists();
    }

    public URI getUrl(String id) {
        BlobClient blob = this.blobContainerClient.getBlobClient(id);
        var expiryTime = OffsetDateTime.now().plusMinutes(10);
        var permissions = new BlobSasPermission().setReadPermission(true);
        var values = new BlobServiceSasSignatureValues(expiryTime, permissions)
            .setStartTime(OffsetDateTime.now().minusMinutes(1));
        var sas = blob.generateSas(values);
        return URI.create(blob.getBlobUrl() + "?" + sas);
    }
}
