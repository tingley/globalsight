package com.adobe.cq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.scheduler.Scheduler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Just a simple DS Component
 */
@Component(metatype = true)
@Service
public class SimpleDSComponent implements Runnable {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private BundleContext bundleContext;

	private static final String upFileUrl = "http://localhost:4502/bin/upfile";
	final Set<JobBean> globalsightJobs = CreateJobServlet.jobNames;

	@Reference
	private Scheduler scheduler;

	public void run() {
	}

	protected void activate(ComponentContext ctx) {
		
		this.bundleContext = ctx.getBundleContext();
		String minuter = this.getCheckInterval();
		
		if (minuter == null || minuter.isEmpty()) {
			minuter = "20"; //default
		}
		logger.info("set the check interval value:	" + minuter + " minuters.");
		logger.info("globalsightJobs size:	" + globalsightJobs.size());
//		String schedulingExpression = "* 0/"+ minuter + " * * * ?";	
		final Set<JobBean> jobsFromAEM = getInProgressJob();
		
		if (jobsFromAEM != null && !jobsFromAEM.isEmpty()) {
			globalsightJobs.addAll(jobsFromAEM);
		}
		
		final Runnable job = new Runnable() {
			public void run() {
				logger.info("******************This is AEM Scheduler Running************************");
				
				//clean memory
				if (jobsFromAEM != null && !jobsFromAEM.isEmpty()){
					for (JobBean jb : jobsFromAEM) {
						if (jb.getStatus().equals(AEMConstants.Finished)) {
							jobsFromAEM.remove(jb);
						}
					}
				}
				
				if (globalsightJobs != null && globalsightJobs.size() != 0) {

					Iterator<JobBean> iterator = globalsightJobs.iterator();
					
					while (iterator.hasNext()) {
						JobBean job = iterator.next();
						String jobName = job.getJobName();
						
						logger.info("Begin to query job: " + jobName);
						String xmlResult = checkStatusJob(jobName);
						logger.info("Job status xmlResult: \n " + xmlResult);

						if (xmlResult == null) {
							continue;
						}
						
						StringReader read = new StringReader(xmlResult);
						InputSource source = new InputSource(read);
						SAXReader saxReader = new SAXReader();
						try {
							Document document = saxReader.read(source);
							Node statusNode = document
									.selectSingleNode("/job/status");
							String status = statusNode.getText();
							logger.info("Job status:	" + status);

							if ("EXPORTED".equalsIgnoreCase(status)
									|| "ARCHIVED".equalsIgnoreCase(status)) {
								importJobFile(jobName);
								job.setStatus(AEMConstants.Finished);
								updateJobStatus(job);
								iterator.remove();
								// postEmail(job, null);
							} else if ("UNKNOWN".equalsIgnoreCase(status)){
								job.setStatus(AEMConstants.Ignored);
								updateJobStatus(job);
								iterator.remove();
							}
						} catch (Exception e) {
							logger.error("Error: ", e);
						}

					}
				} else {
					logger.info("Right now no committed job!");
				}
				
			}
		};
		
		//this line call shutDownHook

		try {
			/**
			 * this.scheduler.addJob("myJob", job, null, schedulingExpression,true);
			 * */
			long period = Integer.parseInt(minuter) * 60;
			this.scheduler.addPeriodicJob("GlobalSight Job", job, null, period, true);
		} catch (Exception ex) {
			job.run();
		}

	}
	
	private String getCheckInterval() {
		return getCustomerService().getCheckInterval();
	}

	private void updateJobStatus(JobBean job) {
		getCustomerService().updateJobStatus(job);
	}
	
	private Set<JobBean> getInProgressJob() {
		return getCustomerService().getInProgressJob();
	}

//	private void shutDownHook(final Set<JobBean> jobs) {
//		logger.info("Added ShutDown Hook.");
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			public void run() {
//				logger.info("Running shutdown hook before JVM complete closed.");
//				getCustomerService().persisJob(jobs);
//			}
//		});
//	}
	
//	private void persistJob(final Set<JobBean> jobs) {
//		logger.info("Running persist job before this bundle deacitived.");
//		getCustomerService().persisJob(jobs);
//	}

	protected void postEmail(JobBean job) {
		if (job == null) {
			return;
		}

		String jobName = job.getJobName();
		String target = job.getTarget();

		logger.info("Done message: job " + jobName + " translated into "
				+ target + " is completed.");

		try {
			// MessageGateway<Email> messageGateway;
			// Email email = new SimpleEmail();
			// String emailToRecipients = "brian.lei@welocalize.com";
			// email.addTo(emailToRecipients);
			//
			// email.setSubject("Job Done Message");
			// email.setFrom("test@adobe.com");
			// email.setMsg("This message is to inform you that the job \'" +
			// jobName + "\' translated into language " + target +
			// " has been completed!");
			//
			// messageGateway = messageGatewayService.getGateway(Email.class);
			// messageGateway.send((Email) email);
		} catch (Exception ex) {
			logger.error("Error post email: " + ex);
		}
	}

	protected void postEmail(JobBean job, String str) {
		if (job == null) {
			return;
		}

		String jobName = job.getJobName();
		String target = job.getTarget();

		logger.info("Done message: job " + jobName + " translated into "
				+ target + " is completed.");

		try {
			String aHost = "E2K7MS01.welocalize.com";
			String toAddr = "brian.lei@welocalize.com";
			String aSubject = "Job Done Message";
			String aMessage = "This message is to inform you that the job \'"
					+ jobName + "\' translated into language " + target
					+ " has been completed!";
			String from = "brian.lei@welocalize.com";
			Properties properties = System.getProperties();

			properties.setProperty("mail.smtp.host", aHost);
			properties
					.setProperty("mail.smtp.user", "brian.lei@welocalize.com");
			Session session = Session.getDefaultInstance(properties);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toAddr));
			message.setSubject(aSubject);
			message.setText(aMessage);
			Transport.send(message);
			logger.info("Sent message successfully....");
		} catch (Exception ex) {
			logger.error("Error:", ex);
		}
	}

	private void importJobFile(String jobName) throws IOException,
			DocumentException {
		String xmlResult = this.getCustomerService().getJobExportFile(jobName);
		
		if (xmlResult == null) {
			return;
		}
		
		logger.info("Get finished Job result: \n " + xmlResult);
		
		if (xmlResult != null) {
			StringReader read = new StringReader(xmlResult);
			InputSource source = new InputSource(read);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(source);
			String root = document.selectSingleNode("/jobFiles/root").getText();
			
			//multiple translated files
//			String path = document.selectSingleNode("/jobFiles/paths")
//					.getText();
			List<Node> pathList = document.selectNodes("/jobFiles/paths");
			
			for (Node pathNode : pathList) {
				String path = pathNode.getText();
				String target = path.substring(0, path.indexOf("/"));
				String fileName = path.substring(path.lastIndexOf("/") + 1);
				StringBuffer downloadURL = new StringBuffer();
				downloadURL.append(root);
				downloadURL.append("/");
				downloadURL.append(path);
				logger.info("----------- Download Url:");
				logger.info(downloadURL.toString());

				String urlDecode = URLDecoder.decode(downloadURL.toString(),
						"UTF-8").replaceAll("\\\\", "/");
				urlDecode = urlDecode.replace(" ", "%20");

				System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
				System.setProperty("sun.net.client.defaultReadTimeout", "10000");
				URL url = new URL(urlDecode);
				HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
				hurl.connect();
				InputStream is = hurl.getInputStream();
				File localeFolder = new File(AEMConstants.GlobalSightTargetFile + File.separator + target);
				
				if (!localeFolder.exists()) {
					localeFolder.mkdirs();
				}

				File file = new File(localeFolder, fileName);
				saveFile(is, file);

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(upFileUrl);
				StringBody body = new StringBody(file.getAbsolutePath());
				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("filePathName", body);
				httppost.setEntity(reqEntity);
				httppost.setHeader(null);
				HttpResponse response = httpclient.execute(httppost);
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == HttpStatus.SC_OK) {
					logger.info("HttpClient post file name successfully!");
					HttpEntity resEntity = response.getEntity();
					logger.info(EntityUtils.toString(resEntity));
					logger.info(resEntity.getContent().toString());
					EntityUtils.consume(resEntity);
				}

				httpclient.getConnectionManager().shutdown();
			}
			
		}
	}

	protected String checkStatusJob(String jobName) {
		return this.getCustomerService().getJobStatus(jobName);
	}

	protected void deactivate(ComponentContext ctx) {
//		try {
//			if (globalsightJobs != null && !globalsightJobs.isEmpty()) {
//				persistJob(globalsightJobs);
//			} 
//		} catch (Exception ex){
//			logger.error("Error: ",  ex);
//		}
		
		this.bundleContext = null;
	}

	private CustomerService getCustomerService() {
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CustomerService.class.getName());
		return CustomerService.class.cast(ctx.getService(serviceReference));
	}

	private void saveFile(InputStream is, File file) throws IOException,
			FileNotFoundException {
		file.createNewFile();

		BufferedReader in = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));

		PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));

		String str;
		while ((str = in.readLine()) != null) {
			out.print(str);
		}

		out.flush();
		out.close();
		is.close();
	}

	
	public static void main(String[] args) {
//		String path = "it_IT/webservice/2048/sourceFile/201503251239-804579669/it_IT/geometrixx.en.products.square.overview.xml";
//		System.out.println(path.subSequence(0, path.indexOf("/")));
//	
//		File targetFolder = new File("d:" + File.separator + "targetFile" + File.separator + "it");
//		if (!targetFolder.exists()) {
//			targetFolder.mkdirs();
//		}
//
//		File file = new File(targetFolder, "test1.xml");
//		try {
//			file.createNewFile();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//	    Set<String> set = new HashSet<String>();  
//	    set.add("aaaaaa");  
//	    set.add("bbbbbb");  
//	    set.add("cccccc");  
//	    set.add("dddddd");  
//	    set.add("eeeeee");  
//	    set.add("ffffff");  
//	    set.add("gggggg");  
//	    Iterator<String> it = set.iterator();  
//	    System.out.println(set.size());
//	    
//	    while (it.hasNext()) {  
//	        String str = it.next();  
//	        if ("dddddd".equals(str)) {
//	        	System.out.println("remove dddddd");
//	        	System.out.println(set.size());
//	            it.remove();  
//	            System.out.println(set.size());
//	        }  
//	    }  
//	    System.out.println("------------");
//	    for (String str : set) {  
//	        System.out.println(str);  
//	    }  
		
		
	}
}