package org.pleutre.jmeter.plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;

public class NotificationReceiverWait extends NotificationAbstract {

	private static final Logger LOG = LoggingManager.getLoggerForClass();
	//private JMeterContext jmctx = JMeterContextService.getContext();
	//private String uuid = jmctx.getVariables().get("new-uuid");

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		// JMeterContext jmctx = JMeterContextService.getContext();
		// String uuid = jmctx.getVariables().get("new-uuid");
		String uuid = context.getParameter("FUNCTIONAL_ID");
		SampleResult result = new SampleResult();
		CompletableFuture<String> future = new CompletableFuture<String>();
		HttpServer.getResults().put(uuid, future);
		result.sampleStart();
		try {
			
			LOG.info("uuid=" + uuid);
			
			
			
			CompletableFuture<String> future1 = HttpServer.getResults().get(uuid);
			String status = future1.get(1, TimeUnit.MINUTES);

			if (status.equals(uuid)) {
				result.setSuccessful(true);
			}
			else {
				result.setSuccessful(false);
				result.setResponseMessage("invalid status " + status);
			}
				
		} catch (Exception e) {
			LOG.error("Exception on " + uuid, e);
			result.setSuccessful(false);
			result.setResponseMessage(e.getMessage());
		} finally {
			result.sampleEnd(); // stop stopwatch
		}
		

		return result;
	}


}
