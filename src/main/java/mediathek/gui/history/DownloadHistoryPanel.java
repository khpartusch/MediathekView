package mediathek.gui.history;

import mediathek.config.Daten;
import mediathek.gui.messages.history.DownloadHistoryChangedEvent;
import net.engio.mbassy.listener.Handler;

import javax.swing.*;

public final class DownloadHistoryPanel extends PanelErledigteUrls {
    public DownloadHistoryPanel(Daten d) {
        super(d);
        workList = daten.getSeenHistoryController();

        jTable1.setModel(createDataModel());
        setsum();
        jTable1.getModel().addTableModelListener(e -> {
            System.out.println("TABLE CHANGED EVENT");
            setsum();
        });

        d.getMessageBus().subscribe(this);
    }

    @Handler
    private void handleChangeEvent(DownloadHistoryChangedEvent e) {
        SwingUtilities.invokeLater(this::changeListHandler);
    }
}
