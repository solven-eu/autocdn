package eu.solven.autocdn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpHeaders;

import eu.solven.autocdn.event_pojo.UrlContent;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest.Builder;

public class AwsS3ContentUploader implements DisposableBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(AwsS3ContentUploader.class);

	static final int MAX_OBJECT_KEYS_BYTES = 1024;

	// This is France: We hardcoded `autocdn` in FR
	// This should be set to the same region than this instance location
	// https://devcenter.heroku.com/articles/regions#viewing-available-regions
	final Region region = Region.EU_WEST_3;
	final S3Client s3 = S3Client.builder().region(region).build();

	private String inferBucketName() {
		// TODO This would change depending on current S3 region
		return "autocdn";
	}

	public void doUpload(UrlContent urlContent) {
		String bucketName = inferBucketName();

		Map<String, String> metadata = new HashMap<>();
		// metadata.put("x-amz-meta-myVal", "test");

		String objectKey = makeObjectKey(urlContent.getUrlHolder().getUrl());
		if (objectKey.startsWith(".") || objectKey.endsWith(".")) {
			// objectKeys starting with `./` or `../` or ending with `.` may introduce difficulties
			// This should not happen as we start with `host` and we end with a hash/sha
			throw new IllegalStateException("Illegal objectKey: `" + objectKey + "`");
		}

		Builder builder = PutObjectRequest.builder().bucket(bucketName).key(objectKey).metadata(metadata);

		if (urlContent.getContentType() != null) {
			builder = builder.contentType(urlContent.getContentType().toString());
		} else {
			LOGGER.warn("We lack {} for {}", HttpHeaders.CONTENT_TYPE, urlContent.getUrlHolder().getUrl());
		}
		if (urlContent.getContentEncoding() != null) {
			builder = builder.contentEncoding(urlContent.getContentEncoding().toString());
		} else {
			LOGGER.warn("We lack {} for {}", HttpHeaders.CONTENT_TYPE, urlContent.getUrlHolder().getUrl());
		}

		PutObjectRequest putOb = builder.build();

		// TODO Enabling upload without materializing the content inMemory
		s3.putObject(putOb, RequestBody.fromBytes(urlContent.getBytes()));
		LOGGER.info("Successfully placed " + objectKey + " into bucket " + bucketName);
	}

	// This objectKey may change from a version to another: it would not be a big deal, as it would simply lead to a
	// re-download of the resource
	private String makeObjectKey(String rawUrl) {
		URL url;
		try {
			url = new URL(rawUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(rawUrl);
		}

		// https://stackoverflow.com/questions/1719132/how-do-i-register-a-domain-with-special-symbols-or-characters
		String host = url.getHost();

		// Typically `http` or `https`
		String protocol = url.getProtocol();

		// We use `/` to simulate folders in S3, even it has to remain clear that S3 has not concept of folder.

		String hashSuffix = hash(rawUrl);

		// https://docs.aws.amazon.com/AmazonS3/latest/userguide/object-keys.html#object-key-guidelines
		String beforeHash = host + "/" + protocol + "/" + escape(url.getPath()) + "-" + escape(url.getQuery());
		String suffix = "-" + hashSuffix;

		// We cut and path and query part to ensure we have a short enough objectKey
		return beforeHash.substring(0, MAX_OBJECT_KEYS_BYTES - suffix.length()) + suffix;
	}

	/**
	 * 
	 * @param path
	 * @return a String where `/` is replaced by `_` and any non word character (`[a-zA-Z0-9_]`) is replaced by `{YYY}`
	 *         where YYY is the codepoint of the character.
	 */
	private String escape(String path) {
		String noSlash = path.replace('/', '_');
		return Pattern.compile("[^\\w\\.]")
				.matcher(noSlash)
				.replaceAll(mr -> "{" + Integer.toString(mr.group().codePointAt(0)) + "}");
	}

	private String hash(String rawUrl) {
		return Integer.toString(rawUrl.hashCode());
	}

	@Override
	public void destroy() throws Exception {
		// BEWARE We may be uploading asynchronously
		s3.close();
	}
}
