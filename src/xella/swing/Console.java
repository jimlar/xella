/*
 * Console.java
 *
 * Created on den 8 september 2001, 00:27
 */

package xella.swing;

/**
 *
 * @author  jimmy
 */
public class Console extends javax.swing.JPanel {

    /** Creates new form Console */
    public Console() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        
        setLayout(new java.awt.BorderLayout());
        
        jTextPane2.setEditable(false);
        jTextPane2.setBackground(java.awt.Color.lightGray);
        jScrollPane1.setViewportView(jTextPane2);
        
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables

}
