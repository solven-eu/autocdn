package eu.solven.autocdn;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This {@link Controller} manages interactions with the CDN.
 * 
 * @author Benoit Lacelle
 *
 */
@RestController("/cdn")
public class AutocdnController {
	
	@GetMapping
	public ResponseEntity<?> index(@RequestParam String url) {
		// We never process relative URL
		// return new RedirectView(url, false);
		// https://stackoverflow.com/questions/29085295/spring-mvc-restcontroller-and-redirect
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build();
	}

	@DeleteMapping
	public Map<String, ?> invalidate(@RequestParam String url) {

		// Return the input URL to help analysis (e.g. wrong URL encoding)
		return Map.of("url", url);
	}

}