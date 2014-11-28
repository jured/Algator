/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.fri.algotest.analysis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.math.plot.Plot2DPanel;
import si.fri.algotest.analysis.DataAnalyser;
import si.fri.algotest.analysis.TableData;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class Analyser extends javax.swing.JFrame {

  SeriesSelect seriesSelect1;
  private Project project = null;

  Plot2DPanel plotPanel = null;
  
  /**
   * Creates new form Analyse
   */
  public Analyser(java.awt.Frame parent, boolean modal) {

    initComponents();
    
    ActionListener onSeriesSelectButtonChange = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        // ActionEvent has to be null (and not e) - see jButton4ActionPerformed for details
        jButton4ActionPerformed(null);
      }
    };
    
    seriesSelect1 = new SeriesSelect(onSeriesSelectButtonChange);
    
    JPanel xypanel = new JPanel(new BorderLayout());
    graphPanel.add(xypanel, BorderLayout.PAGE_END);
    
    JScrollPane scp = new JScrollPane(seriesSelect1);
    scp.setPreferredSize(new Dimension(100,90));
    xypanel.add(scp);

    Toolkit tk = Toolkit.getDefaultToolkit();
    setSize(tk.getScreenSize().width, tk.getScreenSize().width);
    
    queryComposer1.setOuterChangeListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	jButton4ActionPerformed(e);
      }      
    });
  }

  public Analyser(final Project project) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
	Analyser dialog = new Analyser(new javax.swing.JFrame(), true);
        dialog.setProject(project); 
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
  
  
  public void setProject(Project project) {
    if (project == null) return;
    
    this.project = project;
    
    setTitle(String.format("ALGator analyzer - [%s] ", project.getProject().getName()));
    queryComposer1.setProject(project);
  }
  
  

  
  void showTable() {
    if (project == null) return;
    EProject eProject = project.getProject();
    if (eProject != null) {
      String[] algs = eProject.getStringArray(EProject.ID_Algorithms);
      String[] tsts = eProject.getStringArray(EProject.ID_TestSets);


      String resDescFilename = ATGlobal.getRESULTDESCfilename(
	      eProject.getProjectRootDir(), eProject.getName(), MeasurementType.EM);
      EResultDescription eResultDesc = new EResultDescription(new File(resDescFilename));

      ArrayList<String> rezultati = new ArrayList<>();

      String html = "<html><table width=100% border=1>";

      String header = "";
      String[] order = eResultDesc.getStringArray(EResultDescription.ID_TestParOrder);
      for (int i = 0; i < order.length; i++) {
	header += "<th><font color=ff0000>" + order[i] + "</font></th>";
      }
      html += "<tr><th>Algorithm</th><th><font color=ff0000>Testset</font></th><th><font color=ff0000>Unique test ID</font></th><th><font color=ff0000>PASS</font></th>" 
              + header + " ";

      header = "";
      order = eResultDesc.getStringArray(EResultDescription.ID_ResultParOrder);
      for (int i = 0; i < order.length; i++) {
	header += "<th>" + order[i] + "</th>";
      }
      html += header + "</tr>";


      for (String alg : algs) {
	for (String tst : tsts) {
	  String resFileName = ATGlobal.getRESULTfilename(eProject.getProjectRootDir(), alg, tst, MeasurementType.EM);
	  File resFile = new File(resFileName);
	  if (resFile.exists()) {
	    try (Scanner sc = new Scanner(resFile)) {
	      while (sc.hasNextLine()) {
		rezultati.add(sc.nextLine());
	      }
	    } catch (Exception e) {
	      // kako obravnavam napako?
	    }
	  }
	}
      }

      for (String vrstica : rezultati) {
	String[] polja = vrstica.split(";");
	String hVrstica = "";
	for (int i = 0; i < polja.length; i++) {
	  hVrstica += String.format("<td>%s</td>\n", polja[i]);
	}
	if (!hVrstica.isEmpty()) {
	  html += "<tr>" + hVrstica + "</tr>";
	}
      }

      html += "</table></html>";

      htmlPane.setText(html.replaceAll("\"", ""));
    }
  }

  void showSelectResult(EProject project) {
    if (project != null) {
      String[] algs = project.getStringArray(EProject.ID_Algorithms); 
      String[] tsts = project.getStringArray(EProject.ID_TestSets);

      ParameterSet filter = new ParameterSet();
      EParameter f1 = new EParameter("Group", null, ParameterType.STRING, "SORTED");
      // EParameter f2 = new EParameter("N", null, ParameterType.INT, 10);
      filter.addParameter(f1, true);
      // filter.addParameter(f2);
      
      HashMap<String, ArrayList<ParameterSet>> algResults = DataAnalyser.readResultsFromFiles(project, algs, tsts);
      ArrayList<ParameterSet> results = DataAnalyser.selectData(algResults, null, filter, null, null);
      
      String resStr = "";
      for (int i = 0; i < results.size(); i++) {
	resStr += results.get(i).toString() + "<br>\n";
      }
      selectPane.setText(resStr);
    }
  }

  private double [] getDoubleArray(ArrayList<ArrayList<Object>> list, int col) {
    if (list == null || list.get(0).size()<col)
      return new double[0];
    
    double [] result = new double[list.size()];
    for (int i = 0; i < result.length; i++) {
      double value = 0;
      try {
	String sv = String.valueOf(list.get(i).get(col));
	value = Double.parseDouble(sv);
      } catch (Exception e) {
	value=0;
      }
      result[i] = value;
    }
    return result;
  } 
  
  /**
   * Draw y.length graphs with X-axis be the x-th column and y-axis the y[i] column of td. 
   */
  private void drawGraph(TableData td, JPanel outPanel, int xAxis, int[] yAxes) {
    if (td.data.size()==0 || td.data.get(0).size()<2)
      return;
    
    if (plotPanel!=null) {
      outPanel.remove(plotPanel);
    }
    
    plotPanel = new Plot2DPanel();
    double[] x = getDoubleArray(td.data, xAxis);
    
    for (int i = 0; i < yAxes.length; i++) {
      double[] y = getDoubleArray(td.data, yAxes[i]);

      plotPanel.addLinePlot((String) td.header.get(yAxes[i]), x, y);
    }
    
    plotPanel.addLegend("SOUTH");
    
    outPanel.add(plotPanel);
    jSplitPane2.revalidate();
  }
  
  
  void saveQuery() {
    if (project==null) return;
    JFileChooser jfc = new JFileChooser();
    
    String queryRoot = ATGlobal.getQUERIESroot(project.getProject().getProjectRootDir());
    
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
          pw.println(queryComposer1.getQuery().toJSONString(true));
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
    
    String queryRoot = ATGlobal.getQUERIESroot(project.getProject().getProjectRootDir());
    
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
        queryComposer1.setQuery(query);
        jButton4ActionPerformed(new ActionEvent(new JCheckBox(), 0, "Re-run graph"));
      }
    }
  }
  
  public void run(ActionEvent evt) {
    EQuery query = queryComposer1.getQuery();
    System.out.println(query.toJSONString());

    TableData td = DataAnalyser.runQuery(project.getProject(), query);
    if (td==null) return;

    // this action is triggered by many events; to prevent changing the contenet 
    // if seriesSelect panel, addFields is callsed only  when CheckBox is the trigger 
    // (one of the Fields is chenged)
    if (evt !=null && evt.getSource() instanceof JCheckBox)
      seriesSelect1.addFields(td.header, 1);
    
    DefaultTableModel dtm = new DefaultTableModel(td.getDataAsArray(), td.header.toArray());
    dataTable.setModel(dtm);
    
    DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
    headerRenderer.setBackground(new Color(239, 198, 46));

    for (int i = 0; i < dataTable.getModel().getColumnCount(); i++) {
      if (i<td.numberOfInputParameters)
        dataTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
    }

    if (td.data != null && td.data.size() > 0 && td.data.get(0).size() >= 2)
    drawGraph(td, graphPanel, seriesSelect1.getXFieldID(), seriesSelect1.getYFieldsID());
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
    jPanel4 = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    jButton2 = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    htmlPane = new javax.swing.JEditorPane();
    jPanel5 = new javax.swing.JPanel();
    jPanel6 = new javax.swing.JPanel();
    jPanel7 = new javax.swing.JPanel();
    jButton3 = new javax.swing.JButton();
    jPanel8 = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    selectPane = new javax.swing.JEditorPane();
    jPanel9 = new javax.swing.JPanel();
    jSplitPane2 = new javax.swing.JSplitPane();
    jSplitPane1 = new javax.swing.JSplitPane();
    qPanel = new javax.swing.JPanel();
    queryComposer1 = new si.fri.algotest.analysis.view.QueryComposer();
    jButton4 = new javax.swing.JButton();
    jScrollPane3 = new javax.swing.JScrollPane();
    dataTable = new javax.swing.JTable();
    jPanel13 = new javax.swing.JPanel();
    graphPanel = new javax.swing.JPanel();
    jMenuBar1 = new javax.swing.JMenuBar();
    jMenu1 = new javax.swing.JMenu();
    jMenuItem1 = new javax.swing.JMenuItem();
    jMenuItem2 = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    jMenuItem3 = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new java.awt.GridBagLayout());

    jPanel4.setLayout(new java.awt.GridBagLayout());

    jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jButton2.setText("Show table");
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });
    jPanel2.add(jButton2);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    jPanel4.add(jPanel2, gridBagConstraints);

    jPanel3.setLayout(new java.awt.BorderLayout());

    htmlPane.setContentType("text/html"); // NOI18N
    jScrollPane1.setViewportView(htmlPane);

    jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jPanel4.add(jPanel3, gridBagConstraints);

    jTabbedPane1.addTab("Simple table", jPanel4);

    jPanel5.setLayout(new java.awt.BorderLayout());

    jPanel6.setLayout(new java.awt.GridBagLayout());

    jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jButton3.setText("Show query result");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });
    jPanel7.add(jButton3);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    jPanel6.add(jPanel7, gridBagConstraints);

    jPanel8.setLayout(new java.awt.BorderLayout());

    selectPane.setContentType("text/html"); // NOI18N
    jScrollPane2.setViewportView(selectPane);

    jPanel8.add(jScrollPane2, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jPanel6.add(jPanel8, gridBagConstraints);

    jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

    jTabbedPane1.addTab("SimpleQuery", jPanel5);

    jPanel9.setLayout(new java.awt.GridBagLayout());

    jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

    qPanel.setMinimumSize(new java.awt.Dimension(650, 400));
    qPanel.setPreferredSize(new java.awt.Dimension(500, 400));
    qPanel.setLayout(new java.awt.GridBagLayout());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 338;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    qPanel.add(queryComposer1, gridBagConstraints);

    jButton4.setText("Run!");
    jButton4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton4ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    qPanel.add(jButton4, gridBagConstraints);

    jSplitPane1.setLeftComponent(qPanel);

    dataTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    jScrollPane3.setViewportView(dataTable);

    jSplitPane1.setRightComponent(jScrollPane3);

    jSplitPane2.setLeftComponent(jSplitPane1);

    jPanel13.setLayout(new java.awt.BorderLayout());

    graphPanel.setLayout(new java.awt.BorderLayout());
    jPanel13.add(graphPanel, java.awt.BorderLayout.CENTER);

    jSplitPane2.setRightComponent(jPanel13);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jPanel9.add(jSplitPane2, gridBagConstraints);

    jTabbedPane1.addTab("Query", jPanel9);

    jTabbedPane1.setSelectedIndex(2);

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

  private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    run(evt);
  }//GEN-LAST:event_jButton4ActionPerformed

  private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    showSelectResult(project.getProject());
  }//GEN-LAST:event_jButton3ActionPerformed

  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    showTable();
  }//GEN-LAST:event_jButton2ActionPerformed

  private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
    System.exit(0);
  }//GEN-LAST:event_jMenuItem3ActionPerformed

  private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    saveQuery();
  }//GEN-LAST:event_jMenuItem2ActionPerformed

  private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    openQuery();
  }//GEN-LAST:event_jMenuItem1ActionPerformed

  
  
  
  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    if (ATGlobal.ALGatorDataRoot == null || ATGlobal.ALGatorDataRoot.isEmpty())
      ATGlobal.ALGatorDataRoot = "/Users/Tomaz/Dropbox/FRI/ALGator/data_root/"; 
    
    String projectName = "Sorting"; 
    final Project project = new Project(ATGlobal.ALGatorDataRoot, projectName);
            
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
	if ("Nimbus".equals(info.getName())) {
	  javax.swing.UIManager.setLookAndFeel(info.getClassName());
	  break;
	}
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(Analyser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(Analyser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(Analyser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(Analyser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
	Analyser dialog = new Analyser(new javax.swing.JFrame(), true);
        dialog.setProject(project); 
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
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTable dataTable;
  private javax.swing.JPanel graphPanel;
  private javax.swing.JEditorPane htmlPane;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JButton jButton4;
  private javax.swing.JMenu jMenu1;
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JMenuItem jMenuItem2;
  private javax.swing.JMenuItem jMenuItem3;
  private javax.swing.JPanel jPanel13;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JPanel jPanel7;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JPanel jPanel9;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JSplitPane jSplitPane2;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JPanel qPanel;
  private si.fri.algotest.analysis.view.QueryComposer queryComposer1;
  private javax.swing.JEditorPane selectPane;
  // End of variables declaration//GEN-END:variables
}
