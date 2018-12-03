/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DeleteGUI.java
 *
 * Created on 1/Ago/2010, 22:44:01
 */

package pt.com.santos.pharma.db.gui;

import com.google.zxing.NotFoundException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import pt.com.santos.pharma.PharmaApp;

/**
 *
 * @author camon
 */
public class DeleteGUI extends javax.swing.JDialog {

    private Timer timer;
    private TimerTask task;
    private boolean scheduled;
    private Component comp;

    /** Creates new form DeleteGUI */
    public DeleteGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initMyComponents();
    }

    private void initMyComponents() {
        setResizable(false);

        addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {}

            public void windowClosing(WindowEvent e) {
                cancel();
                if (comp != null) {
                    remove(comp);
                }
            }

            public void windowClosed(WindowEvent e) {}

            public void windowIconified(WindowEvent e) {}

            public void windowDeiconified(WindowEvent e) {}

            public void windowActivated(WindowEvent e) {
                 PharmaApp app = PharmaApp.getApplication();
                try {
                    if ((comp = app.getPlayerVisualComponent()) != null) {
                        comp.setSize(new Dimension(320, 240));
                    } else {
                        JTextField tf = new JTextField("No Camera Available!");
                        tf.setHorizontalAlignment(JTextField.CENTER);
                        tf.setCaretColor(Color.white);
                        comp = tf;
                        comp.setSize(new Dimension(320, 230));
                        cancel();
                    }
                    comp.setLocation((getWidth() - 320) / 2 - 5, 0);
                    add(comp);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //System.out.println("BLEH!");
                pack();
            }

            public void windowDeactivated(WindowEvent e) {}
        });
        
        start();
    }

    public void start() {
        PharmaApp app = PharmaApp.getApplication();
        if (app.hasWebCam()) {
            autoRadioButton.setSelected(true);
            manualRadioButton.setSelected(false);
            schedule();
            resetFields(true);
        } else {
            autoRadioButton.setSelected(false);
            autoRadioButton.setEnabled(false);
            manualRadioButton.setSelected(true);
            cancel();
            resetFields(false);
        }
    }

    private void schedule() {
        if (!scheduled) {
            timer = new Timer();
            task = new TimerTask() {

                @Override
                public void run() {
                    synchronized (this) {
                        grabFrame();
                    }
                }
            };
            timer.schedule(task, 5000, 2000);
            scheduled = true;
        }
    }

    private void cancel() {
        if (timer != null) {
            timer.cancel();
        }
        scheduled = false;
    }
    
    private void grabFrame() {
        PharmaApp app = PharmaApp.getApplication();
        BufferedImage bi = app.grabFrame();
        try {
            String s = PharmaApp.getDecodeText(bi);
            long code = Long.parseLong(s);
            codeTextField.setText(s);
            autoComplete(code, null);
            cancel();
        } catch (InvalidParameterException ex) {
            Logger.getLogger(DeleteGUI.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (NotFoundException ex) {
            //Logger.getLogger(InsertGUI.class.getName())
              //      .log(Level.SEVERE, null, ex);
            codeTextField.setText("Not Found");
        }
    }
    
    private void autoComplete(long code, Date date) {
        autoComplete(code, date, true);
    }

    private void autoComplete(long code, Date date, boolean showModal) {
        try {
            PharmaApp app = PharmaApp.getApplication();
            ResultSet rs = app.select(code, date);
            if (rs.next()) {
                long barcode = rs.getLong(1);
                String name = rs.getString(2);
                codeTextField.setText("" + barcode);
                nameTextField.setText(name);
                if (date != null) {
                    Date d = rs.getDate(3);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String sd = sdf.format(d);
                    expTextField.setText(sd);
                    int units = rs.getInt(4);
                    unitsTextField.setText("" + units);
                } else {
                    expTextField.setText("");
                    unitsTextField.setText("");
                }
            } else {
                resetFields(true);
                if (showModal) {
                    JOptionPane.showMessageDialog(this, "Product not found",
                        "Pharma", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            app.closeStat();
        } catch (SQLException ex) {
            Logger.getLogger(InsertGUI.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private void resetFields(boolean auto) {
        codeTextField.setEditable(!auto);
        nameTextField.setEditable(false);
        unitsTextField.setEditable(false);
        codeTextField.setText("");
        nameTextField.setText("");
        expTextField.setText("");
        unitsTextField.setText("");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        codeTextField = new javax.swing.JTextField();
        manualRadioButton = new javax.swing.JRadioButton();
        separator = new javax.swing.JSeparator();
        autoRadioButton = new javax.swing.JRadioButton();
        deleteButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        expTextField = new javax.swing.JTextField();
        unitsTextField = new javax.swing.JTextField();
        codeLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        expLabel = new javax.swing.JLabel();
        unitsLabel = new javax.swing.JLabel();
        completeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(DeleteGUI.class);
        setTitle(resourceMap.getString("DeleteGUI.title")); // NOI18N
        setName("DeleteGUI"); // NOI18N

        codeTextField.setName("codeTextField"); // NOI18N

        buttonGroup1.add(manualRadioButton);
        manualRadioButton.setText(resourceMap.getString("manualRadioButton.text")); // NOI18N
        manualRadioButton.setName("manualRadioButton"); // NOI18N
        manualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualRadioButtonActionPerformed(evt);
            }
        });

        separator.setName("separator"); // NOI18N

        buttonGroup1.add(autoRadioButton);
        autoRadioButton.setSelected(true);
        autoRadioButton.setText(resourceMap.getString("autoRadioButton.text")); // NOI18N
        autoRadioButton.setName("autoRadioButton"); // NOI18N
        autoRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoRadioButtonActionPerformed(evt);
            }
        });

        deleteButton.setText(resourceMap.getString("deleteButton.text")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        nameTextField.setName("nameTextField"); // NOI18N

        expTextField.setName("expTextField"); // NOI18N

        unitsTextField.setName("unitsTextField"); // NOI18N

        codeLabel.setText(resourceMap.getString("codeLabel.text")); // NOI18N
        codeLabel.setName("codeLabel"); // NOI18N

        nameLabel.setText(resourceMap.getString("nameLabel.text")); // NOI18N
        nameLabel.setName("nameLabel"); // NOI18N

        expLabel.setText(resourceMap.getString("expLabel.text")); // NOI18N
        expLabel.setName("expLabel"); // NOI18N

        unitsLabel.setText(resourceMap.getString("unitsLabel.text")); // NOI18N
        unitsLabel.setName("unitsLabel"); // NOI18N

        completeButton.setText(resourceMap.getString("completeButton.text")); // NOI18N
        completeButton.setName("completeButton"); // NOI18N
        completeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                completeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(autoRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addComponent(expLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(manualRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addComponent(nameLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                        .addComponent(unitsLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(completeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(codeLabel)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(unitsTextField)
                    .addComponent(expTextField)
                    .addComponent(nameTextField)
                    .addComponent(codeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(230, 230, 230)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(codeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codeLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(completeButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manualRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expLabel))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteButton)
                    .addComponent(unitsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unitsLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void manualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualRadioButtonActionPerformed
        // TODO add your handling code here:
        if (manualRadioButton.isSelected()) {
            cancel();
            resetFields(false);
        }
}//GEN-LAST:event_manualRadioButtonActionPerformed

    private void autoRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoRadioButtonActionPerformed
        // TODO add your handling code here:
        if (autoRadioButton.isSelected()) {
            start();
            resetFields(true);
        }
}//GEN-LAST:event_autoRadioButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // TODO add your handling code here:
        PharmaApp app = PharmaApp.getApplication();
        String codeS = codeTextField.getText();
        String dateS = expTextField.getText();

        if (codeS.isEmpty()||
                dateS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Barcode and date" +
                    " cannot be empty", "Pharma", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            long code = Long.parseLong(codeS);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sdf.parse(dateS);
            boolean result = app.delete(code, date);
            if (result) {
                JOptionPane.showMessageDialog(this, 
                        "A Unit was deleted for this product",
                    "Pharma", JOptionPane.INFORMATION_MESSAGE);
            }
            autoComplete(code, date, false);
        } catch (NumberFormatException ex) {
            Logger.getLogger(InsertGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(InsertGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(InsertGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_deleteButtonActionPerformed

    private void completeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_completeButtonActionPerformed
        try {
            String codeS = codeTextField.getText();
            String dateS = expTextField.getText();
            if (codeS.isEmpty() || dateS.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Barcode and date" +
                    " cannot be empty", "Pharma", JOptionPane.ERROR_MESSAGE);
                //System.err.println("INCOMPLETE!");
                return;
            }
            long code = Long.parseLong(codeS);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sdf.parse(dateS);
            autoComplete(code, date);
        } catch (ParseException ex) {
            Logger.getLogger(DeleteGUI.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_completeButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DeleteGUI dialog = new DeleteGUI(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel codeLabel;
    private javax.swing.JTextField codeTextField;
    private javax.swing.JButton completeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel expLabel;
    private javax.swing.JTextField expTextField;
    private javax.swing.JRadioButton manualRadioButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel unitsLabel;
    private javax.swing.JTextField unitsTextField;
    // End of variables declaration//GEN-END:variables

}
