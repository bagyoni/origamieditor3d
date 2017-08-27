package origamieditor3d.ui;

import java.awt.Desktop;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import origamieditor3d.resources.Constants;
import origamieditor3d.resources.Dictionary;
import java.net.URL;
import javax.imageio.ImageIO;
import java.util.Scanner;
import java.net.URI;
import javax.swing.JTextArea;
import java.io.File;

public class DialogManager {

    final private OrigamiEditorUI associated_ui;
    
    final private JFileChooser file_dialog;
    
    public DialogManager(OrigamiEditorUI ui) {
        
        associated_ui = ui;
        file_dialog = new JFileChooser();
    }
    
    public boolean canICloseFile() {
        
        Object[] options = { Dictionary.getString("dialog.unsaved.close"), Dictionary.getString("dialog.unsaved.dontclose") };
        
        if (JOptionPane.showOptionDialog(associated_ui, Dictionary.getString("dialog.unsaved"),
                Dictionary.getString("question"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1])
                == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }
    
    private boolean canIOverwriteFile() {
        
        if (JOptionPane.showConfirmDialog(null, Dictionary.getString("overwrite"),
                Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION)
                == javax.swing.JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }
    
    public String getSaveFilePath(String fileExtension) {
        
        file_dialog.setSelectedFile(new File(""));
        file_dialog.resetChoosableFileFilters();
        file_dialog.setAcceptAllFileFilterUsed(false);
        file_dialog.addChoosableFileFilter(new FileNameExtensionFilter(Dictionary.getString(fileExtension), fileExtension));

        if (file_dialog.showSaveDialog(associated_ui) == javax.swing.JFileChooser.APPROVE_OPTION) {
            if (file_dialog.getFileFilter() == file_dialog.getChoosableFileFilters()[0]) {
                
                String fpath = file_dialog.getSelectedFile().getPath();
                if (!fpath.endsWith(fileExtension)) {
                    fpath += "." + fileExtension;
                }
                
                if (new File(fpath).exists()) {
                    
                    if (canIOverwriteFile()) {
                        return fpath;
                    }
                    return getSaveFilePath(fileExtension);
                }
                return fpath;
            }
        }
        return null;
    }
    
    public String getOpenFilePath(String... fileExtensions) {
        
        file_dialog.setSelectedFile(new File(""));
        file_dialog.resetChoosableFileFilters();
        file_dialog.setAcceptAllFileFilterUsed(false);
        for (String ext : fileExtensions) {
            file_dialog.addChoosableFileFilter(new FileNameExtensionFilter(Dictionary.getString(ext), ext));
        }
        
        if (file_dialog.showOpenDialog(associated_ui) == javax.swing.JFileChooser.APPROVE_OPTION) {
            String fpath = file_dialog.getSelectedFile().getPath();
            return fpath;
        }
        return null;
    }
    
    public String getOpenImagePath() {
        
        file_dialog.setSelectedFile(new File(""));
        file_dialog.resetChoosableFileFilters();
        file_dialog.setAcceptAllFileFilterUsed(false);
        file_dialog.addChoosableFileFilter(new FileNameExtensionFilter(Dictionary.getString("img"), ImageIO.getReaderFormatNames()));
        
        if (file_dialog.showOpenDialog(associated_ui) == javax.swing.JFileChooser.APPROVE_OPTION) {
            if (file_dialog.getFileFilter() == file_dialog.getChoosableFileFilters()[0]) {
                
                String fpath = file_dialog.getSelectedFile().getPath();
                return fpath;
            }
        }
        return null;
    }
    
    public void lookForUpdate() {
        
        try {
            
            Scanner inf = new Scanner(
                    new URL(Constants.INFO_LINK).openStream());
            String line;
            while (!(line = inf.nextLine().replace(" ", "")).startsWith("latest_version="))
                ;
            String ver = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            
            if (!Constants.VERSION.equals(ver)) {
                
                Object[] options = { Dictionary.getString("yes"), Dictionary.getString("no") };
                if (JOptionPane.showOptionDialog(associated_ui, Dictionary.getString("update"),
                        Dictionary.getString("question"), javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE, null, options,
                        options[0]) == javax.swing.JOptionPane.YES_OPTION) {
                    
                    inf.reset();
                    while (!(line = inf.nextLine().replace(" ", "")).startsWith("download_link="))
                        ;
                    String dl_url = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                    
                    if (Desktop.isDesktopSupported() 
                            ? Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
                            : false) {
                        
                        Desktop.getDesktop().browse(new URI(dl_url));
                        System.exit(0);
                    }
                    else {
                        
                        JTextArea copyable =
                                new JTextArea(Dictionary.getString("browser-fail", dl_url));
                        copyable.setEditable(false);
                        JOptionPane.showMessageDialog(
                                associated_ui, copyable, Dictionary.getString("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            inf.close();
        }
        catch (Exception ex) {
        }
    }
}
