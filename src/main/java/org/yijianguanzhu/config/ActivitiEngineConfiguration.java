package org.yijianguanzhu.config;

import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author yijianguanzhu 2021年02月07日
 */
@Configuration
public class ActivitiEngineConfiguration implements InitializingBean {

	private static final String FONT_NAME = "宋体";

	@Autowired
	private ProcessEngineConfiguration processEngineConfiguration;

	@Override
	public void afterPropertiesSet() {
		// 处理图像导出乱码问题
		processEngineConfiguration.setActivityFontName( FONT_NAME );
		processEngineConfiguration.setAnnotationFontName( FONT_NAME );
		processEngineConfiguration.setLabelFontName( FONT_NAME );
	}
}
