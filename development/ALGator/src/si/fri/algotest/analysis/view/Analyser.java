package si.fri.algotest.analysis.view;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.Project;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class Analyser extends javax.swing.JFrame {

  private Project project = null;
    
  int lastQueryNumber = 1;
  
  boolean izPrograma = true;
  
  ArrayList<QueryAndGraphPanel> queryAndGraphPanels;
  
  String computerID; // the ID of computer; the results are in computerID folder
  
  /**
   * Creates new form Analyse
   */
  public Analyser(java.awt.Frame parent, boolean modal, String computerID) {

    initComponents();
    queryAndGraphPanels = new ArrayList<QueryAndGraphPanel>();
    
    QueryAndGraphPanel queryAndGraphpanel = new QueryAndGraphPanel(computerID);
    jPanel9.add(queryAndGraphpanel);
    queryAndGraphPanels.add(queryAndGraphpanel);
    
    setSize(Toolkit.getDefaultToolkit().getScreenSize());
    
    izPrograma = false;
  }

  public Analyser(final Project project, final String computerID) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
	Analyser dialog = new Analyser(new javax.swing.JFrame(), true, computerID);
        dialog.setProject(project, computerID); 
	dialog.addWindowListener(new java.awt.event.WindowAdapter() {
	  @Override
	  public void windowClosing(java.awt.event.WindowEvent e) {
	    System.exit(0);
	  }
	});
	dialog.setVisible(true);
      }
    });
  }
  
  
  public void setProject(Project project, String computerID) {
    if (project == null) return;
    
    this.project = project;
    this.computerID = computerID;
    
    setTitle(String.format("ALGator analyzer - [%s] ", project.getEProject().getName()));
    
    getCurrentQAG().setProject(project);
  }
  
  private QueryAndGraphPanel getCurrentQAG() {
    int index = jTabbedPane1.getSelectedIndex();
    return queryAndGraphPanels.get(index);
  }
  
  
  private void addNewQueryTab() {
    if (jTabbedPane1.getSelectedIndex() == jTabbedPane1.getTabCount()-1) {
      String queryName = "Query" + (++lastQueryNumber);
      int tabCount = jTabbedPane1.getTabCount();
      
      QueryAndGraphPanel qagp = new QueryAndGraphPanel(computerID);
      queryAndGraphPanels.add(qagp);
      qagp.setProject(project);
      
      JPanel nov = new JPanel(new BorderLayout());
      nov.add(qagp);
      
      jTabbedPane1.insertTab(queryName, null, nov, queryName, tabCount-1);
      jTabbedPane1.setSelectedIndex(tabCount-1);
    }
  }
  
  
  
  void saveQuery() {
    if (project==null) return;
    JFileChooser jfc = new JFileChooser();
    
    String queryRoot = ATGlobal.getQUERIESroot(project.getEProject().getProjectRootDir());
    
    jfc.setCurrentDirectory(new File(queryRoot));
    jfc.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.getAbsolutePath().endsWith(ATGlobal.AT_FILEEXT_query);
      }

      @Override
      public String getDescription() {
        return "ALGator query (*."+ATGlobal.AT_FILEEXT_query+")";
      }
    });
    
    int ans = jfc.showSaveDialog(this);
    if (ans == JFileChooser.APPROVE_OPTION) {
      File fileToSave = jfc.getSelectedFile();
      if (!fileToSave.getPath().endsWith("." + ATGlobal.AT_FILEEXT_query))
        fileToSave = new File(fileToSave.getPath() + "." + ATGlobal.AT_FILEEXT_query);
      
      String fileMsg = String.format("File %s exists. Overwrite?", fileToSave.getPath());
      boolean save = !fileToSave.exists() || 
        (JOptionPane.showConfirmDialog(this, fileMsg, "Save query warning", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION);
      
      if (save) {
        
        try {
          PrintWriter pw = new PrintWriter(fileToSave);
          pw.println(getCurrentQAG().getQuery().toJSONString(true));
          pw.close();
        } catch (FileNotFoundException ex) {
          JOptionPane.showMessageDialog(this, ex.toString(), "Save query error", JOptionPane.OK_OPTION);
        }
      }
    }
  }
  
  void openQuery() {
    if (project==null) return;
    JFileChooser jfc = new JFileChooser();
    
    String queryRoot = ATGlobal.getQUERIESroot(project.getEProject().getProjectRootDir());
    
    jfc.setCurrentDirectory(new File(queryRoot));
    jfc.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.getAbsolutePath().endsWith(ATGlobal.AT_FILEEXT_query);
      }

      @Override
      public String getDescription() {
        return "ALGator query (*."+ATGlobal.AT_FILEEXT_query+")";
      }
    });
    
    int ans = jfc.showOpenDialog(this);
    if (ans == JFileChooser.APPROVE_OPTION) {
      File fileToOpen = jfc.getSelectedFile();
      EQuery query = new EQuery();
      query.initFromFile(fileToOpen);
      if (ErrorStatus.getLastErrorStatus() == ErrorStatus.STATUS_OK) {
        getCurrentQAG().setQuery(query);
        getCurrentQAG().run(new ActionEvent(new JCheckBox(), 0, "Re-run graph"));
      }
    }
  }
  
 
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel9 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    jMenuBar1 = new javax.swing.JMenuBar();
    jMenu1 = new javax.swing.JMenu();
    jMenuItem1 = new javax.swing.JMenuItem();
    jMenuItem2 = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    jMenuItem3 = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new java.awt.GridBagLayout());

    jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        jTabbedPane1StateChanged(evt);
      }
    });

    jPanel9.setLayout(new java.awt.BorderLayout());
    jTabbedPane1.addTab("Query1", jPanel9);
    jTabbedPane1.addTab("+", jPanel1);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(jTabbedPane1, gridBagConstraints);

    jMenu1.setText("File");

    jMenuItem1.setText("Open query...");
    jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuItem1ActionPerformed(evt);
      }
    });
    jMenu1.add(jMenuItem1);

    jMenuItem2.setText("Save query...");
    jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuItem2ActionPerformed(evt);
      }
    });
    jMenu1.add(jMenuItem2);
    jMenu1.add(jSeparator1);

    jMenuItem3.setText("Quit");
    jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuItem3ActionPerformed(evt);
      }
    });
    jMenu1.add(jMenuItem3);

    jMenuBar1.add(jMenu1);

    setJMenuBar(jMenuBar1);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
    System.exit(0);
  }//GEN-LAST:event_jMenuItem3ActionPerformed

  private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    saveQuery();
  }//GEN-LAST:event_jMenuItem2ActionPerformed

  private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    openQuery();
  }//GEN-LAST:event_jMenuItem1ActionPerformed

  private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
    if (!izPrograma) {
      izPrograma = true;
      addNewQueryTab();
      izPrograma = false;
    }
  }//GEN-LAST:event_jTabbedPane1StateChanged

  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenu jMenu1;
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JMenuItem jMenuItem2;
  private javax.swing.JMenuItem jMenuItem3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel9;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JTabbedPane jTabbedPane1;
  // End of variables declaration//GEN-END:variables
}
