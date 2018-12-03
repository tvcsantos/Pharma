/*
 * PharmaView.java
 */

package pt.com.santos.pharma;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import pt.com.santos.pharma.db.gui.DeleteGUI;
import pt.com.santos.pharma.db.gui.InsertGUI;

/**
 * The application's main frame.
 */
public class PharmaView extends FrameView {
    
    private TrayIconStreamed trayIcon =
            new TrayIconStreamed(getResourceMap().
            getImageIcon("Application.trayIcon").getImage());

    private Timer timer;
    private TimerTask task;

    public PharmaView(SingleFrameApplication app) {
        super(app);
        initComponents();
        initMyComponents();
    }

    private void initMyComponents() {
        getFrame().setResizable(false);
        getFrame().addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) { }
            public void windowClosing(WindowEvent e) { }
            public void windowClosed(WindowEvent e) { }

            public void windowIconified(WindowEvent e) {
                PharmaView.this.getFrame().setVisible(false);
                PharmaView.this.getFrame().setState(JFrame.ICONIFIED);
            }

            public void windowDeiconified(WindowEvent e) { }
            public void windowActivated(WindowEvent e) { }
            public void windowDeactivated(WindowEvent e) { }
        });

        fillTable();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });

        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                synchronized(this) {
                    checkOutOfDate();
                }
            }
        };

        timer.schedule(task, 5000, 2*60*60*1000);
    }

    private void checkOutOfDate() {
        try {
            PharmaApp app = PharmaApp.getApplication();
            int x = app.expiredSize();
            trayIcon.displayMessage("Pharma", x +
                    " medicament expired", MessageType.INFO);
        } catch (SQLException ex) {
            Logger.getLogger(PharmaView.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

     private void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem showSedItem = new MenuItem("Show Pharma");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to popup menu
        popup.add(showSedItem);
        popup.addSeparator();
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Pharma");

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PharmaView.this.getFrame().setVisible(true);
                PharmaView.this.getFrame().setState(JFrame.NORMAL);
            }
        });

        showSedItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PharmaView.this.getFrame().setVisible(true);
                PharmaView.this.getFrame().setState(JFrame.NORMAL);
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PharmaView.this.showAboutBox();
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                getApplication().exit();
            }
        });
    }

    public void fillTable() {
        DefaultTableModel dtm = (DefaultTableModel) mainTable.getModel();
        dtm.getDataVector().removeAllElements();
        PharmaApp app = PharmaApp.getApplication();
        try {
            ResultSet rs = app.list();
            while (rs.next()) {
                long barcode = rs.getLong(1);
                String name = rs.getString(2);
                java.sql.Date date = rs.getDate(3);
                int units = rs.getInt(4);
                System.out.println(barcode + " " + name + " " +
                        date + " " + units);
                dtm.addRow(new Object[]{ barcode, name, date, units});
            }
            app.closeStat();
        } catch (SQLException ex) {
            Logger.getLogger(PharmaView.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        dtm.fireTableDataChanged();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PharmaApp.getApplication().getMainFrame();
            aboutBox = new PharmaAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PharmaApp.getApplication().show(aboutBox);
    }

    public void showAddBox() {
        if (addBox == null) {
            JFrame mainFrame = PharmaApp.getApplication().getMainFrame();
            addBox = new InsertGUI(mainFrame, true);
            addBox.setLocationRelativeTo(mainFrame);
            addBox.addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {}

            public void windowClosing(WindowEvent e) {
                fillTable();
            }

            public void windowClosed(WindowEvent e) {}

            public void windowIconified(WindowEvent e) {}

            public void windowDeiconified(WindowEvent e) {}

            public void windowActivated(WindowEvent e) {}

            public void windowDeactivated(WindowEvent e) {}
        });
        }
        addBox.start();
        PharmaApp.getApplication().show(addBox);
    }

    public void showDeleteBox() {
         if (deleteBox == null) {
            JFrame mainFrame = PharmaApp.getApplication().getMainFrame();
            deleteBox = new DeleteGUI(mainFrame, true);
            deleteBox.setLocationRelativeTo(mainFrame);
            deleteBox.addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {}

            public void windowClosing(WindowEvent e) {
                fillTable();
            }

            public void windowClosed(WindowEvent e) {}

            public void windowIconified(WindowEvent e) {}

            public void windowDeiconified(WindowEvent e) {}

            public void windowActivated(WindowEvent e) {}

            public void windowDeactivated(WindowEvent e) {}
        });
        }
        deleteBox.start();
        PharmaApp.getApplication().show(deleteBox);
    }

    public void showListBox(ResultSet rs) {
        if (listBox == null) {
            JFrame mainFrame = PharmaApp.getApplication().getMainFrame();
            listBox = new ListGUI(mainFrame, true, rs);
            listBox.setLocationRelativeTo(mainFrame);
        }
        PharmaApp.getApplication().show(listBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        expiredListMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        mainTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Bar Code", "Name", "Expiration", "Units"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        mainTable.setName("mainTableName"); // NOI18N
        mainTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(mainTable);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(pt.com.santos.pharma.PharmaApp.class).getContext().getResourceMap(PharmaView.class);
        mainTable.getColumnModel().getColumn(0).setResizable(false);
        mainTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("mainTableName.columnModel.title0")); // NOI18N
        mainTable.getColumnModel().getColumn(1).setResizable(false);
        mainTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("mainTableName.columnModel.title1")); // NOI18N
        mainTable.getColumnModel().getColumn(2).setResizable(false);
        mainTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("mainTableName.columnModel.title2")); // NOI18N
        mainTable.getColumnModel().getColumn(3).setResizable(false);
        mainTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("mainTableName.columnModel.title3")); // NOI18N

        addButton.setText(resourceMap.getString("addButton.text")); // NOI18N
        addButton.setName("addButton"); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText(resourceMap.getString("deleteButton.text")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 270, Short.MAX_VALUE)
                .addComponent(deleteButton)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteButton)
                    .addComponent(addButton))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(pt.com.santos.pharma.PharmaApp.class).getContext().getActionMap(PharmaView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        expiredListMenuItem.setText(resourceMap.getString("expiredListMenuItem.text")); // NOI18N
        expiredListMenuItem.setName("expiredListMenuItem"); // NOI18N
        expiredListMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expiredListMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(expiredListMenuItem);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        showAddBox();
        listBox = null;
    }//GEN-LAST:event_addButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        showDeleteBox();
        listBox = null;
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void expiredListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expiredListMenuItemActionPerformed
        try {
            PharmaApp app = PharmaApp.getApplication();
            ResultSet rs = app.expired();
            showListBox(rs);
            app.closeStat();
        } catch (SQLException ex) {
            Logger.getLogger(PharmaView.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_expiredListMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JMenuItem expiredListMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTable mainTable;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables

    private JDialog aboutBox;
    private InsertGUI addBox;
    private DeleteGUI deleteBox;
    private ListGUI listBox;
}
