package org.yijianguanzhu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * activiti6 内部服务使用，无需认证
 * 
 * @author yijianguanzhu 2021年02月05日
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	public void configure( WebSecurity web ) {
		web.ignoring().mvcMatchers( "/**" );
	}

	@Override
	protected void configure( HttpSecurity http ) throws Exception {
		http.sessionManagement().sessionCreationPolicy( SessionCreationPolicy.STATELESS )
				.and().csrf().disable();
	}
}
