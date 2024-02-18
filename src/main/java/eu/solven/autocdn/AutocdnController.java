package eu.solven.autocdn;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.Controller;

import com.google.common.eventbus.EventBus;

import eu.solven.autocdn.event_pojo.UrlHolder;
import eu.solven.autocdn.event_pojo.UrlToInvalidate;

/**
 * This {@link Controller} manages interactions with the CDN.
 * 
 * @author Benoit Lacelle
 *
 */
@RestController
// https://stackoverflow.com/questions/25569303/the-dispatcherservlet-configuration-needs-to-include-a-handleradapter-that-suppo
@RequestMapping("/cdn")
public class AutocdnController {

	final EventBus eventBus;

	public AutocdnController(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@GetMapping
	public ResponseEntity<?> pushToCdn(@RequestParam String url) {
		eventBus.post(new UrlHolder(url));

		// We never process relative URL
		// return new RedirectView(url, false);
		// https://stackoverflow.com/questions/29085295/spring-mvc-restcontroller-and-redirect
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build();
	}

	@DeleteMapping
	public Map<String, ?> invalidate(@RequestParam String url) {
		eventBus.post(new UrlToInvalidate(new UrlHolder(url)));

		// Return the input URL to help analysis (e.g. wrong URL encoding)
		return Map.of("url", url);
	}

}