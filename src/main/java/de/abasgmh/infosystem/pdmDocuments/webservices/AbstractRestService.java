package de.abasgmh.infosystem.pdmDocuments.webservices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import de.abasgmh.infosystem.pdmDocuments.DocumentsInterface;
import de.abasgmh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmh.infosystem.pdmDocuments.utils.Util;

public abstract class AbstractRestService implements DocumentsInterface {

	protected final static Logger log = Logger.getLogger(AbstractRestService.class);

	protected String server;
	protected String pdmProductID;
	protected String pdmDocumentTyp;
	protected String user;
	protected String password;

	@Override
	public void setPasword(String password) {
		this.password = password;

	}

	@Override
	public void setUser(String user) {
		this.user = user;

	}

	@Override
	public void setServer(String serveradresse) {
		this.server = serveradresse;

	}

	@Override
	public void setProduct(String pdmProductID) {
		this.pdmProductID = pdmProductID;

	}

	@Override
	public void setDocumentTyp(String pdmDocumentTyp) {
		this.pdmDocumentTyp = pdmDocumentTyp;

	}

	protected String callRestservice(String url) throws PdmDocumentsException {

		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(url);
		target.register(new BasicAuthentication(this.user, this.password));

		Response response = target.request().get();
		if (response.getStatus() == 200) {
			log.info("ok - " + url);
			
			String value;
			try {
				value = response.readEntity(String.class);
				log.debug("Response : " + value);
			} finally {
				response.close();
			}
			return value;
		} else {
			log.error(url + " " + response.getStatus() + " " + response.getMetadata().toString());
			throw new PdmDocumentsException(
					Util.getMessage("pdmDocument.restservice.keytech.error.getfilehttprequest", response.getStatus() + ";" + response.getMetadata().toString()));
		}

	}

	protected List<File> downloadFileFromRestservice(String url, String fileName, String path)
			throws IOException, FileNotFoundException, PdmDocumentsException {

		List<File> fileList = new ArrayList<File>();

		ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();

		ResteasyClient client = null;

		ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder().providerFactory(factory);

		client = resteasyClientBuilder.build();

		ResteasyWebTarget target = client.target(url);
		target.register(new BasicAuthentication(this.user, this.password));

		InputStream inputStream = target.request().get(InputStream.class);

		File outputfile = new File(path + fileName);
		OutputStream out = new FileOutputStream(outputfile);

		int read = 0;
		byte[] bytes = new byte[2048];
		while ((read = inputStream.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		inputStream.close();
		out.flush();
		out.close();
		fileList.add(outputfile);

		return fileList;

	}

}
