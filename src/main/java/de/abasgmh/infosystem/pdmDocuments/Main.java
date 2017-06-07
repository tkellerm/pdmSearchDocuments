package de.abasgmh.infosystem.pdmDocuments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import de.abas.eks.jfop.annotation.Stateful;
import de.abas.eks.jfop.remote.FO;
import de.abas.erp.api.gui.TextBox;
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
import de.abas.erp.db.ContextManager;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments.Row;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM;
import de.abas.erp.db.internal.AbasObject2Impl;
import de.abas.erp.db.internal.AbasObjectImpl;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.purchasing.PurchaseOrder;
import de.abas.erp.db.schema.purchasing.Purchasing;
import de.abas.erp.db.schema.purchasing.Request;
import de.abas.erp.db.schema.sales.BlanketOrder;
import de.abas.erp.db.schema.sales.CostEstimate;
import de.abas.erp.db.schema.sales.Invoice;
import de.abas.erp.db.schema.sales.Opportunity;
import de.abas.erp.db.schema.sales.PackingSlip;
import de.abas.erp.db.schema.sales.Quotation;
import de.abas.erp.db.schema.sales.RepairOrder;
import de.abas.erp.db.schema.sales.Sales;
import de.abas.erp.db.schema.sales.SalesOrder;
import de.abas.erp.db.schema.sales.ServiceOrder;
import de.abas.erp.db.schema.sales.ServiceQuotation;
import de.abas.erp.db.schema.sales.WebOrder;
import de.abas.erp.db.schema.workorder.WorkOrders;
import de.abas.erp.db.util.ContextHelper;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abasgmh.infosystem.pdmDocuments.data.DocMetaData;
import de.abasgmh.infosystem.pdmDocuments.data.PdmDocument;
import de.abasgmh.infosystem.pdmDocuments.utils.Util;

@Stateful
@EventHandler(head = PdmDocuments.class, row = PdmDocuments.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class Main {
	
	private static String CONFIGFILE = "owpdm/pdmDocuments.config.properties";
	private static String PDM_CONFIG_SERVER = "pdm.config.server";
	private static String PDM_CONFIG_USER = "pdm.config.user";
	private static String PDM_CONFIG_PASSWORD = "pdm.config.password";
	private static String PDM_CONFIG_PDMSYSTEM = "pdm.config.pdmsystem";
	
	
	
	@ButtonEventHandler(field="start", type = ButtonEventType.AFTER, table = false)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head) throws EventException{
		
		getConfigInMask(head , ctx);
	if (head.getYartikel() != null) {
//	    String product = head.getYartikel().getIdno();
		String product = head.getYartikel().getDrawingNorm();
		String user = head.getYuser();
		String password = head.getYpassword();
		String server = head.getYserver();
		String documentTyp = head.getYdokart();
		String pdmsystem = head.getYpdmsystem();
		head.table().clear();
		loadProductsInTable(head, ctx);
		DocumentSearchfactory documentSearchfactory = new DocumentSearchfactory();
		DocumentsInterface searchdokuments = documentSearchfactory.create(pdmsystem, server, user, password);
		if (searchdokuments != null) {
			try {
				Iterable<Row> rows = head.table().getRows();
				for (Row row : rows) {
					if (row.getYtartikel() != null) {
						insertDocuments(product, searchdokuments, head);
					}
				}
	
			} catch (PdmDocumentsException | IOException e) {
				showErrorBox(ctx, e.getMessage());
			}
		} else {
			showErrorBox(ctx, Util.getMessage("main.error.noConnection", head.getYserver()));
		} 
	}else {
		showErrorBox(ctx, Util.getMessage("main.error.noProduct"));
	}
		
	}
	
	

	private void insertDocuments(String product, DocumentsInterface searchdokuments, PdmDocuments head) throws PdmDocumentsException, IOException {

		ArrayList<PdmDocument> pdmDocuments = searchdokuments.getAllDocuments(product);
		for (PdmDocument pdmDocument : pdmDocuments) {
			
			if (checkdocuments(head , pdmDocument)) {
				Row row = head.table().appendRow();
				row.setYdateiname(pdmDocument.getFilename());
				row.setYdatend(pdmDocument.getFiletyp());
				row.setYpfad(pdmDocument.getFile().getCanonicalPath().toString());
				row.setYdoktyp(pdmDocument.getDocumenttyp());
			}
		}
	}



	private boolean checkdocuments(PdmDocuments head, PdmDocument pdmDocument) {
		// TODO Auto-generated method stub
		return true;
	}



	@ButtonEventHandler(field="ysaveconfig", type = ButtonEventType.AFTER)
	public void ysaveconfigAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head) throws EventException{
		
		File propertiesFile = new File(CONFIGFILE);
		
		Properties configProperties = new Properties();

		configProperties.setProperty(PDM_CONFIG_SERVER, head.getYserver());
		configProperties.setProperty(PDM_CONFIG_USER, head.getYuser());
		configProperties.setProperty(PDM_CONFIG_PASSWORD, head.getYpassword());
		configProperties.setProperty(PDM_CONFIG_PDMSYSTEM, head.getYpdmsystem());
			
		FileOutputStream out;
		try {
			out = new FileOutputStream(propertiesFile);
			configProperties.store(out, "---config PDMDocuments---");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@ButtonEventHandler(field="ybuanzeigen", type = ButtonEventType.AFTER, table=true)
	public void ybuanzeigenAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head, PdmDocuments.Row currentRow) throws EventException{
		    String varnamezieldat = "xtzieldat";
		    String vartypezieldat = "text";
		    String varnamecmd = "xtcmd";
		    String vartypecmd = "text";
//			UserTextBuffer userTextBuffer = BufferFactory.newInstance(true).getUserTextBuffer();
//			if (!userTextBuffer.isVarDefined(varnamezieldat)) {
//				userTextBuffer.defineVar(vartypezieldat, varnamezieldat);
//			}
//			if (!userTextBuffer.isVarDefined(varnamecmd)) {
//				userTextBuffer.defineVar(vartypecmd, varnamecmd);
//			}
			
			
			String zieldatvalue = "C:\\abas\\pdmDocuments\\" 	+   currentRow.getYdateiname() ;
//			userTextBuffer.setValue(varnamezieldat , zieldatvalue);
			
			String valuecmd = " -PC -BIN " +  currentRow.getYpfad() + " " + zieldatvalue;
			

			FO.pc_copy(valuecmd);
			FO.pc_open(zieldatvalue);
			
	}

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head) throws EventException{
		getConfigInMask(head , ctx);
	}

	private void showErrorBox(DbContext ctx, String message) {
        new TextBox(ctx, Util.getMessage("main.exception.title"), message).show();
    }
	
	private void getConfigInMask(PdmDocuments head, DbContext ctx) {
		
		File propertiesFile = new File(CONFIGFILE);

		if (propertiesFile.exists()) {
			
			
			FileInputStream in;
			
				try {
					in = new FileInputStream(propertiesFile);
					Properties configProperties = new Properties();
					configProperties .load(in);
					in.close();
											
					// config contains all properties read from the file
					head.setYserver(configProperties.getProperty(PDM_CONFIG_SERVER));
					head.setYuser(configProperties.getProperty(PDM_CONFIG_USER));
					head.setYpassword(configProperties.getProperty(PDM_CONFIG_PASSWORD));
				 	head.setYpdmsystem(configProperties.getProperty(PDM_CONFIG_PDMSYSTEM));
				} catch (IOException e) {

					showErrorBox(ctx, Util.getMessage("pdmDocument.error.loadKonfiguration") + "/n" + e.getMessage() );
						
				}
			 
		}else {
			showErrorBox(ctx, Util.getMessage("pdmDocument.info.loadKonfiguration") );
		}
		
	}



	private void loadProductsInTable(PdmDocuments head , DbContext ctx) {
		
			
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
					Product product = (Product)selProduct;
					insertProductInRow(product , treeLevel , head);
				}
			}
			mlb.close();
			
		}else {
			
			if (head.getYbeleg() == null) {
				insertProductInRow(head.getYartikel(), head);
			}else {
				SelectableObject beleg = head.getYbeleg();
				ArrayList<Product>listProduct = getProducts(beleg);
				for (Product product : listProduct) {
					insertProductInRow(product, head);
				}
				
			}			
		}
		
	}



	private ArrayList<Product> getProducts(SelectableObject beleg) {
		ArrayList<Product> listProduct = new ArrayList<Product>(); 
		
		if (beleg instanceof Sales) {
			
			if (beleg instanceof BlanketOrder) {
				BlanketOrder sbeleg = (BlanketOrder)beleg;
				Iterable<de.abas.erp.db.schema.sales.BlanketOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.BlanketOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof Quotation) {
				Quotation sbeleg = (Quotation)beleg;
				Iterable<de.abas.erp.db.schema.sales.Quotation.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.Quotation.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof SalesOrder) {
				SalesOrder sbeleg = (SalesOrder)beleg;
				Iterable<de.abas.erp.db.schema.sales.SalesOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.SalesOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
				
			}else if (beleg instanceof PackingSlip) {
				PackingSlip sbeleg = (PackingSlip)beleg;
				Iterable<de.abas.erp.db.schema.sales.PackingSlip.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.PackingSlip.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof Invoice) {
				Invoice sbeleg = (Invoice) beleg;
				Iterable<de.abas.erp.db.schema.sales.Invoice.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.Invoice.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof ServiceQuotation) {
				ServiceQuotation sbeleg = (ServiceQuotation)beleg;
				Iterable<de.abas.erp.db.schema.sales.ServiceQuotation.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.ServiceQuotation.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof ServiceOrder) {
				ServiceOrder sbeleg = (ServiceOrder)beleg;
				Iterable<de.abas.erp.db.schema.sales.ServiceOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.ServiceOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof RepairOrder) {
				RepairOrder sbeleg = (RepairOrder)beleg;
				Iterable<de.abas.erp.db.schema.sales.RepairOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.RepairOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof Opportunity) {
				Opportunity sbeleg = (Opportunity)beleg;
				Iterable<de.abas.erp.db.schema.sales.Opportunity.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.Opportunity.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof CostEstimate) {
				CostEstimate sbeleg = (CostEstimate)beleg;
				Iterable<de.abas.erp.db.schema.sales.CostEstimate.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.CostEstimate.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof WebOrder) {
				WebOrder sbeleg = (WebOrder)beleg;
				Iterable<de.abas.erp.db.schema.sales.WebOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.WebOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}
			
			
		}else if (beleg instanceof Purchasing) {
			if (beleg instanceof BlanketOrder) {
				BlanketOrder sbeleg = (BlanketOrder)beleg;
				Iterable<de.abas.erp.db.schema.sales.BlanketOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.sales.BlanketOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
				
			}else if (beleg instanceof Request) {
				Request sbeleg = (Request)beleg;
				Iterable<de.abas.erp.db.schema.purchasing.Request.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.purchasing.Request.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof PurchaseOrder) {
				PurchaseOrder sbeleg = (PurchaseOrder)beleg;
				Iterable<de.abas.erp.db.schema.purchasing.PurchaseOrder.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.purchasing.PurchaseOrder.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof de.abas.erp.db.schema.purchasing.PackingSlip) {
				de.abas.erp.db.schema.purchasing.PackingSlip sbeleg = (de.abas.erp.db.schema.purchasing.PackingSlip)beleg;
				Iterable<de.abas.erp.db.schema.purchasing.PackingSlip.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.purchasing.PackingSlip.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}else if (beleg instanceof de.abas.erp.db.schema.purchasing.Invoice) {
				de.abas.erp.db.schema.purchasing.Invoice sbeleg = (de.abas.erp.db.schema.purchasing.Invoice)beleg;
				Iterable<de.abas.erp.db.schema.purchasing.Invoice.Row> rows = sbeleg.table().getRows();
				for (de.abas.erp.db.schema.purchasing.Invoice.Row row : rows) {
					SelectablePart selproduct = row.getProduct();
					Product product = getProduct(selproduct);
					if (product != null) {
						listProduct.add(product);
					}
				}
			}
			
		}else if (beleg instanceof WorkOrders) {
			WorkOrders sbeleg = (WorkOrders)beleg;
			Product product = getProduct(sbeleg.getProduct());
			if (product != null) {
				listProduct.add(product);
			}
			
		} 
		
		
		return listProduct;
	}



	private Product getProduct(SelectablePart selProduct) {
		if (selProduct instanceof Product) {
			return (Product)selProduct;
		}else {
			return null;
		}
	}



	private void insertProductInRow(Product product, int treeLevel, PdmDocuments head) {
		Row row = head.table().appendRow();
		row.setYtartikel(head.getYartikel());
		row.setYtstufe(treeLevel + 1 );
	}



	private void insertProductInRow(Product product, PdmDocuments head) {
		Row row = head.table().appendRow();
		row.setYtartikel(head.getYartikel());
		
	}
	
}


