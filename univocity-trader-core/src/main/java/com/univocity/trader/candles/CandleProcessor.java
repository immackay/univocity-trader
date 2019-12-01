package com.univocity.trader.candles;

import com.univocity.trader.*;
import com.univocity.trader.strategy.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

public class CandleProcessor<T> {

	private static final Logger log = LoggerFactory.getLogger(CandleProcessor.class);

	private final Engine consumer;
	private final Exchange exchange;

	public CandleProcessor(Engine consumer, Exchange<T> exchange) {
		this.consumer = consumer;
		this.exchange = exchange;
	}

	public void processCandle(String symbol, Candle candle, boolean initializing) {
		if (!StringUtils.equalsIgnoreCase(symbol, consumer.getSymbol())) {
			return;
		}
		try {
			if (!initializing) {
				log.debug("{} - {}", symbol, candle);
			}
			consumer.process(candle, initializing);
		} catch (Exception e) {
			log.error("Error processing current candle:" + candle, e);
		}
	}

	public void processCandle(String symbol, T realTimeTick, boolean initializing) {
		try {
			if (!StringUtils.equalsIgnoreCase(symbol, consumer.getSymbol())) {
				return;
			}
			synchronized (consumer) {
				PreciseCandle tick = exchange.generatePreciseCandle(realTimeTick);
				if (!CandleRepository.addToHistory(consumer.getSymbol(), tick, initializing)) {  //already processed, skip.
					return;
				}
				Candle candle = exchange.generateCandle(realTimeTick);

				processCandle(symbol, candle, initializing);
			}
		} catch (Exception e) {
			log.error("Error processing event:" + realTimeTick, e);
		}
	}


}

