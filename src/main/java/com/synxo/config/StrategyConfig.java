package com.synxo.config;

import com.synxo.domain.strategy.MatchingStrategy;
import com.synxo.domain.strategy.ProximityStrategy;
import com.synxo.domain.strategy.RecommendationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfig {

	@Bean
	public MatchingStrategy recommendationStrategy() {
		return new RecommendationStrategy();
	}

	@Bean
	public MatchingStrategy proximityStrategy() {
		return new ProximityStrategy();
	}
}
