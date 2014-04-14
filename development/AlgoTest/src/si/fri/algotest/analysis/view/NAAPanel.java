/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.fri.algotest.analysis.view;

import java.awt.event.ActionListener;
import si.fri.algotest.entities.NameAndAbrev;

/**
 *
 * @author tomaz
 */
public class NAAPanel extends javax.swing.JPanel {
  
  private NameAndAbrev naa;

  ActionListener onChange;
  
  /**
   * Creates new form NAAPanel
   */
  public NAAPanel(String name, String abrev, ActionListener action) {
    initComponents();
    naa = new NameAndAbrev(name, abrev);
    nameCB.setText(name);
    abrevTF.setText(abrev);
    onChange = action;
  }

  public boolean isSelected() {
    return nameCB.isSelected();
  }
  
  public NameAndAbrev getNAA() {
    naa.setAbrev(abrevTF.getText());
    return naa;
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

    nameCB = new javax.swing.JCheckBox();
    jLabel1 = new javax.swing.JLabel();
    abrevTF = new javax.swing.JTextField();

    setLayout(new java.awt.GridBagLayout());

    nameCB.setText("jCheckBox1");
    nameCB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nameCBActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(nameCB, gridBagConstraints);

    jLabel1.setText("AS");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
    add(jLabel1, gridBagConstraints);

    abrevTF.setColumns(6);
    abrevTF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        abrevActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(abrevTF, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void nameCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameCBActionPerformed
    if (onChange != null) 
      onChange.actionPerformed(evt);
  }//GEN-LAST:event_nameCBActionPerformed

  private void abrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrevActionPerformed
    if (onChange != null) 
      onChange.actionPerformed(evt);
  }//GEN-LAST:event_abrevActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField abrevTF;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JCheckBox nameCB;
  // End of variables declaration//GEN-END:variables
}
