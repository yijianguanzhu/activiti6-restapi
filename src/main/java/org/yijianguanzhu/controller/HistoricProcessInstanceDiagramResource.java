package org.yijianguanzhu.controller;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * activiti6 rest api未提供历史流程实例路线图的获取
 * <p>
 * 这里自定义接口以获取历史流程实例路线图
 * </p>
 * 
 * @see org.activiti.rest.service.api.runtime.process.ProcessInstanceDiagramResource
 * @see <a href="https://cloud.tencent.com/developer/article/1165460" />
 *
 * @author yijianguanzhu 2021年02月05日
 */
@RestController
public class HistoricProcessInstanceDiagramResource {

	@Autowired
	protected HistoryService historyService;

	@Autowired
	protected RepositoryService repositoryService;

	@Autowired
	protected ProcessEngineConfiguration processEngineConfiguration;

	@RequestMapping(value = "/history/historic-process-instances/{processInstanceId}/diagram", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getHistoricProcessInstanceDiagram(
			@PathVariable String processInstanceId, HttpServletResponse response ) {

		try {
			byte[] bytes = getHistoricProcessInstanceDiagram( processInstanceId );
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set( HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE );
			return new ResponseEntity<byte[]>( bytes, responseHeaders, HttpStatus.OK );
		}
		catch ( IOException e ) {
			throw new ActivitiIllegalArgumentException( "Error exporting history diagram", e );
		}
	}

	/**
	 * 获取已经流转的线
	 *
	 * @param bpmnModel
	 * @param historicActivityInstances
	 * @return
	 */
	private static List<String> getHighLightedFlows( BpmnModel bpmnModel,
			List<HistoricActivityInstance> historicActivityInstances ) {
		// 高亮流程已发生流转的线id集合
		List<String> highLightedFlowIds = new ArrayList<>();
		// 全部活动节点
		List<FlowNode> historicActivityNodes = new ArrayList<>();
		// 已完成的历史活动节点
		List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

		for ( HistoricActivityInstance historicActivityInstance : historicActivityInstances ) {
			FlowNode flowNode = ( FlowNode ) bpmnModel.getMainProcess()
					.getFlowElement( historicActivityInstance.getActivityId(), true );
			historicActivityNodes.add( flowNode );
			if ( historicActivityInstance.getEndTime() != null ) {
				finishedActivityInstances.add( historicActivityInstance );
			}
		}

		FlowNode currentFlowNode = null;
		FlowNode targetFlowNode = null;
		// 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
		for ( HistoricActivityInstance currentActivityInstance : finishedActivityInstances ) {
			// 获得当前活动对应的节点信息及outgoingFlows信息
			currentFlowNode = ( FlowNode ) bpmnModel.getMainProcess().getFlowElement( currentActivityInstance.getActivityId(),
					true );
			List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

			/**
			 * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转：
			 * 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
			 * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
			 */
			if ( "parallelGateway".equals( currentActivityInstance.getActivityType() )
					|| "inclusiveGateway".equals( currentActivityInstance.getActivityType() ) ) {
				// 遍历历史活动节点，找到匹配流程目标节点的
				for ( SequenceFlow sequenceFlow : sequenceFlows ) {
					targetFlowNode = ( FlowNode ) bpmnModel.getMainProcess().getFlowElement( sequenceFlow.getTargetRef(), true );
					if ( historicActivityNodes.contains( targetFlowNode ) ) {
						highLightedFlowIds.add( targetFlowNode.getId() );
					}
				}
			}
			else {
				List<Map<String, Object>> tempMapList = new ArrayList<>();
				for ( SequenceFlow sequenceFlow : sequenceFlows ) {
					for ( HistoricActivityInstance historicActivityInstance : historicActivityInstances ) {
						if ( historicActivityInstance.getActivityId().equals( sequenceFlow.getTargetRef() ) ) {
							Map<String, Object> map = new HashMap<>();
							map.put( "highLightedFlowId", sequenceFlow.getId() );
							map.put( "highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime() );
							tempMapList.add( map );
						}
					}
				}
				if ( !CollectionUtils.isEmpty( tempMapList ) ) {
					// 遍历匹配的集合，取得开始时间最早的一个
					long earliestStamp = 0L;
					String highLightedFlowId = null;
					for ( Map<String, Object> map : tempMapList ) {
						long highLightedFlowStartTime = Long.parseLong( map.get( "highLightedFlowStartTime" ).toString() );
						if ( earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime ) {
							highLightedFlowId = map.get( "highLightedFlowId" ).toString();
							earliestStamp = highLightedFlowStartTime;
						}
					}
					highLightedFlowIds.add( highLightedFlowId );
				}
			}
		}
		return highLightedFlowIds;
	}

	/**
	 * 根据流程实例Id,获取实时流程图片
	 *
	 * @param processInstanceId
	 * @return
	 */
	public byte[] getHistoricProcessInstanceDiagram( String processInstanceId ) throws IOException {

		if ( StringUtils.isEmpty( processInstanceId ) ) {
			throw new ActivitiIllegalArgumentException( "Variable operation is missing for variable: processInstanceId" );
		}
		// 获取历史流程实例
		HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId( processInstanceId ).singleResult();

		if ( historicProcessInstance == null ) {
			throw new ActivitiObjectNotFoundException(
					"Could not find a history process instance with id '" + processInstanceId + "'." );
		}

		// 获取流程中已经执行的节点，按照执行先后顺序排序
		List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId( processInstanceId )
				.orderByHistoricActivityInstanceId().asc().list();
		// 高亮已经执行流程节点ID集合
		List<String> highLightedActivitiIds = new ArrayList<>();
		for ( HistoricActivityInstance historicActivityInstance : historicActivityInstances ) {
			highLightedActivitiIds.add( historicActivityInstance.getActivityId() );
		}
		// 图片颜色线条标记
		ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();

		BpmnModel bpmnModel = repositoryService.getBpmnModel( historicProcessInstance.getProcessDefinitionId() );
		// 高亮流程已发生流转的线id集合
		List<String> highLightedFlowIds = getHighLightedFlows( bpmnModel, historicActivityInstances );

		// 使用默认配置获得流程图表生成器，并生成追踪图片字符流
		InputStream imageStream = processDiagramGenerator.generateDiagram( bpmnModel, "png", highLightedActivitiIds,
				highLightedFlowIds, processEngineConfiguration.getActivityFontName(),
				processEngineConfiguration.getLabelFontName(), processEngineConfiguration.getAnnotationFontName(),
				processEngineConfiguration.getClassLoader(), 2.0 );

		return IOUtils.toByteArray( imageStream );
	}
}
