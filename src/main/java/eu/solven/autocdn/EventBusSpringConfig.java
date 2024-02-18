package eu.solven.autocdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import eu.solven.autocdn.spring.ISpringProfiles;
import eu.solven.pepper.thread.PepperExecutorsHelper;

@Configuration
public class EventBusSpringConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventBusSpringConfig.class);

	@Bean
	public SubscriberExceptionHandler subscriberExceptionHandler() {
		return makeExceptionhandler();
	}

	@Profile(ISpringProfiles.ASYNCHRONOUS)
	@Bean
	public EventBus asynchronousEventBus(SubscriberExceptionHandler subscriberExceptionHandler) {
		return new AsyncEventBus(PepperExecutorsHelper.newShrinkableFixedThreadPool("AsyncEventBus"),
				subscriberExceptionHandler);
	}

	@Profile(ISpringProfiles.SYNCHRONOUS)
	@Bean
	public EventBus synchronousEventBus(SubscriberExceptionHandler subscriberExceptionHandler) {
		return new EventBus(subscriberExceptionHandler);
	}

	private SubscriberExceptionHandler makeExceptionhandler() {
		return new SubscriberExceptionHandler() {

			@Override
			public void handleException(Throwable exception, SubscriberExceptionContext context) {
				LOGGER.warn("Arg: {} {} {}",
						context.getEvent(),
						context.getSubscriber(),
						context.getSubscriberMethod(),
						exception);
			}
		};
	}
}
