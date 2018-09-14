package com.video.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class VideoApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent>{


	private static final Logger LOGGER = LoggerFactory.getLogger(VideoApplicationReadyEventListener.class);
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
				
		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		try {
			LOGGER.info("==================開始加載檔案查詢==================");
			FindFileCache findFileCache = applicationContext.getBean(FindFileCache.class);
			findFileCache.reload();
			LOGGER.info("==================結束加載檔案查詢==================");
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	

}
