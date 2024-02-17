package eu.solven.autocdn;

import org.springframework.http.MediaType;

import lombok.Value;

@Value
public class UrlContent {
	UrlHolder urlHolder;

	byte[] bytes;

	MediaType contentType;
	MediaType contentEncoding;
}
