package eu.solven.autocdn.event_pojo;

import org.springframework.http.MediaType;

import lombok.Value;

@Value
public class UrlContent {
	UrlHolder urlHolder;

	byte[] bytes;

	MediaType contentType;
	MediaType contentEncoding;
}
