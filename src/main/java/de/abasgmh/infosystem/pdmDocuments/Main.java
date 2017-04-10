package de.abasgmh.infosystem.pdmDocuments;

import java.util.ArrayList;
import java.util.List;

import de.abas.eks.jfop.annotation.Stateful;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments.Row;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abasgmh.infosystem.pdmDocuments.data.PdmDocument;
import de.abas.erp.axi2.EventHandlerRunner;

@Stateful
@EventHandler(head = PdmDocuments.class, row = PdmDocuments.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class Main {
	
	
	@ButtonEventHandler(field="start", type = ButtonEventType.AFTER, table = false)
	public void startAfter(PdmDocuments infosys, ScreenControl screenControl) throws EventException{
//		String product = infosys.getYartikel().getIdno();
		String product = infosys.getYartikel().getDrawingNorm();
		String user = infosys.getYuser();
		String password = infosys.getYpassword();
		String server = infosys.getYserver();
		String documentTyp = infosys.getYdokart();
		String pdmsystem = infosys.getYpdmsystem();
		
		
		DocumentSearchfactory documentSearchfactory = new DocumentSearchfactory();
		DocumentsInterface searchdokuments = documentSearchfactory.create(pdmsystem, server , user , password);
		try {
			ArrayList<PdmDocument> pdmDocuments = searchdokuments.getAllDocuments(product);
			for (PdmDocument pdmDocument : pdmDocuments) {
				Row row = infosys.table().appendRow();
				row.setYdateiname(pdmDocument.getFilename());
				row.setYdatend(pdmDocument.getFiletyp());
				
			}
		} catch (PdmDocumentsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@ButtonEventHandler(field="ysaveconfig", type = ButtonEventType.AFTER)
	public void ysaveconfigAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head) throws EventException{
		// TODO Auto-generated method stub
	}

	@ButtonEventHandler(field="ybuanzeigen", type = ButtonEventType.AFTER, table=true)
	public void ybuanzeigenAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head, PdmDocuments.Row currentRow) throws EventException{
		// TODO Auto-generated method stub
	}

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, PdmDocuments head) throws EventException{
		// TODO Auto-generated method stub
	}

}


