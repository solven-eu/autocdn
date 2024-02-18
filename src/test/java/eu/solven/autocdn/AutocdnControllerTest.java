package eu.solven.autocdn;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import eu.solven.autocdn.spring.ISpringProfiles;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		classes = { AutocdnApplication.class, AutocdnControllerTest.Complement.class })
@AutoConfigureMockMvc
@ActiveProfiles(ISpringProfiles.SYNCHRONOUS)
public class AutocdnControllerTest {

	@Configuration
	public static class Complement {
		@Bean
		public SubscriberExceptionHandler subscriberExceptionHandler() {
			return new SubscriberExceptionHandler() {

				@Override
				public void handleException(Throwable exception, SubscriberExceptionContext context) {
					throw new RuntimeException(exception);
				}
			};
		}
	}

	@Autowired
	private MockMvc mvc;

	@Test
	public void redirectRelativeUrls() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/cdn?url=unitTest.jpg"))
				.andExpect(MockMvcResultMatchers.status().isFound())
				.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "unitTest.jpg"))
				.andExpect(MockMvcResultMatchers.content().string(""));
	}

	@Test
	public void deleteRelativeUrls() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete("/cdn?url=unitTest.jpg"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", CoreMatchers.is("unitTest.jpg")));
	}
}