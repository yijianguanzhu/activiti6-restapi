#资源获取
1.获取部署资源 e.g. xml文件、png图片

`http://localhost:8899/activiti-rest/repository/deployments/5007/resources`

2.获取部署图

`http://localhost:8899/activiti-rest/repository/deployments/3/resourcedata/etc4.Process_0ilbl1g.png`


# 多轮对话启动实例

0.根据deploymentId获取流程定义id

`http://localhost:8899/activiti-rest/repository/process-definitions?deploymentId=5007` GET

1.根据流程定义id启动流程实例
`http://localhost:8899/activiti-rest/runtime/process-instances` POST
	
	请求参数
	json body:
	{
     	    "processDefinitionId":"etc4:1:5010",
     	    "businessKey":"Multir_Round_Dialogue_Key"
     	}

	响应
	response:

		{
		    "id": "5011",
		    "url": "http://localhost:8899/activiti-rest/runtime/process-instances/5011",
		    "businessKey": "Multir_Round_Dialogue_Key",
		    "suspended": false,
		    "ended": false,
		    "processDefinitionId": "etc4:1:5010",
		    "processDefinitionUrl": "http://localhost:8899/activiti-rest/repository/process-definitions/etc4:1:5010",
		    "processDefinitionKey": "etc4",
		    "activityId": null,
		    "variables": [],
		    "tenantId": "",
		    "name": null,
		    "completed": false
		}

2.获取当前多轮对话进度图

`http://localhost:8899/activiti-rest/runtime/process-instances/{processInstanceId}/diagram` GET

3.根据流程实例id获取task

`http://localhost:8899/activiti-rest/runtime/tasks?processInstanceId=5011` GET

4.根据taskid，执行任务
`http://localhost:8899/activiti-rest/runtime/tasks/5015` POST

	请求参数
	{
	    "action":"complete",
	    "variables":[{
	        "name":"score",
	        "value":"424"
	    }]
	}
	
5.获取历史流程实例进度图

`http://localhost:8899/activiti-rest/history/historic-process-instances/{processInstanceId}/diagram` GET