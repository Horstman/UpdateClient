package org.appwork.update.updateclient.translation;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.TranslateInterface;

@Defaults(lngs = { "en", "de" })
public interface UpdateTranslation extends TranslateInterface {

    @Default(lngs = { "en", "de" }, values = { "%s2 %s1-Edition does not exist any more.\r\n                  Resetted to latest stable edition.", "%s2 %s1-Edition existiert nicht mehr.\r\n                  Zurückgesetzt auf aktuelle Hauptversion." })
    String branch_resetted(String string, String string2);

    @Default(lngs = { "en", "de" }, values = { "Using %s1-Edition", "Verwende %s1-Edition" })
    String branch_updated(String string);

    @Default(lngs = { "en", "de" }, values = { "Cancel update", "Update abbrechen" })
    String cancel_update();

    @Default(lngs = { "en", "de" }, values = { "Updater will update itself now.", "Updater wird sich nun selbst aktualisieren" })
    String clientUpdate();

    @Default(lngs = { "en", "de" }, values = { "Updateclient is outdated!", "Updater ist nicht aktuell!" })
    String clientupdate_title();

    @Default(lngs = { "en", "de" }, values = { "Close", "Schließen" })
    String close();

    @Default(lngs = { "en", "de" }, values = { "Could not install file: %s1", "Datei konnte nicht installiert werden: %s1" })
    String could_not_install_file(String absolutePath);

    @Default(lngs = { "en", "de" }, values = { "Could not overwrite file %s1.", "Datei %s1 kann nicht überschrieben werden." })
    String could_not_overwrite(String absolutePath);

    @Default(lngs = { "en", "de" }, values = { "Edition oudated", "Version veraltet" })
    String dialag_branch_resetted();

    @Default(lngs = { "en", "de" }, values = { "%s2 %s1-Edition does not exist any more.\r\nResetted to latest stable edition.", "%s2 %s1-Edition existiert nicht mehr.\r\nZurückgesetzt auf aktuelle Hauptversion." })
    String dialog_branch_resetted_msg(String string, String string2);

    @Default(lngs = { "en", "de" }, values = { "Really cancel update?", "Update wirklich abbrechen?" })
    String dialog_rly_cancel();

    @Default(lngs = { "en", "de" }, values = { "%s1 Updates found! Download now?\r\n\r\nYou can decide afterwards when to install these updates.", "%s1 Updates gefunden! Jetzt herunterladen?\r\n\r\nSie können anschließend entscheiden wann diese Updates instaliert werden." })
    String downloadUpdatesNow(int size);

    @Default(lngs = { "en", "de" }, values = { "The '%s1'-Edition cannot be updated right now. Please try again later!", "Die '%s1' Edition kann momentan nicht aktualisiert werden. Bitte später versuchen." })
    String error_invalid_branch(String name);

    @Default(lngs = { "en", "de" }, values = { "Error occured", "Ein Fehler ist aufgetreten" })
    String error_occured();

    @Default(lngs = { "en", "de" }, values = { "Could not finish update.", "Update konnte nicht abgeschlossen werden." })
    String error_occured_detailed();

    @Default(lngs = { "en", "de" }, values = { "Update has been interrupted. Reverting changes...", "Update wurde unterbrochen. Änderungen werden rückgängig gemacht." })
    String error_occured_start_reverting();

    @Default(lngs = { "en", "de" }, values = { "Cannot update %s1. \r\n                  Try again later!", "Kann %s1 nicht aktualisieren. \r\n                  Bitte später erneut versuchen." })
    String error_unknown_app(String appID);

    @Default(lngs = { "en", "de" }, values = { "Unknown Edition: %s1", "Unbekannte Version: %s1" })
    String error_unknown_branch(String string);

    @Default(lngs = { "en", "de" }, values = { "Unknown Updateserver problems.\r\nPlease try again later", "Unbekannte Updateprobleme.\r\n                  Bitte versuchen Sie es später erneut." })
    String error_unknown_server();

    @Default(lngs = { "en", "de" }, values = { "%s1", "%s1" })
    String exception_msg(String message);

    @Default(lngs = { "en", "de" }, values = { "Unexpected Error", "Unerwarteter Fehler" })
    String exception_title();

    @Default(lngs = { "en", "de" }, values = { "Close", "Schließen" })
    String exit();

    @Default(lngs = { "en", "de" }, values = { "Uninstalled file: %s1", "Datei deinstalliert: %s1" })
    String filelog_deletedfile(String string);

    @Default(lngs = { "en", "de" }, values = { "Installed file %s1", "Datei installiert: %s1" })
    String filelog_installedfile(String absolutePath);

    @Default(lngs = { "en", "de" }, values = { "Updating %s1", "%s1 aktualisieren...." })
    String getPanelTitle(String app);

    @Default(lngs = { "en", "de" }, values = { "Canceled by user", "Vom Benutzer abgebrochen" })
    String interrupted();

    @Default(lngs = { "en", "de" }, values = { "Canceled", "Abgebrochen" })
    String interrupted_title();

    @Default(lngs = { "en", "de" }, values = { "Cannot Update. %s1 is still running.", "Kann nicht aktualisieren. %s1 läuft noch." })
    String locked(String appID);

    @Default(lngs = { "en", "de" }, values = { "Could not install update.\r\nIt seems like %s1 is still running.\r\nPlease close %s1 main application.", "Konnte Update nicht installieren. Es scheint als würde %s1 noch laufen.\r\nBitte beenden Sie die %s1 Hauptanwendung" })
    String locked_dialog_msg(String appID);

    @Default(lngs = { "en", "de" }, values = { "Close Main Application", "Hauptanwendung beenden" })
    String locked_dialog_title();

    @Default(lngs = { "en", "de" }, values = { "Found: %s2 file(s) to download, %s1 file(s) to install & %s3 oudated file(s)", "Gefunden: %s2 Datei(en) herunterladen, %s1 Datei(en) installieren & %s3 alte Datei(en) entfernen." })
    String log_x_files_to_update_found(int uninstalled, int updates, int remove);

    @Default(lngs = { "en", "de" }, values = { "You already run the latest Version", "Sie haben bereits die neuste Version" })
    String log_you_are_up2date();

    @Default(lngs = { "en", "de" }, values = { "Please wait!", "Bitte warten!" })
    String please_wait();

    @Default(lngs = { "en", "de" }, values = { "Please wait until updater finished his job!", "Bitte warten bis der Updater fertig ist." })
    String please_wait_until_update_finished();

    @Default(lngs = { "en", "de" }, values = { "Updates available!", "Updates verfügbar!" })
    String readyToDownloadUpdates(int size);

    @Default(lngs = { "en", "de" }, values = { "Download %s1 Updates now?", "Jetzt %s1 Updates herunterladen?" })
    String readyToDownloadUpdatesDetailed(int size);

    @Default(lngs = { "en", "de" }, values = { "Install %s1 file(s) & folder(s)?", "%s1 Datei(en) & Ordner installieren?" })
    String readyToInstallFiles(int installedFiles);

    @Default(lngs = { "en", "de" }, values = { "Unexpected redirection to %s1!", "Unerwartete Weiterleitung auf %s1!" })
    String redirect_error(String responseHeader);

    @Default(lngs = { "en", "de" }, values = { "Update has been interrupted.", "Update wurde unterbrochen." })
    String reverting_msg();

    @Default(lngs = { "en", "de" }, values = { "Reverting...", "Zurücksetzen..." })
    String reverting_title();

    @Default(lngs = { "en", "de" }, values = { "Start %s1", "%s1 starten" })
    String start_jd(String app);

    @Default(lngs = { "en", "de" }, values = { "Updates available!", "Updates verfügbar!" })
    String udpates_found();

    @Default(lngs = { "en", "de" }, values = { "Unexpected HTTP Error (%s1)", "Unerwarteter HTTP Fehler (%s1)" })
    String unexpected_http_error(int code);

    @Default(lngs = { "en", "de" }, values = { "Uninstalled file(s): %s1", "Nicht installierte Datei(en): %s1" })
    String uninstalledfiles(int size);

    @Default(lngs = { "en", "de" }, values = { "No Internet connection to updateserver: %s1", "Keine Internetverbindung zum Updateserver: %s1" })
    String UpdateException_socket(String message);

    @Default(lngs = { "en", "de" }, values = { "Updatelog", "Update Log:" })
    String UpdateServer_UpdaterGui_layoutGUI_details();

    @Default(lngs = { "en", "de" }, values = { "An error occured: %s1", "Fehler aufgetreten: %s1" })
    String UpdateServer_UpdaterGui_onException_error_occured(String errormessage);

    @Default(lngs = { "en", "de" }, values = { "Failed: %s1", "Fehler: %s1" })
    String UpdateServer_UpdaterGui_onException_error_occured_bartext(String bartest);

    @Default(lngs = { "en", "de" }, values = { "Please try again later.", "Bitte warten." })
    String UpdateServer_UpdaterGui_onServiceNotAvailable_bar();

    @Default(lngs = { "en", "de" }, values = { "Updateserver busy. Please wait or try later.", "Updateserver sind überlastet. Bitte warten." })
    String UpdateServer_UpdaterGui_onServiceNotAvailable_wait();

    @Default(lngs = { "en", "de" }, values = { "Find latest version", "Suche neuste Version" })
    String UpdateServer_UpdaterGui_onStateChange_branchlist();

    @Default(lngs = { "en", "de" }, values = { "Download", "Download" })
    String UpdateServer_UpdaterGui_onStateChange_download();

    @Default(lngs = { "en", "de" }, values = { "Extract Updatepackage", "Entpacke Updatepaket" })
    String UpdateServer_UpdaterGui_onStateChange_extract();

    @Default(lngs = { "en", "de" }, values = { "Update failed", "Update fehlgeschlagen" })
    String UpdateServer_UpdaterGui_onStateChange_failed();

    @Default(lngs = { "en", "de" }, values = { "%s1", "%s1" })
    String UpdateServer_UpdaterGui_onStateChange_failed2(String message);

    @Default(lngs = { "en", "de" }, values = { "Find updates", "Suche nach Updates" })
    String UpdateServer_UpdaterGui_onStateChange_filter();

    @Default(lngs = { "en", "de" }, values = { "Contact Updateserver", "Kontaktiere Updateserver" })
    String UpdateServer_UpdaterGui_onStateChange_hashlist();

    @Default(lngs = { "en", "de" }, values = { "Installing", "Installiere Updates" })
    String UpdateServer_UpdaterGui_onStateChange_install();

    @Default(lngs = { "en", "de" }, values = { "Please close %s1 to continue", "Bitte beenden Sie %s1 um fortzufahren" })
    String UpdateServer_UpdaterGui_onStateChange_locked(String appid);

    @Default(lngs = { "en", "de" }, values = { "Create Updatepackage", "Erstelle Updatepaket" })
    String UpdateServer_UpdaterGui_onStateChange_package();

    @Default(lngs = { "en", "de" }, values = { "Successful", "Erfolgreich" })
    String UpdateServer_UpdaterGui_onStateChange_successful();

    @Default(lngs = { "en", "de" }, values = { "Update %s1 | %s2-Edition", "Aktualisiere %s1 | %s2-Edition" })
    String UpdateServer_UpdaterGui_onUpdaterEvent_branch(String app, String branch);

    @Default(lngs = { "en", "de" }, values = { "Removed %s1", "Entfernt: %s1" })
    String UpdateServer_UpdaterGui_onUpdaterEvent_remove(String path);

    @Default(lngs = { "en", "de" }, values = { "%s1 update(s) found!", "%s1 Update(s) gefunden" })
    String UpdateServer_UpdaterGui_onUpdaterModuleEnd_end_filtering(int num);

    @Default(lngs = { "en", "de" }, values = { "Download %s1", "Lade %s1 herunter" })
    String UpdateServer_UpdaterGui_onUpdaterModuleStart_download(String filesize);

    @Default(lngs = { "en", "de" }, values = { "Finished - No Updates", "Fertig - keine Updates!" })
    String UpdateServer_UpdaterGui_runInEDT_finished();

    @Default(lngs = { "en", "de" }, values = { "Updateserver busy.", "Updateserver überlastet." })
    String UpdateServer_UpdaterGui_runInEDT_mainbar();

    @Default(lngs = { "en", "de" }, values = { "You already have the latest version!", "Sie haben bereits die aktuellste Version" })
    String UpdateServer_UpdaterGui_runInEDT_noupdates();

    @Default(lngs = { "en", "de" }, values = { "Successful!", "Erfolgreich!" })
    String UpdateServer_UpdaterGui_runInEDT_successful();

    @Default(lngs = { "en", "de" }, values = { "Updated %s1 file(s)", "%s1 Datei(en) aktualisiert" })
    String UpdateServer_UpdaterGui_runInEDT_updates(int size);

    @Default(lngs = { "en", "de" }, values = { "Install path: %s1", "Installationsordner: %s1" })
    String UpdateServer_UpdaterGui_UpdaterGui_path(String path);

    @Default(lngs = { "en", "de" }, values = { "Started Update: %s1", "Updater gestarted: %s1" })
    String UpdateServer_UpdaterGui_UpdaterGui_started(String appid);

    @Default(lngs = { "en", "de" }, values = { "User interrupted Update!", "Benutzer hat Update unterbrochen!" })
    String userinterrupted();

    @Default(lngs = { "en", "de" }, values = { "No rights to write to %s1. ", "Nicht genug Rechte um nach %s1 zu schreiben." })
    String virtual_file_system_detected(String installDirFile);

}