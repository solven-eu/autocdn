package eu.solven.autocdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import eu.solven.pepper.thread.PepperExecutorsHelper;

@Configuration
public class AsyncCdnSpringConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCdnSpringConfig.class);

	@Bean
	public EventBus eventBus() {
		return new AsyncEventBus(PepperExecutorsHelper.newShrinkableFixedThreadPool("AsyncEventBus"),
				new SubscriberExceptionHandler() {

					@Override
					public void handleException(Throwable exception, SubscriberExceptionContext context) {
						LOGGER.warn("Arg: {} {} {}",
								context.getEvent(),
								context.getSubscriber(),
								context.getSubscriberMethod(),
								exception);
					}
				});
	}
}
