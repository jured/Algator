/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.fri.algotest.analysis.view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.NameAndAbrev;
import si.fri.algotest.entities.Project;

/**
 *
 * @author tomaz
 */
public class QueryComposer extends javax.swing.JPanel {

  Project project;
  
  NAAPanel [] algNAAs, tstsNAAs, infieldNAAs, outfieldNAAs;
  
  ActionListener outerChangeListener, innerChangeListner;
  

  /**
   * Creates new form QueryComposer
   */
  public QueryComposer() {
    initComponents();
    
    innerChangeListner= new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
	dataChanged();
	if (outerChangeListener != null)
	outerChangeListener.actionPerformed(e);
      }
    };    
  }

  public void setProject(Project project) {
    this.project = project;
    
    setAlgsAndFields();
  }
  
  public void setOuterChangeListener(ActionListener action) {
    ActionListener a;
    this.outerChangeListener = action;
  }
  
  // poslusam spremembe na panelih in ukrepam (primer: če se dodajo nova polja
  // med filde, moram spremeniti sortby panel)
  void dataChanged() {
  }
  
  private NAAPanel [] createNAAPanels(String [] names, JPanel panel) {
    JPanel p = new JPanel(new GridLayout(100, 1));
    JScrollPane jsp = new JScrollPane(p);
    panel.add(jsp);
    
    NAAPanel [] result = new NAAPanel[names.length];
    if (names == null) return result;
    
    for(int i=0; i<names.length;i++) {
      result[i] = new NAAPanel(names[i], names[i], innerChangeListner);
      p.add(result[i]);
    }
    return result;
  }

  private String [] getNAAsAsStringArray(NAAPanel [] naaPanel) {
    int count = 0;
    for (int i = 0; i < naaPanel.length; i++) {
      if (naaPanel[i].isSelected()) count++;
    }
    String [] result = new String[count];
    int k=0;
    for (int i = 0; i < naaPanel.length; i++) {
      if (!naaPanel[i].isSelected()) continue;
     
      result[k++] = naaPanel[i].getNAA().toString();
    }
    
    return result;
  }
  
  private void setNAAPanelValues(NameAndAbrev[] naas, NAAPanel [] naaPanel) {
    for (int i = 0; i < naaPanel.length; i++) {
      naaPanel[i].setSelected(false);
    }
    for (NameAndAbrev naa : naas) {
      for (NAAPanel naaPanel1 : naaPanel) {
        if (naaPanel1.correspondsTo(naa)) {
          naaPanel1.setValues(naa, true);
          break;
        }
      }
    }
  }
  
  private void setAlgsAndFields() {
    if (project == null) return;
    
    EProject eProject = project.getProject();
    if (eProject == null) return;
    
    String [] algs = eProject.getStringArray(EProject.ID_Algorithms);    
    String [] tsts = eProject.getStringArray(EProject.ID_TestSets);
      
    
    algNAAs = createNAAPanels(algs, algPanel);
    tstsNAAs = createNAAPanels(tsts, tstsPanel);

    infieldNAAs  = createNAAPanels(project.getTestParameters(),   infieldPanel); 
    outfieldNAAs = createNAAPanels(project.getResultParameters(), outfieldPanel);

  }
  
  public EQuery getQuery() {
    String [] algs = getNAAsAsStringArray(algNAAs);
    String [] tsts = getNAAsAsStringArray(tstsNAAs);
    String [] inf  = getNAAsAsStringArray(infieldNAAs);
    String [] outf = getNAAsAsStringArray(outfieldNAAs);
    
    // currently only one GropuBy, one filter and one sortby is suported
    
    return new EQuery(algs, tsts, inf, outf, groupbyTF.getText().split(" *\\| *"), 
                  filterTF.getText().split(" *\\| *"), sortbyTF.getText().split(" *\\| *"));
  }
  
  public void setQuery(EQuery query) {
    try {
      setNAAPanelValues(query.getNATabFromJSONArray(EQuery.ID_Algorithms), algNAAs);
      setNAAPanelValues(query.getNATabFromJSONArray(EQuery.ID_TestSets), tstsNAAs);
      setNAAPanelValues(query.getNATabFromJSONArray(EQuery.ID_inParameters), infieldNAAs);
      setNAAPanelValues(query.getNATabFromJSONArray(EQuery.ID_outParameters), outfieldNAAs);
    
    // currently only one GropuBy, one Filter and one SortBy is supported
      String[] gb = query.getStringArray(EQuery.ID_GroupBy);
      String qS = "";
      for (String q : gb) {
        qS += (qS.isEmpty() ? "" : " | ") + q;
      }
      groupbyTF.setText(qS);
  
      String[] ft = query.getStringArray(EQuery.ID_Filter);
      String fS = "";
      for (String f : ft) {
        fS += (fS.isEmpty() ? "" : " | ") + f;
      }
      filterTF.setText(fS);
      
      String[] sb = query.getStringArray(EQuery.ID_SortBy);
      String sS = "";
      for (String s : sb) {
        sS += (sS.isEmpty() ? "" : " | ") + s;
      }
      sortbyTF.setText(sS);
      
    } catch (Exception e) {}
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

    jPanel4 = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    jLabel6 = new javax.swing.JLabel();
    groupByPanel = new javax.swing.JPanel();
    groupbyTF = new javax.swing.JTextField();
    jLabel7 = new javax.swing.JLabel();
    groupByPanel1 = new javax.swing.JPanel();
    filterTF = new javax.swing.JTextField();
    jLabel8 = new javax.swing.JLabel();
    groupByPanel2 = new javax.swing.JPanel();
    sortbyTF = new javax.swing.JTextField();
    jLabel5 = new javax.swing.JLabel();
    jLabel9 = new javax.swing.JLabel();
    jLabel10 = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    algPanel = new javax.swing.JPanel();
    tstsPanel = new javax.swing.JPanel();
    infieldPanel = new javax.swing.JPanel();
    outfieldPanel = new javax.swing.JPanel();

    org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(0, 100, Short.MAX_VALUE)
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(0, 100, Short.MAX_VALUE)
    );

    setLayout(new java.awt.GridBagLayout());

    jPanel2.setLayout(new java.awt.GridBagLayout());

    jLabel6.setText("GroupBy");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    jPanel2.add(jLabel6, gridBagConstraints);

    groupByPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    groupByPanel.setLayout(new java.awt.BorderLayout());
    groupByPanel.add(groupbyTF, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
    jPanel2.add(groupByPanel, gridBagConstraints);

    jLabel7.setText("Filter");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    jPanel2.add(jLabel7, gridBagConstraints);

    groupByPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    groupByPanel1.setLayout(new java.awt.BorderLayout());
    groupByPanel1.add(filterTF, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
    jPanel2.add(groupByPanel1, gridBagConstraints);

    jLabel8.setText("SortBy");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    jPanel2.add(jLabel8, gridBagConstraints);

    groupByPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    groupByPanel2.setLayout(new java.awt.BorderLayout());
    groupByPanel2.add(sortbyTF, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
    jPanel2.add(groupByPanel2, gridBagConstraints);

    jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/info.png"))); // NOI18N
    jLabel5.setToolTipText("<html>\nTo enter more than one SortBy value, use the separator  |. Example: <br><br>\n\n&nbsp; &nbsp; N | QS.TMin\n</html>\n");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    jPanel2.add(jLabel5, gridBagConstraints);

    jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/info.png"))); // NOI18N
    jLabel9.setToolTipText("<html>\nTo enter more than one GroupBy, use the separator  |. Example: <br><br>\n\n&nbsp; &nbsp; N; Tmax:MAX; MIN | Group\n</html>\n");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    jPanel2.add(jLabel9, gridBagConstraints);

    jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/info.png"))); // NOI18N
    jLabel10.setToolTipText("<html>\nTo enter more than one filter, use the separator  |. Example: <br><br>\n\n&nbsp; &nbsp; N>=100  | Group == RND\n</html>\n");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    jPanel2.add(jLabel10, gridBagConstraints);

    jPanel1.setLayout(new java.awt.GridLayout(2, 2));

    algPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithms"));
    algPanel.setPreferredSize(new java.awt.Dimension(250, 34));
    algPanel.setLayout(new java.awt.BorderLayout());
    jPanel1.add(algPanel);

    tstsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Testsets"));
    tstsPanel.setLayout(new java.awt.BorderLayout());
    jPanel1.add(tstsPanel);

    infieldPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Input fields"));
    infieldPanel.setLayout(new java.awt.BorderLayout());
    jPanel1.add(infieldPanel);

    outfieldPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output fields"));
    outfieldPanel.setLayout(new java.awt.BorderLayout());
    jPanel1.add(outfieldPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jPanel2.add(jPanel1, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jPanel2, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel algPanel;
  private javax.swing.JTextField filterTF;
  private javax.swing.JPanel groupByPanel;
  private javax.swing.JPanel groupByPanel1;
  private javax.swing.JPanel groupByPanel2;
  private javax.swing.JTextField groupbyTF;
  private javax.swing.JPanel infieldPanel;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel outfieldPanel;
  private javax.swing.JTextField sortbyTF;
  private javax.swing.JPanel tstsPanel;
  // End of variables declaration//GEN-END:variables
}
