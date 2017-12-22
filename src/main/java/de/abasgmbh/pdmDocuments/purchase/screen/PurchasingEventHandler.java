package de.abasgmbh.pdmDocuments.purchase.screen;

import java.io.File;
import java.io.IOException;

import de.abas.erp.api.AppContext;
import de.abas.erp.api.commands.CommandFactory;
import de.abas.erp.api.commands.FieldManipulator;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.owpdm.PdmDocuments;
import de.abas.erp.db.schema.purchasing.PurchasingEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abasgmbh.pdmDocuments.infosystem.utils.Util;

@EventHandler(head = PurchasingEditor.class)

@RunFopWith(EventHandlerRunner.class)

public class PurchasingEventHandler {

	@ButtonEventHandler(field = "ypdm01budocsammel", type = ButtonEventType.AFTER)
	public void ypdm01budocsammelAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx,
			PurchasingEditor head) throws EventException {
		try {
			checkTempFile(head);
			openPdmDocuments(head, ctx);
		} catch (IOException e) {
			Util.showErrorBox(ctx, Util.getMessage("purchasing.tempfile.error", e.getMessage()));
			e.printStackTrace();
		}

	}

	private void openPdmDocuments(PurchasingEditor head, DbContext ctx) {

		CommandFactory commandFactory = AppContext.createFor(ctx).getCommandFactory();

		FieldManipulator<PdmDocuments> fieldManipulator = commandFactory.getScrParamBuilder(PdmDocuments.class);
		fieldManipulator.setReference(PdmDocuments.META.ybeleg, head);
		fieldManipulator.setField(PdmDocuments.META.yanhangliste, head.getYpdm01anhanglist());
		fieldManipulator.pressButton(PdmDocuments.META.start);

		commandFactory.startInfosystem(PdmDocuments.class, fieldManipulator);

	}

	private void checkTempFile(PurchasingEditor head) throws IOException {
		if (head.getYpdm01anhanglist().isEmpty()) {
			head.setYpdm01anhanglist(gettempFile().toString());
		}
	}

	private File gettempFile() throws IOException {
		File tmpverz = new File("tmp");
		File tempFile;
		tempFile = File.createTempFile("pdmDocSammel", ".TMP", tmpverz);

		return tempFile;

	}
}
