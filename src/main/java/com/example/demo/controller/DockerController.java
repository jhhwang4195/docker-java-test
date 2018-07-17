package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;

@RestController
public class DockerController {
	
	public static List<String> getDockerLogs(DockerClient dockerClient, String containerId) {
	    final List<String> logs = new ArrayList<>();

	    LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId);
	    logContainerCmd.withStdOut(true).withStdErr(true).withTail(10);
	    try {
	        logContainerCmd.exec(new LogContainerResultCallback() {
	            @Override
	            public void onNext(Frame item) {
	                logs.add(item.toString());
	            }
	        }).awaitCompletion();
	    } catch (InterruptedException e) {
	        System.out.println("Failed to retrieve logs of container " + containerId +e.toString());
	    }

	    return logs;
	}

	@RequestMapping("/")
	public String index() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
			    .withDockerHost("tcp://40.121.52.173:4000")
			    .withDockerTlsVerify(false)
			    .withApiVersion("1.23")
			    .build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		
		System.out.println("\n\n##### Info");
		Info info = dockerClient.infoCmd().exec();
		System.out.println(info);
		System.out.println(info.getContainers());
		System.out.println(info.getArchitecture());
		
		System.out.println("\n\n##### Container List");
		List<Container> list_con = dockerClient.listContainersCmd().exec();
		for(int i=0; i<list_con.size(); i++) {
			System.out.println(list_con.get(i).getNames()[0]);
			System.out.println(list_con.get(i).getId());
		}
		
		System.out.println("\n\n##### Container Inspect");
		InspectContainerResponse con = dockerClient.inspectContainerCmd("1b97862ea7ac").exec();
		System.out.println(con);
		System.out.println(con.getId());
		System.out.println(con.getName());
		
		System.out.println("\n\n##### Container Log");
		// Reference: https://www.programcreek.com/java-api-examples/?api=com.github.dockerjava.api.command.LogContainerCmd
		List<String> con_logs = getDockerLogs(dockerClient, "1b97862ea7ac");
		for (int i=0; i<con_logs.size(); i++) {
			System.out.println(con_logs.get(i));
		}
		
		System.out.println("\n\n##### Container Top");
		TopContainerResponse con_top = dockerClient.topContainerCmd("1b97862ea7ac").exec();
		for(int i=0; i<con_top.getTitles().length; i++) {
			System.out.println(con_top.getTitles()[i].toString());
		}
		
		for(int i=0; i<con_top.getProcesses().length; i++) {
			System.out.println("Process: " +i);
			for(int j=0; j<con_top.getProcesses()[i].length; j++) {
				System.out.println(con_top.getProcesses()[i][j].toString());
			}
		}
		
		return "hello world";
	}
}
