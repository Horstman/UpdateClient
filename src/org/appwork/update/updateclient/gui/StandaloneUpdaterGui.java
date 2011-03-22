package org.appwork.update.updateclient.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storage;
import org.appwork.update.updateclient.Updater;
import org.appwork.update.updateclient.UpdaterState;
import org.appwork.update.updateclient.event.UpdaterEvent;
import org.appwork.update.updateclient.event.UpdaterListener;
import org.appwork.update.updateclient.translation.T;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.net.HTTPException;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public class StandaloneUpdaterGui implements UpdaterListener {

    private final JFrame   frame;
    private final Storage  storage;
    private final Updater  updateController;

    private UpdaterCoreGui coreGUI;
    private JLabel         branchLabel;
    private JButton        btn1;
    private JButton        btn2;
    private JPanel         btnBar;

    public StandaloneUpdaterGui(final Updater updateController) {

        this.updateController = updateController;
        this.setLaf();

        this.storage = JSonStorage.getPlainStorage(updateController.getAppID() + "_WebUpdaterGUI");

        this.frame = new JFrame(updateController.getAppID() + " Updater");
        Dialog.getInstance().setParentOwner(this.frame);
        this.frame.addWindowListener(new WindowListener() {

            public void windowActivated(final WindowEvent arg0) {
            }

            public void windowClosed(final WindowEvent arg0) {
            }

            public void windowClosing(final WindowEvent arg0) {
                StandaloneUpdaterGui.this.cancel();
            }

            public void windowDeactivated(final WindowEvent arg0) {
            }

            public void windowDeiconified(final WindowEvent arg0) {
            }

            public void windowIconified(final WindowEvent arg0) {
            }

            public void windowOpened(final WindowEvent arg0) {
            }

        });
        updateController.getEventSender().addListener(this);

        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // set appicon
        final ArrayList<Image> list = new ArrayList<Image>();

        try {
            list.add(ImageProvider.getBufferedImage("icon", true));

            this.frame.setIconImages(list);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        // Set Application dimensions and locations

        final Dimension dim = new Dimension(this.storage.get("DIMENSION_WIDTH", 300), this.storage.get("DIMENSION_HEIGHT", 60));
        // restore size
        this.frame.setSize(dim);
        // this.frame.setPreferredSize(dim);

        this.frame.setMinimumSize(new Dimension(100, 60));

        this.layoutGUI();

        // restore location. use center of screen as default.
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = screenSize.width / 2 - this.frame.getSize().width / 2;
        final int y = screenSize.height / 2 - this.frame.getSize().height / 2;

        this.frame.setLocation(this.storage.get("LOCATION_X", x), this.storage.get("LOCATION_Y", y));

        this.frame.setVisible(true);

        this.frame.pack();

    }

    protected void cancel() {
        // TODO: cancel while reverting
        if (this.updateController.isInterrupted() || this.updateController.hasPassed(this.updateController.stateDone) || this.updateController.hasPassed(this.updateController.stateError)) {
            this.frame.dispose();
            System.exit(0);
        }
        try {
            Dialog.getInstance().showConfirmDialog(0, T._.dialog_rly_cancel());
            this.updateController.requestExit();
            // frame.dispose();

        } catch (final DialogClosedException e) {
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        if (this.frame.getExtendedState() == Frame.NORMAL && this.frame.isShowing()) {

            this.storage.put("LOCATION_X", this.frame.getLocationOnScreen().x);
            this.storage.put("LOCATION_Y", this.frame.getLocationOnScreen().y);
            this.storage.put("DIMENSION_WIDTH", this.frame.getSize().width);
            this.storage.put("DIMENSION_HEIGHT", this.frame.getSize().height);

        }

        this.frame.setVisible(false);
        this.frame.dispose();
    }

    public JFrame getFrame() {
        return this.frame;

    }

    private void layoutGUI() {
        this.branchLabel = new JLabel("");
        this.branchLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.branchLabel.setForeground(this.frame.getBackground().darker());
        this.frame.setLayout(new MigLayout("ins 0,wrap 1", "[grow,fill]", "[grow,fill][][]"));
        this.frame.add(this.coreGUI = new UpdaterCoreGui(this.updateController));

        if (this.updateController != null) {
            this.updateController.getEventSender().addListener(this.coreGUI);

        }
        this.btnBar = new JPanel(new MigLayout("ins 0 4 0 4", "[grow,fill][][]", "[]"));
        this.btn1 = new JButton("Exit");
        this.btn2 = new JButton("Exit");
        this.frame.add(this.btnBar, "hidemode 3");
        this.btnBar.add(Box.createHorizontalGlue());
        this.btnBar.add(this.btn1, "sg btn,tag cancel");
        this.btnBar.add(this.btn2, "sg btn,tag ok");
        StandaloneUpdaterGui.this.btn2.setText(T._.close());
        StandaloneUpdaterGui.this.btn1.setText(T._.start_jd(StandaloneUpdaterGui.this.updateController.getAppID()));

        // this.btnBar.setVisible(false);
        this.btn2.setEnabled(false);
        this.btn1.setEnabled(false);
        this.btn2.setToolTipText(T._.please_wait_until_update_finished());
        this.btn1.setToolTipText(T._.please_wait_until_update_finished());
        this.frame.add(new JSeparator(), "pushx,growx,gaptop 5");
        this.frame.add(this.branchLabel, "alignx right,gapbottom 5,gapright 5,height 16!");

    }

    public void onServiceNotAvailable(final HTTPException cause) throws InterruptedException {
        this.coreGUI.onServiceNotAvailable(cause);

    }

    @Override
    public void onStateEnter(final UpdaterState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStateExit(final UpdaterState event) {
        if (this.updateController.isFinal()) {
            // error or done
            System.out.println("done");
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    StandaloneUpdaterGui.this.btn1.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            // TODO Auto-generated method stub

                        }
                    });
                    StandaloneUpdaterGui.this.btn2.setEnabled(true);
                    StandaloneUpdaterGui.this.btn1.setEnabled(true);

                }
            };

        }
    }

    @Override
    public void onUpdaterEvent(final UpdaterEvent event) {

        switch (event.getType()) {

            case BRANCH_UPDATED:

                this.updateBranchLabel(T._.UpdateServer_UpdaterGui_onUpdaterEvent_branch(this.updateController.getAppID(), this.updateController.getBranch().getName()));

                break;
            case EXIT_REQUEST:

                // TODO
                // dispose();
                break;
        }

    }

    @Override
    public void onUpdaterModuleEnd(final UpdaterEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdaterModuleProgress(final UpdaterEvent event, final int parameter) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdaterModuleStart(final UpdaterEvent event) {
        // TODO Auto-generated method stub

    }

    private void setLaf() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {

        }
    }

    private void updateBranchLabel(final String branch) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                StandaloneUpdaterGui.this.branchLabel.setText(branch);
                // frame.pack();
            }
        };
    }

}
