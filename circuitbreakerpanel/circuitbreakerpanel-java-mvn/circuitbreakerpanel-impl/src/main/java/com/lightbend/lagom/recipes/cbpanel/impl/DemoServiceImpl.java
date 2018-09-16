package com.lightbend.lagom.recipes.cbpanel.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.client.CircuitBreakersPanel;
import com.lightbend.lagom.recipes.cbpanel.api.DemoService;
import com.lightbend.lagom.recipes.cbpanel.api.User;
import com.lightbend.lagom.recipes.cbpanel.impl.repository.UserRepository;

import javax.inject.Inject;

public class DemoServiceImpl implements DemoService {
    
    private UserRepository userRepository;
    
    private CircuitBreakersPanel circuitBreakerPanel;
    
    @Inject
    public DemoServiceImpl(UserRepository userRepository, CircuitBreakersPanel circuitBreakerPanel) {
        this.userRepository = userRepository;
        this.circuitBreakerPanel = circuitBreakerPanel;
    }
    
    
    /**
     * The CircuitBreakerPanel#withCircuitBreaker method accepts as string as the
     * circuitBreaker name ( configuration for which it picks from the application.conf)
     * As a second parameter it accepts a supplier in which you can make your call to the
     * external service
     */
    @Override
    public ServiceCall<NotUsed, User> getUser(Integer userId) {

        return request -> circuitBreakerPanel.withCircuitBreaker("user-repository-breaker",
                () -> userRepository.getUser(userId));
    }
}
