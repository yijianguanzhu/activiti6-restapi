package org.yijianguanzhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = { org.activiti.spring.boot.SecurityAutoConfiguration.class })
public class AppLauncher extends SpringBootServletInitializer {

	public static void main( String[] args ) {
		SpringApplication.run( AppLauncher.class, args );
	}

	@Override
	protected SpringApplicationBuilder configure( SpringApplicationBuilder application ) {
		return application.sources( AppLauncher.class );
	}
}
