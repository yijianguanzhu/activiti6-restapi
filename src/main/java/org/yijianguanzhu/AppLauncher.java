package org.yijianguanzhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = { org.activiti.spring.boot.SecurityAutoConfiguration.class })
public class AppLauncher {

	public static void main( String[] args ) {
		SpringApplication.run( AppLauncher.class, args );
	}
}
