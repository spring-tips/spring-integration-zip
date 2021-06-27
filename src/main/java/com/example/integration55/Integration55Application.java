package com.example.integration55;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.zip.splitter.UnZipResultSplitter;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.integration.zip.transformer.ZipTransformer;
import org.springframework.nativex.hint.TypeHint;

import java.io.File;

@TypeHint(types = org.springframework.messaging.support.GenericMessage.class)
@SpringBootApplication
public class Integration55Application {

	public static void main(String[] args) {
		SpringApplication.run(Integration55Application.class, args);
	}

	@Bean
	UnZipTransformer read() {
		return new UnZipTransformer();
	}

	@Bean
	UnZipResultSplitter unZipResultSplitter() {
		return new UnZipResultSplitter();
	}

	@Bean
	ZipTransformer write() {
		return new ZipTransformer();
	}

	@Bean
	IntegrationFlow files(
		UnZipTransformer read,
		UnZipResultSplitter unZipResultSplitter,
		ZipTransformer write,
		@Value("file:///${user.home}/Desktop/inbound") File in,
		@Value("file:///${user.home}/Desktop/outbound") File out) {
		var inbound = Files
			.inboundAdapter(in)
			.autoCreateDirectory(true);
		return IntegrationFlows
			.from(inbound, pm -> pm.poller(pc -> pc.fixedRate(1000)))
			.transform(read)
			.split(unZipResultSplitter)
			.transform(write)
			.handle(Files
				.outboundAdapter(out)
				.autoCreateDirectory(true)
				.fileNameGenerator(in1 -> in1.getHeaders().get(FileHeaders.FILENAME) + ".zip")
			)
			.get();
	}


}


