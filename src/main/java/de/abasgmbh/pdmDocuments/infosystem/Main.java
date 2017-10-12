package de.abasgmbh.pdmDocuments.infosystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.abas.eks.jfop.annotation.Stateful;
import de.abas.eks.jfop.remote.FO;
import de.abas.erp.api.session.GUIInformation;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.Id;
import de.abas.erp.common.type.enums.EnumPrinterType;
import de.abas.erp.db.ContextManager;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.EditorObject;
import de.abas.erp.db.Query;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments.Row;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM;
import de.abas.erp.db.schema.infrastructure.Printer;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.printparameter.PrintDialogEditor;
import de.abas.erp.db.schema.purchasing.PurchaseOrder;
import de.abas.erp.db.schema.purchasing.Purchasing;
import de.abas.erp.db.schema.sales.Sales;
import de.abas.erp.db.schema.sales.SalesOrder;
import de.abas.erp.db.schema.sales.WebOrder;
import de.abas.erp.db.schema.userenums.UserEnumPdmSystems;
import de.abas.erp.db.schema.workorder.WorkOrders;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;
import de.abas.erp.db.util.ContextHelper;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.PrintBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abasgmbh.pdmDocuments.infosystem.config.Configuration;
import de.abasgmbh.pdmDocuments.infosystem.config.ConfigurationHandler;
import de.abasgmbh.pdmDocuments.infosystem.data.PdmDocument;
import de.abasgmbh.pdmDocuments.infosystem.utils.Util;

@Stateful
@EventHandler(head = PdmDocuments.class, row = PdmDocuments.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class Main {
	protected final static Logger log = Logger.getLogger(Main.class);
	protected final static String SQL_DRIVER_DEFAULT = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	
	
	

	private Configuration config = Configuration.getInstance();
	
	@ButtonEventHandler(field = "start", type = ButtonEventType.AFTER, table = false)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head)
			throws EventException {
		
		getConfigInMask(head, ctx);
		
//		getAttachmentFileList(head);
				
		log.info(Util.getMessage("pdmDocument.info.startvalues", head.getYartikel() , head.getYbeleg() , head.getYdrucker() , head.getYanhangliste()));

		if (head.getYdrucker() != null) {
			head.setYistdrucker(isRealPrinter(head.getYdrucker()));
			head.setYistemail(isEmailPrinter(head.getYdrucker()));
		}else {
			head.setYistdrucker(false);
			head.setYistemail(false);
		}
		
		if (!(head.getYistdrucker() && head.getYistemail())) {
			head.setYistbildschirm(true);
		}
		
		if (head.getYartikel() != null || head.getYbeleg() != null) {
			
			head.table().clear();
			loadProductsInTable(head, ctx);
			DocumentSearchfactory documentSearchfactory = new DocumentSearchfactory();
			
			try {
				DocumentsInterface searchdokuments = documentSearchfactory.create(config);

				FileWriter fileWriter;
				File tempFile;
				tempFile = gettempFile(head);

				head.setYanhangliste(tempFile.getAbsolutePath());
				fileWriter = new FileWriter(tempFile);

				if (searchdokuments != null) {

					Iterable<Row> rows = head.table().getRows();
					for (Row row : rows) {
						if (row.getYtartikel() != null) {
							insertDocuments(row.getYtartikel().getIdno(), searchdokuments, head, ctx , row);
						}
					}
					// Übergabe Dateien für den Druck anlegen.
					rows = head.table().getRows();
					for (Row row : rows) {
						if (!row.getYpfad().isEmpty()) {
							// Kopieren für FOPMULTI in das Output-Verzeichnis
							
							Printer printer = head.getYdrucker();
							if (printer != null ) {
								if (isEmailPrinter(printer)) {
									// In die Dateianhangliste eintragen
									fileWriter.append(row.getYpfad() + System.getProperty("line.separator"));
								} else {
//									copyFile(row, head);
//									wird im Multi-FOP Layout gelöst
								}
							}
							
						}

					}
					fileWriter.flush();
					fileWriter.close();

				} else {
					Util.showErrorBox(ctx, Util.getMessage("main.error.noConnection", head.getYserver()));
				}
			} catch (PdmDocumentsException | IOException e) {
				Util.showErrorBox(ctx, e.getMessage());
			}
		} else {
			Util.showErrorBox(ctx, Util.getMessage("main.error.noProduct"));
		}

	}

	private boolean isEmailPrinter(Printer printer) {
		
		EnumPrinterType printertyp = printer.getPrinterType();
		if (printertyp == EnumPrinterType.EmailClientSend
				|| printertyp == EnumPrinterType.EmailClientView) {
			log.info(Util.getMessage("pdmDocument.info.is.emailprinter", printer.getSearchExt()));
			return true;
		}else {
			log.info(Util.getMessage("pdmDocument.info.is.no.emailprinter", printer.getSearchExt()));
			return false;	
		}
		
		
	}
	
private boolean isRealPrinter(Printer printer) {
		
		EnumPrinterType printertyp = printer.getPrinterType();
		if (printertyp == EnumPrinterType.Printer || printertyp == EnumPrinterType.Terminal
				|| printertyp == EnumPrinterType.StandardWorkStationPrinter
				|| printertyp == EnumPrinterType.LocalPrinter) {
			log.info(Util.getMessage("pdmDocument.info.is.realprinter", printer.getSearchExt()));
			return true;
		}else {
			log.info(Util.getMessage("pdmDocument.info.is.no.realprinter", printer.getSearchExt()));
			return false;	
		}
		
		
	}
	
	
	

	private File gettempFile(PdmDocuments head) throws IOException {
		File tmpverz = new File("rmtmp");
		File tempFile;
		
		String emailAttachmentFile = head.getYanhangliste();
		
		if (emailAttachmentFile.isEmpty()) {
			tempFile = File.createTempFile("pdmDoc", ".TMP", tmpverz);
			
			return tempFile;
		} else {
			tempFile = new File(emailAttachmentFile);
			if (!tempFile.exists()) {
				tempFile.createNewFile();
			}
			return tempFile;
		}

	}
	
	

//	private void copyFile(Row row, PdmDocuments head) throws PdmDocumentsException {
//		
//		String actTempDir = getActTempDir(head);
//		if (!actTempDir.isEmpty()) {
//			String outputTempDir = actTempDir + "/output/";
//			File outputDir = new File(outputTempDir + row.getYdateiname());
//			File file = new File(row.getYpfad());
//			try {
//				log.info(Util.getMessage("pdmdocuments.log.copyfile", file.toPath().toString() , outputDir.toPath().toString()));
//				Files.copy(file.toPath(), outputDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
//			} catch (IOException e) {
//				throw new PdmDocumentsException("main.error.copyFile", e);
//			}
//		}
//
//	}

	private  String getAttachmentFileList(PdmDocuments head) {
		BufferFactory buff = BufferFactory.newInstance(true);
		UserTextBuffer utextBuff = buff.getUserTextBuffer();
		utextBuff.defineVar("text" , "xttest");		
		FO.formel("U|xttest = P|diadrucker^id");
//		FO.formel("U|xttest = \"test\"");
		String testString = utextBuff.getStringValue("xttest");
		FO.eingabe("TESTFOP");
		PrintDialogEditor printedit = null;
		EditorObject test= null;
		try {
			test =  head.invokePrint();
//			head.invokeButton("print");
			if (test != null ) {
				printedit = (PrintDialogEditor) test;
				return printedit.getAttachmentFileList();
			}
			return "";
		} catch (CommandException e1) {
			log.error(e1);
		} finally {
			if (test != null) {
				if (test.active()) {
					test.abort();
				} 
			}
		}
		return null;
	}
	
//	private  String getActTempDir(PdmDocuments head) {
//		PrintDialogEditor test = null;
//		try {
//			test = (PrintDialogEditor) head.invokePrint();
//			return test.getActTempDir();
//		} catch (CommandException e1) {
//			log.error(e1);
//		} finally {
//			if (test != null) {
//				if (test.active()) {
//					test.abort();
//				} 
//			}
//		}
//		return null;
//	}
	
	private   Printer getactPrinter(PdmDocuments head, DbContext ctx) {
		
			PrintBuffer printBuffer = getPrintBuffer();
			
			Id printerId = printBuffer.getId("actPrinter");

			if (printerId.isNullRef()) {
				Printer printer = head.getYdrucker();
				return printer;
			}else {
				String criteria = "id=" + printerId.toString();
				Selection<Printer> selection = ExpertSelection.create(Printer.class, criteria);
				Query<Printer> querySales = ctx.createQuery(selection);
				List<Printer> printers = querySales.execute();
					for (Printer printer2 : printers) {
						return printer2;
					} 
			}
			return null;

			
		
	}
	
	

	private PrintBuffer getPrintBuffer() {
		BufferFactory bufferFact = BufferFactory.newInstance(true);
		return bufferFact.getPrintBuffer();
	}

	private void insertDocuments(String product, DocumentsInterface searchdokuments, PdmDocuments head, DbContext ctx, Row row)
			throws PdmDocumentsException, IOException {

		ArrayList<PdmDocument> pdmDocuments = searchdokuments.getAllDocuments(product);
		int rowIndex = row.getRowNo();
		for (PdmDocument pdmDocument : pdmDocuments) {
			rowIndex = rowIndex + 1;
			if (checkdocuments(head, pdmDocument, ctx)) {
				
				Row rowNew;
				if (rowIndex <= head.table().getRowCount()) {
					rowNew = head.table().insertRow(rowIndex);
				}else {
					rowNew = head.table().appendRow();
				}
				rowNew.setYdateiname(pdmDocument.getFilename());
				rowNew.setYdatend(pdmDocument.getFiletyp());
				rowNew.setYpfad(pdmDocument.getFile().getCanonicalPath().toString());
				rowNew.setYdoktyp(pdmDocument.getDocumenttyp());
				
			}
		}
	}

	private boolean checkdocuments(PdmDocuments head, PdmDocument pdmDocument, DbContext ctx) {

		String drucktypen = head.getYdrucktypen();
		String emailtypen = head.getYemailtypen();
		String bildschirmtypen = head.getYbildschirmtypen();
		
		String[] drucktyplist = drucktypen.split(",");
		String[] emailtyplist = emailtypen.split(",");
		String[] bildschirmtyplist = bildschirmtypen.split(",");
		
		

		Printer printer = getactPrinter(head, ctx);

		if (printer == null) {
			printer = head.getYdrucker();
			if (printer != null) {
				return checkPrinter(head, pdmDocument, drucktyplist, emailtyplist,bildschirmtyplist, printer);
			}else {
				if(checkDocumentString(pdmDocument, bildschirmtyplist)
				&& checkDocumenttyp(pdmDocument, head.getYdokart())){
					return true;
				}else return false;
			}
		}else {
			return checkPrinter(head, pdmDocument, drucktyplist, emailtyplist,bildschirmtyplist, printer);		 
		}

	}

	private Boolean checkPrinter(PdmDocuments head, PdmDocument pdmDocument, String[] drucktyplist, String[] emailtyplist, String[] bildschirmtyplist,
			Printer printer2) {
//		EnumPrinterType printertyp = printer2.getPrinterType();
		
		if (isRealPrinter(printer2)) {

			if (checkDocumentString(pdmDocument, drucktyplist)
					&& checkDocumenttyp(pdmDocument, head.getYdokart())) {

				return true;
			} else {
				return false;
			}

		} else if (isEmailPrinter(printer2)) {

			if (checkDocumentString(pdmDocument, emailtyplist)
					&& checkDocumenttyp(pdmDocument, head.getYdokart())) {
				
				return true;
			} else {
				return false;
			}
		} else {
			if (checkDocumentString(pdmDocument, bildschirmtyplist)
					&& checkDocumenttyp(pdmDocument, head.getYdokart())) {

				return true;
			} else {
				return false;
			}
		}
	
	}

	private boolean checkDocumenttyp(PdmDocument pdmDocument, String documentart) {

		if (!documentart.isEmpty()) {
			if (pdmDocument.getDocumenttyp().equals(documentart)) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	private boolean checkDocumentString(PdmDocument pdmDocument, String[] typlist) {

		String filetyp = pdmDocument.getFiletyp();
		Boolean allempty = true;
//		boolean test = typlist.toString().isEmpty();
//		int anz = typlist.length;

		if (!typlist.toString().isEmpty()) {
			for (String typ : typlist) {
				if (!typ.isEmpty()) {
					if (typ.trim().toUpperCase().equals(filetyp.toUpperCase())) {
						log.trace(Util.getMessage("pdmDocument.checkdocument.includePdmDoc", pdmDocument.getFilename() , Arrays.toString(typlist)));
						return true;
					}
					allempty = false;
				}
			}
		}

		if (allempty) {
			log.trace(Util.getMessage("pdmDocument.checkdocument.includePdmDoc.emptyTypliste", pdmDocument.getFilename() ));
			return true;
		} else {
			log.trace(Util.getMessage("pdmDocument.checkdocument.excludePdmDoc", pdmDocument.getFilename() , Arrays.toString(typlist)));
			return false;
		}
	}

	@ButtonEventHandler(field = "ysaveconfig", type = ButtonEventType.AFTER)
	public void ysaveconfigAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head)
			throws EventException {
		
			Configuration config = Configuration.getInstance();
			try {
				config.setRestServer(head.getYserver(), head.getYuser() , head.getYpassword(), head.getYtenant());
				config.setSqlConnection(head.getYsqlserver(), head.getYsqlport(), head.getYdatabase() , head.getYsqluser(), head.getYsqlpassword(), head.getYsqldriver());
		        config.setFiletypes(head.getYemailtypen(), head.getYdrucktypen(), head.getYbildschirmtypen());
		        config.setPdmSystem(head.getYpdmsystem());
		        config.setPartFieldName(head.getYfieldfornumber());
		        config.setPartProFileIDFieldName(head.getYfieldforpartid());
				ConfigurationHandler.saveConfigurationtoFile(config);
			} catch (PdmDocumentsException e) {
				log.error(e);
				Util.showErrorBox(ctx, Util.getMessage("main.saveconfiguration.error"));
				
			}
		
	}

	@ButtonEventHandler(field = "ybuanzeigen", type = ButtonEventType.AFTER, table = true)
	public void ybuanzeigenAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head,
			PdmDocuments.Row currentRow) throws EventException {

		
		
		
		 GUIInformation gui = new GUIInformation(ctx);
		 File clientDir = gui.getClientTempDir();
//				gP.getStringValue("cltempdir");
		
		String zieldatvalue = clientDir.getPath() + "\\" + currentRow.getYdateiname();
		String valuecmd = " -PC -BIN " + currentRow.getYpfad() + " " + zieldatvalue;
		FO.pc_copy(valuecmd);
		FO.pc_open(zieldatvalue);

	}
	
	
	
	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head)
			throws EventException {
		getConfigInMask(head, ctx);
		showConfiguration(ctx);		
	}

	private void showConfiguration(DbContext ctx) {
		BufferFactory buff = BufferFactory.newInstance();
		PrintBuffer printbuf = buff.getPrintBuffer();
		
		if (config.getPdmSystem() != null) {
			
			if (config.getPdmSystem() == UserEnumPdmSystems.PROFILE) {
				String maskkont = printbuf.getStringValue("maskkontextfop");
				String newMaskkontext = UserEnumPdmSystems.PROFILE.name().toString().toUpperCase(); 
				printbuf.assign("maskkontextfop" , newMaskkontext);
			}else if (config.getPdmSystem() == UserEnumPdmSystems.KEYTECH) {
				printbuf.assign("maskkontextfop" , UserEnumPdmSystems.KEYTECH.name().toString().toUpperCase());
			}
			
		}else {
			
			UserEnumPdmSystems[] pdmSystemValues = UserEnumPdmSystems.values();
			for (UserEnumPdmSystems userEnumPdmSystems : pdmSystemValues) {
				
				if (userEnumPdmSystems != null) {
					String maskkont = printbuf.getStringValue("maskkontextfop");
					String newMaskkontext = maskkont + " " + userEnumPdmSystems.name().toString().toUpperCase();
					printbuf.assign("maskkontextfop", newMaskkontext);
				}
			}
		}
	}
	

	private void getConfigInMask(PdmDocuments head, DbContext ctx) {

		try {
			Configuration config = ConfigurationHandler.loadConfiguration();
			
			head.setYserver(config.getRestServer());
			head.setYuser(config.getRestUser());
			head.setYpassword(config.getRestPassword());
			head.setYtenant(config.getRestTenant());
			
			head.setYsqlserver(config.getSqlServer());
			head.setYsqlport(config.getSqlPort());
			head.setYdatabase(config.getSqldatabase());
			head.setYsqluser(config.getSqlUser());
			head.setYsqlpassword(config.getSqlPassword());
			head.setYsqldriver(config.getSqlDriver());
			
//			Vorbelegung für SQL-Server falls noch nicht gespeichert
			if (head.getYsqldriver().isEmpty()) {
				head.setYsqldriver(Main.SQL_DRIVER_DEFAULT);
				config.setSqlDriver(Main.SQL_DRIVER_DEFAULT);
			}

			head.setYpdmsystem(config.getPdmSystem());
			
			head.setYbildschirmtypen(config.getFileTypesScreen());
			head.setYdrucktypen(config.getFileTypesPrinter());
			head.setYemailtypen(config.getFileTypesEmail());
			head.setYfieldfornumber(config.getPartFieldName());
			head.setYfieldforpartid(config.getPartProFileIDFieldName());
			
		} catch (PdmDocumentsException e) {
			
			Util.showErrorBox(ctx, Util.getMessage("pdmDocument.error.loadKonfiguration") + "/n" + e.getMessage());
		}
		
	}

	private void loadProductsInTable(PdmDocuments head, DbContext ctx) {

		if (head.getYstruktur()) {
			insertProductInRow(head.getYartikel(), head);
			ContextManager contextmanager = ContextHelper.buildContextManager();
			DbContext dbcontext = contextmanager.getServerContext();
			MultiLevelBOM mlb = dbcontext.openInfosystem(MultiLevelBOM.class);
			mlb.setArtikel(head.getYartikel());
			mlb.setCountLevels(head.getYstufe());
			mlb.setBmitag(false);
			mlb.setBmitfm(false);
			mlb.invokeStart();
			Iterable<de.abas.erp.db.infosystem.standard.st.MultiLevelBOM.Row> mlbRows = mlb.table().getRows();
			for (de.abas.erp.db.infosystem.standard.st.MultiLevelBOM.Row row : mlbRows) {
				int treeLevel = row.getTreeLevel();
				SelectableObject selProduct = row.getElem();
				if (selProduct instanceof Product) {
					Product product = (Product) selProduct;
					insertProductInRow(product, treeLevel, head);
				}
			}
			mlb.close();

		} else {

			if (head.getYbeleg() == null) {
				insertProductInRow(head.getYartikel(), head);
			} else {
				SelectableObject beleg = head.getYbeleg();
				ArrayList<Product> listProduct = getProducts(beleg , ctx);
				for (Product product : listProduct) {
					insertProductInRow(product, head);
				}

			}
		}

	}

	private ArrayList<Product> getProducts(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product> listProduct = new ArrayList<Product>();
		
		
		if (beleg instanceof Sales) {
			
			listProduct = productsfromSalesBlanketOrder(beleg, ctx);
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesQuotation(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesOrder(beleg, ctx);
			}

			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesInvoice(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesInvoice(beleg, ctx);
			}
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesWebOrder(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesServiceQuotation(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesServiceOrder(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesRepairOrder(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesOpportunity(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromSalesCostEstimate(beleg, ctx);
			}

			
			
			
			
			
		} else if (beleg instanceof Purchasing) {
			
			
			listProduct = productsfromPurchaseBlanketOrder(beleg, ctx);
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromPurchaseOrder(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromPurchaseOrder(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromRequest(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromPurchasePackingSlip(beleg, ctx);
			}
			
			if (listProduct.isEmpty()) {
				listProduct = productsfromPurchaseInvoice(beleg, ctx);
			}

			

		} else if (beleg instanceof WorkOrders) {
			WorkOrders sbeleg = (WorkOrders) beleg;
			Product product = getProduct(sbeleg.getProduct());
			if (product != null) {
				listProduct.add(product);
			}

		}

		return listProduct;
	}

	private ArrayList<Product> productsfromPurchaseOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
		String criteria = "id=" + beleg.getId().toString() + ";@filingmode=(Both)";
		Selection<PurchaseOrder> select = ExpertSelection.create( PurchaseOrder.class, criteria  );
		Query<PurchaseOrder> queryPurchasing = ctx.createQuery(select);
		for (PurchaseOrder purchasing : queryPurchasing) {
			Iterable<de.abas.erp.db.schema.purchasing.PurchaseOrder.Row> rows = purchasing.table().getRows();
			for (de.abas.erp.db.schema.purchasing.PurchaseOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	  }
	
	private ArrayList<Product> productsfromSalesOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
		String criteria = "id=" + beleg.getId().toString();
		Selection<SalesOrder> select = ExpertSelection.create( SalesOrder.class, criteria  );
		Query<SalesOrder> querysales = ctx.createQuery(select);
		for (SalesOrder sales : querysales) {
			Iterable<SalesOrder.Row> rows = sales.table().getRows();
			for (SalesOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	  }
	
	private ArrayList<Product> productsfromSalesWebOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
		String criteria = "id=" + beleg.getId().toString();
		Selection<WebOrder> select = ExpertSelection.create( WebOrder.class, criteria  );
		Query<WebOrder> querysales = ctx.createQuery(select);
		for (WebOrder sales : querysales) {
			Iterable<WebOrder.Row> rows = sales.table().getRows();
			for (WebOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	  }
	
	private ArrayList<Product> productsfromPurchaseBlanketOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString() ;
		Selection<de.abas.erp.db.schema.purchasing.BlanketOrder> select = ExpertSelection.create( de.abas.erp.db.schema.purchasing.BlanketOrder.class, criteria  );
		Query<de.abas.erp.db.schema.purchasing.BlanketOrder> queryPurchasing = ctx.createQuery(select);
		for (de.abas.erp.db.schema.purchasing.BlanketOrder purchasing : queryPurchasing) {
			Iterable<de.abas.erp.db.schema.purchasing.BlanketOrder.Row> rows = purchasing.getTableRows();
			
			for (de.abas.erp.db.schema.purchasing.BlanketOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesQuotation(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.Quotation> select = ExpertSelection.create( de.abas.erp.db.schema.sales.Quotation.class, criteria  );
		Query<de.abas.erp.db.schema.sales.Quotation> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.Quotation sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.Quotation.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.Quotation.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesCostEstimate(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.CostEstimate> select = ExpertSelection.create( de.abas.erp.db.schema.sales.CostEstimate.class, criteria  );
		Query<de.abas.erp.db.schema.sales.CostEstimate> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.CostEstimate sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.CostEstimate.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.CostEstimate.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesOpportunity(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.Opportunity> select = ExpertSelection.create( de.abas.erp.db.schema.sales.Opportunity.class, criteria  );
		Query<de.abas.erp.db.schema.sales.Opportunity> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.Opportunity sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.Opportunity.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.Opportunity.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesServiceQuotation(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.ServiceQuotation> select = ExpertSelection.create( de.abas.erp.db.schema.sales.ServiceQuotation.class, criteria  );
		Query<de.abas.erp.db.schema.sales.ServiceQuotation> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.ServiceQuotation sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.ServiceQuotation.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.ServiceQuotation.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesServiceOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.ServiceOrder> select = ExpertSelection.create( de.abas.erp.db.schema.sales.ServiceOrder.class, criteria  );
		Query<de.abas.erp.db.schema.sales.ServiceOrder> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.ServiceOrder sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.ServiceOrder.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.ServiceOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesRepairOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.RepairOrder> select = ExpertSelection.create( de.abas.erp.db.schema.sales.RepairOrder.class, criteria  );
		Query<de.abas.erp.db.schema.sales.RepairOrder> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.RepairOrder sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.RepairOrder.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.RepairOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesBlanketOrder(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.BlanketOrder> select = ExpertSelection.create( de.abas.erp.db.schema.sales.BlanketOrder.class, criteria  );
		Query<de.abas.erp.db.schema.sales.BlanketOrder> querySales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.BlanketOrder sales : querySales) {
			Iterable<de.abas.erp.db.schema.sales.BlanketOrder.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.BlanketOrder.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromPurchasePackingSlip(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.purchasing.PackingSlip> select = ExpertSelection.create( de.abas.erp.db.schema.purchasing.PackingSlip.class, criteria  );
		Query<de.abas.erp.db.schema.purchasing.PackingSlip> queryPurchasing = ctx.createQuery(select);
		for (de.abas.erp.db.schema.purchasing.PackingSlip purchasing : queryPurchasing) {
			Iterable<de.abas.erp.db.schema.purchasing.PackingSlip.Row> rows = purchasing.getTableRows();
			
			for (de.abas.erp.db.schema.purchasing.PackingSlip.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
//	private ArrayList<Product> productsfromSalesPackingSlip(SelectableObject beleg, DbContext ctx) {
//		ArrayList<Product>listProduct = new ArrayList<Product>();
//	
//		String criteria = "id=" + beleg.getId().toString();
//		Selection<de.abas.erp.db.schema.sales.PackingSlip> select = ExpertSelection.create( de.abas.erp.db.schema.sales.PackingSlip.class, criteria  );
//		Query<de.abas.erp.db.schema.sales.PackingSlip> querysales = ctx.createQuery(select);
//		for (de.abas.erp.db.schema.sales.PackingSlip sales : querysales) {
//			Iterable<de.abas.erp.db.schema.sales.PackingSlip.Row> rows = sales.getTableRows();
//			
//			for (de.abas.erp.db.schema.sales.PackingSlip.Row row : rows) {
//				SelectablePart selproduct = row.getProduct();
//				Product product = getProduct(selproduct);
//				if (product != null) {
//					listProduct.add(product);
//				}
//			}
//		
//		}
//		return listProduct;
//	}
	
	private ArrayList<Product> productsfromRequest(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.purchasing.Request> select = ExpertSelection.create( de.abas.erp.db.schema.purchasing.Request.class, criteria  );
		Query<de.abas.erp.db.schema.purchasing.Request> queryPurchasing = ctx.createQuery(select);
		for (de.abas.erp.db.schema.purchasing.Request purchasing : queryPurchasing) {
			Iterable<de.abas.erp.db.schema.purchasing.Request.Row> rows = purchasing.getTableRows();
			
			for (de.abas.erp.db.schema.purchasing.Request.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromPurchaseInvoice(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.purchasing.Invoice> select = ExpertSelection.create( de.abas.erp.db.schema.purchasing.Invoice.class, criteria  );
		Query<de.abas.erp.db.schema.purchasing.Invoice> queryPurchasing = ctx.createQuery(select);
		for (de.abas.erp.db.schema.purchasing.Invoice purchasing : queryPurchasing) {
			Iterable<de.abas.erp.db.schema.purchasing.Invoice.Row> rows = purchasing.getTableRows();
			
			for (de.abas.erp.db.schema.purchasing.Invoice.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	private ArrayList<Product> productsfromSalesInvoice(SelectableObject beleg, DbContext ctx) {
		ArrayList<Product>listProduct = new ArrayList<Product>();
	
		String criteria = "id=" + beleg.getId().toString();
		Selection<de.abas.erp.db.schema.sales.Invoice> select = ExpertSelection.create( de.abas.erp.db.schema.sales.Invoice.class, criteria  );
		Query<de.abas.erp.db.schema.sales.Invoice> querysales = ctx.createQuery(select);
		for (de.abas.erp.db.schema.sales.Invoice sales : querysales) {
			Iterable<de.abas.erp.db.schema.sales.Invoice.Row> rows = sales.getTableRows();
			
			for (de.abas.erp.db.schema.sales.Invoice.Row row : rows) {
				SelectablePart selproduct = row.getProduct();
				Product product = getProduct(selproduct);
				if (product != null) {
					listProduct.add(product);
				}
			}
		
		}
		return listProduct;
	}
	
	
	private Product getProduct(SelectablePart selProduct) {
		if (selProduct instanceof Product) {
			return (Product) selProduct;
		} else {
			return null;
		}
	}

	private void insertProductInRow(Product product, int treeLevel, PdmDocuments head) {
		Row row = head.table().appendRow();
		row.setYtartikel(head.getYartikel());
		row.setYtstufe(treeLevel + 1);
	}

	private void insertProductInRow(Product product, PdmDocuments head) {
		Row row = head.table().appendRow();
		row.setYtartikel(product);
	}

}
