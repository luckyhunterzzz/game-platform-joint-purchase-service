package com.gameplatform.jointpurchaseservice.media.service;

import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import com.gameplatform.jointpurchaseservice.media.config.MinioProperties;
import com.gameplatform.jointpurchaseservice.media.model.StoredImage;
import com.gameplatform.jointpurchaseservice.media.resolver.MediaUrlResolver;
import com.gameplatform.jointpurchaseservice.media.validation.ImageUploadValidator;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioMediaStorageService implements MediaStorageService {

    private static final String OFFER_FOLDER = "joint-purchases/offers";
    private static final String APPLICATION_FOLDER = "joint-purchases/applications";

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ImageUploadValidator imageUploadValidator;
    private final MediaUrlResolver mediaUrlResolver;
    private final WebpImageConverter webpImageConverter;

    @Override
    public StoredImage uploadOfferScreenshot(MultipartFile file) {
        return uploadImage(file, OFFER_FOLDER, minioProperties.getPublicBucket());
    }

    @Override
    public StoredImage uploadApplicationScreenshot(MultipartFile file) {
        return uploadImage(file, APPLICATION_FOLDER, minioProperties.getPublicBucket());
    }

    @Override
    public String createApplicationScreenshotAccessUrl(String bucket, String objectKey) {
        return mediaUrlResolver.resolvePublicUrl(bucket, objectKey);
    }

    private StoredImage uploadImage(MultipartFile file, String folder, String bucket) {
        imageUploadValidator.validate(file);

        PreparedImage preparedImage = prepareImage(file);
        String objectKey = folder + "/" + UUID.randomUUID() + "." + preparedImage.extension();

        try (InputStream inputStream = new ByteArrayInputStream(preparedImage.bytes())) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, preparedImage.bytes().length, -1)
                            .contentType(preparedImage.contentType())
                            .build()
            );
        } catch (Exception exception) {
            throw new BadRequestException("Failed to upload image to MinIO");
        }

        return new StoredImage(
                bucket,
                objectKey,
                mediaUrlResolver.resolvePublicUrl(bucket, objectKey)
        );
    }

    private PreparedImage prepareImage(MultipartFile file) {
        try {
            WebpImageConverter.ConvertedImage convertedImage = webpImageConverter.convert(
                    file.getBytes(),
                    file.getContentType()
            );

            return new PreparedImage(
                    convertedImage.bytes(),
                    convertedImage.contentType(),
                    convertedImage.extension()
            );
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadRequestException("Failed to prepare image for upload");
        }
    }

    private record PreparedImage(byte[] bytes, String contentType, String extension) {
    }
}
