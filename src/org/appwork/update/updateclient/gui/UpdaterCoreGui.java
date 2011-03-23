package org.appwork.update.updateclient.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

import net.miginfocom.swing.MigLayout;

import org.appwork.update.exchange.Mirror;
import org.appwork.update.updateclient.InstalledFile;
import org.appwork.update.updateclient.Updater;
import org.appwork.update.updateclient.UpdaterState;
import org.appwork.update.updateclient.event.UpdaterEvent;
import org.appwork.update.updateclient.event.UpdaterListener;
import org.appwork.update.updateclient.http.ClientUpdateRequiredException;
import org.appwork.update.updateclient.http.UpdateServerException;
import org.appwork.update.updateclient.translation.T;
import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.HTTPException;
import org.appwork.utils.swing.EDTHelper;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.SwingUtils;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;

public class UpdaterCoreGui extends JPanel implements UpdaterListener, ActionListener {
    /**
     * 
     */
    private static final long      serialVersionUID = 1L;
    private final UpdaterGuiPanel  panel;
    private final ProgressLogo     progressLogo;

    private final JLabel           lblDetailsLabel;
    private final JButton          btnDetails;
    private final JTextPane        logField;
    private final JScrollPane      scrollPane;
    private final SimpleDateFormat logDateFormat;
    private final SimpleDateFormat logTimeFormat;
    private final Updater          updateController;
    private int                    currentStepSize  = 2;
    private final Color            defBarColor;

    public UpdaterCoreGui(final Updater updateController) {
        this.updateController = updateController;
        this.logDateFormat = new SimpleDateFormat("dd.MM.yy");
        this.logTimeFormat = new SimpleDateFormat("HH:mm:ss");

        this.setLayout(new MigLayout("ins 0,wrap 1", "[]", " [][]"));

        this.panel = new UpdaterGuiPanel(T._.getPanelTitle(updateController.getAppID()));
        this.defBarColor = this.panel.getBar().getForeground();
        this.progressLogo = new ProgressLogo(IconIO.getImageIcon(this.getClass().getResource("resource/updaterIcon100.png")), IconIO.getImageIcon(this.getClass().getResource("resource/updaterIcon0.png")));

        this.lblDetailsLabel = new JLabel(T._.UpdateServer_UpdaterGui_layoutGUI_details());
        this.lblDetailsLabel.setVisible(false);
        SwingUtils.toBold(this.lblDetailsLabel);

        this.add(this.progressLogo, "split 2,gapright 10,gaptop 5,gapleft 5");
        this.add(this.panel, "growx,pushx,gapright 5, gaptop 5");

        this.btnDetails = new JButton("Details");
        SwingUtils.toBold(this.btnDetails);
        this.btnDetails.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.btnDetails.setFocusable(false);
        this.btnDetails.setContentAreaFilled(false);
        this.btnDetails.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, this.panel.getBackground().darker()));
        this.btnDetails.addActionListener(this);
        this.logField = new JTextPane();
        this.logField.setEditable(true);
        this.logField.setAutoscrolls(true);
        this.scrollPane = new JScrollPane(this.logField);
        this.scrollPane.setVisible(false);
        this.logField.setEditable(true);
        this.logField.setAutoscrolls(true);
        this.add(this.btnDetails, "hidemode 3,shrinky,alignx right,aligny top,gapright 5");
        this.add(this.lblDetailsLabel, "hidemode 3,gaptop 5,gapleft 5");
        this.add(this.scrollPane, "hidemode 3,height 100:120:n,pushx,growx,pushy 99,growy 99,gapleft 5,gapright 5");
        this.add(Box.createVerticalGlue(), "pushy 1,growy 1");
        this.log(T._.UpdateServer_UpdaterGui_UpdaterGui_started(this.logDateFormat.format(new Date())));
        this.log(T._.UpdateServer_UpdaterGui_UpdaterGui_path(updateController.getInstallDirectory().getAbsolutePath()));

    }

    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.btnDetails) {
            this.expand();
        }
    }

    public void expand() {
        this.btnDetails.setVisible(false);
        this.scrollPane.setVisible(true);
        this.lblDetailsLabel.setVisible(true);
        this.pack();

    }

    // protected void cancel() {
    // try {
    // Dialog.getInstance().showConfirmDialog(0, "Realy cancel update?");
    // updateController.requestExit();
    // dispose();
    //
    // } catch (final DialogClosedException e) {
    // e.printStackTrace();
    // } catch (final DialogCanceledException e) {
    // e.printStackTrace();
    // }
    // }

    public JProgressBar getBar() {
        return this.panel.getBar();
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension ret = super.getPreferredSize();
        ret.width = 600;
        return ret;
    }

    public ProgressLogo getProgressLogo() {
        return this.progressLogo;
    }

    public JProgressBar getSubBar() {
        return this.panel.getSubBar();
    }

    public void log(final String msg) {
        final Document doc = this.logField.getDocument();

        final EditorKit editorkit = this.logField.getEditorKit();
        System.out.println(this.logTimeFormat.format(new Date()) + " - " + msg);
        final StringReader r = new StringReader("\r\n" + this.logTimeFormat.format(new Date()) + " - " + msg);
        try {
            editorkit.read(r, doc, doc.getLength());
        } catch (final Exception e1) {
            e1.printStackTrace();
        }

    }

    /**
     * call this method only if update ran successfully
     * 
     * @param installedFiles
     */
    private void onFinished(final ArrayList<File> installedFiles) {

        new EDTRunner() {

            @Override
            protected void runInEDT() {

                if (installedFiles != null && installedFiles.size() > 0) {
                    UpdaterCoreGui.this.panel.getBar().setValue(100);
                    UpdaterCoreGui.this.panel.getSubBar().setValue(100);

                    UpdaterCoreGui.this.panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_runInEDT_updates(installedFiles.size()));
                    UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_runInEDT_updates(installedFiles.size()));
                    UpdaterCoreGui.this.panel.getBar().setString(T._.UpdateServer_UpdaterGui_runInEDT_successfull());
                } else {
                    UpdaterCoreGui.this.panel.getBar().setValue(100);
                    UpdaterCoreGui.this.panel.getSubBar().setValue(100);

                    UpdaterCoreGui.this.panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_runInEDT_noupdates());
                    UpdaterCoreGui.this.panel.getBar().setString(T._.UpdateServer_UpdaterGui_runInEDT_finished());
                    UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_runInEDT_finished());
                }
                UpdaterCoreGui.this.progressLogo.setProgress(1.0f);
                UpdaterCoreGui.this.panel.getBar().setIndeterminate(false);
                UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
            }
        };
    }

    private void onInterrupted() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                final String message = T._.interrupted();

                UpdaterCoreGui.this.log(message);
                UpdaterCoreGui.this.panel.getSubBar().setForeground(Color.ORANGE.darker());
                UpdaterCoreGui.this.panel.getBar().setForeground(Color.ORANGE.darker());
                UpdaterCoreGui.this.panel.getSubBar().setValue(100);
                UpdaterCoreGui.this.panel.getBar().setValue(100);
                UpdaterCoreGui.this.panel.getBar().setIndeterminate(false);
                UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                UpdaterCoreGui.this.panel.getBar().setString(T._.interrupted_title());
                UpdaterCoreGui.this.panel.getSubBar().setString(message);
                UpdaterCoreGui.this.getProgressLogo().setProgress(1.0f);
                UpdaterCoreGui.this.expand();

            }
        };

    }

    public void onServiceNotAvailable(final HTTPException e) throws InterruptedException {

        final String oldBar = this.panel.getSubBar().getString();
        final String oldSubBar = this.panel.getSubBar().getString();
        final boolean indeterminate = this.panel.getSubBar().isIndeterminate();
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_onServiceNotAvailable_wait());

                UpdaterCoreGui.this.panel.getBar().setIndeterminate(true);

                UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);
                UpdaterCoreGui.this.panel.getBar().setString(T._.UpdateServer_UpdaterGui_runInEDT_mainbar());
                UpdaterCoreGui.this.panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_onServiceNotAvailable_bar());

            }
        };
        Thread.sleep(10000);
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                UpdaterCoreGui.this.panel.getBar().setIndeterminate(false);
                UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(indeterminate);
                UpdaterCoreGui.this.panel.getBar().setString(oldBar);
                UpdaterCoreGui.this.panel.getSubBar().setString(oldSubBar);

            }
        };
    }

    @Override
    public void onStateEnter(final UpdaterState state) {
        System.out.println(state);

        this.currentStepSize = state.getChildren().size() == 0 ? 1 : ((UpdaterState) state.getChildren().get(0)).getProgress() - state.getProgress();

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                UpdaterCoreGui.this.panel.getSubBar().setString(null);
                if (state == UpdaterCoreGui.this.updateController.stateDone) {

                } else if (state == UpdaterCoreGui.this.updateController.stateDownloadData) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);

                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_download(), state.getProgress());
                } else if (state == UpdaterCoreGui.this.updateController.stateBranchUpdate) {
                    // UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);
                    //
                    // UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_branchlist(),
                    // state.getProgress());

                } else if (state == UpdaterCoreGui.this.updateController.stateDownloadHashList) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);
                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_hashlist(), state.getProgress());

                    if (UpdaterCoreGui.this.updateController.getFilesToInstall().size() > 0) {
                        UpdaterCoreGui.this.log(T._.uninstalledfiles(UpdaterCoreGui.this.updateController.getFilesToInstall().size()));
                    }

                } else if (state == UpdaterCoreGui.this.updateController.stateCreatePackage) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);

                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_package(), state.getProgress());

                } else if (state == UpdaterCoreGui.this.updateController.stateExtract) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);
                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_extract(), state.getProgress());
                } else if (state == UpdaterCoreGui.this.updateController.stateFilter) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_filter(), state.getProgress());
                } else if (state == UpdaterCoreGui.this.updateController.stateInstall) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_install(), state.getProgress());
                } else if (state == UpdaterCoreGui.this.updateController.stateError) {
                } else if (state == UpdaterCoreGui.this.updateController.stateWaitForUnlock) {
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);
                    //
                    // this.setModuleProgress(T.UpdateServer_UpdaterGui_onStateChange_locked(updateController.getAppID()),
                    // state.getProgress());

                }
                if (UpdaterCoreGui.this.panel.getSubBar().isIndeterminate()) {
                    UpdaterCoreGui.this.panel.getSubBar().setString(T._.please_wait());
                }
            }
        };
    }

    @Override
    public void onStateExit(final UpdaterState state) {
        System.out.println(state);

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                UpdaterCoreGui.this.panel.getSubBar().setString(null);
                if (state == UpdaterCoreGui.this.updateController.stateDone) {
                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_successfull(), state.getProgress());

                } else if (state == UpdaterCoreGui.this.updateController.stateDownloadData) {
                } else if (state == UpdaterCoreGui.this.updateController.stateBranchUpdate) {
                } else if (state == UpdaterCoreGui.this.updateController.stateDownloadHashList) {
                } else if (state == UpdaterCoreGui.this.updateController.stateCreatePackage) {
                } else if (state == UpdaterCoreGui.this.updateController.stateExtract) {
                } else if (state == UpdaterCoreGui.this.updateController.stateFilter) {
                    final int updates = UpdaterCoreGui.this.updateController.getFilesToInstall().size() + UpdaterCoreGui.this.updateController.getUpdates().size() + UpdaterCoreGui.this.updateController.getFilesToRemove().size();
                    if (updates == 0) {
                        UpdaterCoreGui.this.log(T._.log_you_are_up2date());

                    } else {
                        UpdaterCoreGui.this.log(T._.log_x_files_to_update_found(updates));
                    }
                } else if (state == UpdaterCoreGui.this.updateController.stateInstall) {
                } else if (state == UpdaterCoreGui.this.updateController.stateError) {
                    Throwable exception = UpdaterCoreGui.this.updateController.getException();
                    String message = T._.UpdateServer_UpdaterGui_onException_error_occured(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName());

                    if (exception != null) {
                        if (exception instanceof InterruptedException || UpdaterCoreGui.this.updateController.isInterrupted()) {

                            UpdaterCoreGui.this.log(T._.userinterrupted());
                            UpdaterCoreGui.this.panel.getSubBar().setForeground(Color.ORANGE.darker());
                            UpdaterCoreGui.this.panel.getBar().setForeground(Color.ORANGE.darker());
                            UpdaterCoreGui.this.panel.getSubBar().setValue(100);
                            UpdaterCoreGui.this.panel.getBar().setValue(100);
                            UpdaterCoreGui.this.panel.getBar().setIndeterminate(false);
                            UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                            UpdaterCoreGui.this.panel.getBar().setString(T._.interrupted_title());
                            UpdaterCoreGui.this.panel.getSubBar().setString(T._.userinterrupted());
                            UpdaterCoreGui.this.getProgressLogo().setProgress(1.0f);
                            UpdaterCoreGui.this.expand();
                            return;
                        } else if (exception instanceof ClientUpdateRequiredException) {
                            message = T._.clientUpdate();
                            UpdaterCoreGui.this.log(message);
                            UpdaterCoreGui.this.panel.getSubBar().setForeground(Color.ORANGE.darker());
                            UpdaterCoreGui.this.panel.getBar().setForeground(Color.ORANGE.darker());
                            UpdaterCoreGui.this.panel.getSubBar().setValue(100);
                            UpdaterCoreGui.this.panel.getBar().setValue(100);
                            UpdaterCoreGui.this.panel.getBar().setIndeterminate(true);
                            UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(true);
                            UpdaterCoreGui.this.panel.getBar().setString(T._.clientupdate_title());
                            UpdaterCoreGui.this.panel.getSubBar().setString(message);
                            UpdaterCoreGui.this.getProgressLogo().setProgress(1.0f);
                            UpdaterCoreGui.this.expand();
                            return;

                        }
                    }
                    UpdaterCoreGui.this.panel.getSubBar().setForeground(Color.RED.darker());
                    UpdaterCoreGui.this.panel.getBar().setForeground(Color.RED.darker());
                    UpdaterCoreGui.this.panel.getSubBar().setValue(100);
                    UpdaterCoreGui.this.panel.getBar().setValue(100);
                    UpdaterCoreGui.this.panel.getBar().setIndeterminate(false);
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                    UpdaterCoreGui.this.panel.getSubBar().setString(null);

                    Log.exception(exception);
                    if (exception.getCause() != null) {
                        exception = exception.getCause();
                    }
                    if (exception instanceof SocketException) {
                        message = T._.UpdateException_socket(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName());
                    } else if (exception instanceof SocketTimeoutException) {
                        message = T._.UpdateException_socket(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName());
                    } else if (exception instanceof UpdateServerException) {

                        switch (((UpdateServerException) exception).getType()) {
                            case DOWNLOADPACKAGE_VALIDATION_ERROR:
                            case DOWNLOADPACKAGE_VALIDATION_ERROR_INTERN:
                            case PKG_CREATE_HASHMISMATCH:
                                message = T._.error_unknown_server();
                                break;
                            case UNKNOWN_APP:
                                message = T._.error_unknown_app(UpdaterCoreGui.this.updateController.getAppID());
                                break;
                            case UNKNOWN_BRANCH:
                                message = T._.error_unknown_branch(UpdaterCoreGui.this.updateController.getBranch().getName());
                            case INVALID_BRANCH:
                                message = T._.error_invalid_branch(UpdaterCoreGui.this.updateController.getBranch().getName());

                                break;

                        }
                    }
                    UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                    UpdaterCoreGui.this.setModuleProgress(T._.UpdateServer_UpdaterGui_onStateChange_failed(), 100);
                    UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_onStateChange_failed2(message));
                    UpdaterCoreGui.this.panel.getBar().setString(T._.error_occured());
                    UpdaterCoreGui.this.panel.getSubBar().setString(T._.error_occured_detailed());
                    UpdaterCoreGui.this.getProgressLogo().setProgress(1.0f);
                    UpdaterCoreGui.this.expand();

                } else if (state == UpdaterCoreGui.this.updateController.stateWaitForUnlock) {

                }

            }
        };

    }

    @Override
    public void onUpdaterEvent(final UpdaterEvent event) {

        switch (event.getType()) {
            case LOCKED:
                new EDTHelper<T>() {

                    @Override
                    public T edtRun() {
                        try {
                            UpdaterCoreGui.this.log(T._.locked(UpdaterCoreGui.this.updateController.getAppID()));
                            Dialog.getInstance().showConfirmDialog(0, T._.locked_dialog_title(), T._.locked_dialog_msg(UpdaterCoreGui.this.updateController.getAppID()), null, null, T._.cancel_update());
                        } catch (final DialogNoAnswerException e) {
                            UpdaterCoreGui.this.updateController.requestExit();
                        }
                        return null;
                    }
                }.getReturnValue();
                break;
            case DELETED_FILE:
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        final InstalledFile iFile = (InstalledFile) event.getParameter();

                        UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_onUpdaterEvent_remove(iFile.getRelPath()));
                        UpdaterCoreGui.this.panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_onUpdaterEvent_remove(iFile.getRelPath()));
                        System.out.println(T._.filelog_deletedfile(new File(UpdaterCoreGui.this.updateController.getInstallDirectory(), iFile.getRelPath()).getAbsolutePath()));
                    }
                };
                break;
            case BRANCH_RESET:
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {

                        UpdaterCoreGui.this.log(T._.branch_resetted(UpdaterCoreGui.this.updateController.getAppID(), event.getParameter() + ""));
                        // panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_onUpdaterEvent_remove(iFile.getRelPath()));

                    }
                };
                Dialog.getInstance().showMessageDialog(T._.dialag_branch_resetted(), T._.dialog_branch_resetted_msg(this.updateController.getAppID(), event.getParameter() + ""));

                break;

            case BRANCH_UPDATED:
                new EDTRunner() {

                    @Override
                    protected void runInEDT() {

                        UpdaterCoreGui.this.log(T._.branch_updated(event.getParameter() + ""));
                        // panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_onUpdaterEvent_remove(iFile.getRelPath()));

                    }
                };

                break;

        }

    }

    @Override
    public void onUpdaterModuleEnd(final UpdaterEvent event) {

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                switch (event.getType()) {
                    case END_FILELIST_UPDATE:

                        System.out.println("Files in List: " + UpdaterCoreGui.this.updateController.getUpdates().size());

                        break;
                    case END_FILTERING:

                        UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_onUpdaterModuleEnd_end_filtering(UpdaterCoreGui.this.updateController.getUpdates().size()));

                        System.out.println(UpdaterCoreGui.this.updateController.getUpdates().size() + " Updates found");

                        break;
                    case END_INSTALL_FILE:

                        System.out.println(T._.filelog_installedfile(((File) event.getParameter(0)).getAbsolutePath()));

                }
            }
        };
        this.panel.getSubBar().setString("");

    }

    @Override
    public void onUpdaterModuleProgress(final UpdaterEvent event, final int percent) {
        // System.out.println("Progress: " + event.getType() + " : " + percent);
        new EDTRunner() {

            @Override
            protected void runInEDT() {

                UpdaterCoreGui.this.panel.getSubBar().setValue(percent);

                final int dynamicPercent = (int) (UpdaterCoreGui.this.updateController.getProgress() + UpdaterCoreGui.this.currentStepSize * percent / 100.0f);

                UpdaterCoreGui.this.progressLogo.setProgress(dynamicPercent / 100.0f);
                UpdaterCoreGui.this.panel.setModuleProgress(null, dynamicPercent);

            }
        };

    }

    @Override
    public void onUpdaterModuleStart(final UpdaterEvent event) {

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                switch (event.getType()) {
                    case START_DOWNLOAD_FILE:
                        final Mirror mirror = (Mirror) event.getParameter();

                        UpdaterCoreGui.this.panel.getSubBar().setString(T._.UpdateServer_UpdaterGui_onUpdaterModuleStart_download(SizeFormatter.formatBytes(mirror.getSize())));
                        UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_onUpdaterModuleStart_download(SizeFormatter.formatBytes(mirror.getSize())));

                        break;
                    case START_INSTALL_FILE:
                        UpdaterCoreGui.this.log("      -> " + (String) event.getParameter(1));
                        UpdaterCoreGui.this.panel.getSubBar().setString(((String) event.getParameter(1)));

                        break;

                    case START_REVERT:

                        UpdaterCoreGui.this.panel.getSubBar().setForeground(Color.RED);
                        UpdaterCoreGui.this.panel.getBar().setForeground(Color.RED);
                        UpdaterCoreGui.this.panel.getBar().setString(T._.reverting_title());
                        UpdaterCoreGui.this.panel.getSubBar().setString(T._.reverting_msg());
                        UpdaterCoreGui.this.log(T._.error_occured_start_reverting());

                        break;

                }
            }
        };

    }

    private void pack() {
        Container p = this.getParent();
        while (p != null) {
            if (p instanceof Window) {
                ((Window) p).pack();
                break;
            }
            p = p.getParent();
        }
    }

    public void reset() {
        new EDTHelper<T>() {

            @Override
            public T edtRun() {

                UpdaterCoreGui.this.setModuleProgress("", 0);
                UpdaterCoreGui.this.logField.setText("");
                UpdaterCoreGui.this.panel.getSubBar().setForeground(UpdaterCoreGui.this.defBarColor);
                UpdaterCoreGui.this.panel.getBar().setForeground(UpdaterCoreGui.this.defBarColor);
                UpdaterCoreGui.this.panel.getSubBar().setValue(0);
                UpdaterCoreGui.this.panel.getBar().setValue(0);
                UpdaterCoreGui.this.panel.getBar().setIndeterminate(false);
                UpdaterCoreGui.this.panel.getSubBar().setIndeterminate(false);
                UpdaterCoreGui.this.panel.getBar().setString("");
                UpdaterCoreGui.this.panel.getSubBar().setString("");
                UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_UpdaterGui_started(UpdaterCoreGui.this.logDateFormat.format(new Date())));
                UpdaterCoreGui.this.log(T._.UpdateServer_UpdaterGui_UpdaterGui_path(UpdaterCoreGui.this.updateController.getInstallDirectory().getAbsolutePath()));

                return null;
            }
        }.getReturnValue();

    }

    private void setModuleProgress(final String status, final int percent) {

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                if (status != null) {
                    UpdaterCoreGui.this.log(status);
                }
                UpdaterCoreGui.this.progressLogo.setProgress(percent / 100.0f);
                UpdaterCoreGui.this.panel.setModuleProgress(status, percent);

            }
        };

    }

}
