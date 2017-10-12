package de.abasgmbh.pdmDocuments.printscreen;

import de.abasgmbh.pdmDocuments.infosystem.utils.Util;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.common.type.enums.EnumPrinterType;
import de.abas.erp.db.schema.infrastructure.Printer;
import de.abas.erp.db.schema.infrastructure.SelectableInfrastructure;
import de.abas.erp.db.schema.printparameter.PrintDialogEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;

import java.io.File;
import java.io.IOException;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.db.DbContext;
import de.abas.erp.axi.screen.ScreenControl;


@EventHandler(head = PrintDialogEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class PrintDialogEventHandler {

	@FieldEventHandler(field="printer", type = FieldEventType.EXIT)
	public void printerExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, PrintDialogEditor head) throws EventException{
		
		SelectableInfrastructure selPrinter = head.getPrinter();
		
		if (selPrinter instanceof Printer) {
			Printer printer = (Printer)selPrinter;
			EnumPrinterType printerTyp = printer.getPrinterType();
			if (printerTyp.equals(EnumPrinterType.EmailClientSend) || 
					printerTyp.equals(EnumPrinterType.EmailClientView) ||
					printerTyp.equals(EnumPrinterType.SMTPServer)) {
				
					try {
						head.setAttachmentFileList(gettempFile().toString());
					} catch (IOException e) {
						Util.showErrorBox(ctx, "printscreen.error.tempFile");
					}
				
			}
		}
		
		
		
	}

	private File gettempFile() throws IOException {
		File tmpverz = new File("rmtmp");
		File tempFile;
		tempFile = File.createTempFile("pdmDoc", ".TMP", tmpverz);
		
		return tempFile;
		

	}
	
	
	
}
