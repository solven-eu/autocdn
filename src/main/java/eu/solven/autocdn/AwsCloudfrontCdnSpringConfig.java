package eu.solven.autocdn;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.solven.autocdn.event_pojo.UrlContent;
import eu.solven.autocdn.event_pojo.UrlHolder;
import eu.solven.pepper.logging.PepperLogHelper;

@Configuration
public class AwsCloudfrontCdnSpringConfig implements InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(AwsCloudfrontCdnSpringConfig.class);

	final EventBus eventBus;

	public AwsCloudfrontCdnSpringConfig(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		eventBus.register(this);
	}

	@Subscribe
	@AllowConcurrentEvents
	public void onUrlHolder(UrlHolder urlHolder) throws MalformedURLException, RestClientException, URISyntaxException {
		// TODO What should we do if the URL has a `#...` fragment?
		String rawUrl = urlHolder.getUrl();
		URL url = new URL(rawUrl);

		ResponseEntity<byte[]> responseEntity = downloadContent(url);

		HttpStatusCode statusCode = responseEntity.getStatusCode();
		if (!statusCode.is2xxSuccessful()) {
			throw new IllegalStateException("We got a " + statusCode + " on " + rawUrl);
		}

		byte[] bytes = responseEntity.getBody();
		if (bytes == null) {
			LOGGER.warn("We received a null byte[] from {}", rawUrl);
			return;
		}

		LOGGER.info("We downloded {} from {}", PepperLogHelper.humanBytes(bytes.length), rawUrl);
		UrlContent urlContent = wrapAsUrlContent(urlHolder, responseEntity, bytes);
		eventBus.post(urlContent);
	}

	private ResponseEntity<byte[]> downloadContent(URL url) throws URISyntaxException {
		// https://stackoverflow.com/questions/29418583/follow-302-redirect-using-spring-resttemplate
		// By default, we follow redirects
		RestTemplate rt = new RestTemplate();

		ResponseEntity<byte[]> responseEntity = rt.getForEntity(url.toURI(), byte[].class);
		return responseEntity;
	}

	private UrlContent wrapAsUrlContent(UrlHolder urlHolder, ResponseEntity<byte[]> responseEntity, byte[] bytes) {
		HttpHeaders headers = responseEntity.getHeaders();
		UrlContent urlContent = new UrlContent(urlHolder, bytes, headers.getContentType(), null);
		return urlContent;
	}

	@Subscribe
	@AllowConcurrentEvents
	public void onUrlContent(UrlContent urlContent) {
		AwsS3ContentUploader uploader = new AwsS3ContentUploader();

		uploader.doUpload(urlContent);
	}

}
